<template>
  <div class="charts-panel space-y-6">
    <!-- 情绪趋势图 -->
    <div class="chart-card rounded-2xl p-6" :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
      <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">
        📈 情绪变化趋势
      </h3>
      <div ref="emotionChartRef" class="w-full h-64"></div>
    </div>
    
    <!-- 消费趋势图 -->
    <div class="chart-card rounded-2xl p-6" :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
      <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">
        💰 消费趋势
      </h3>
      <div ref="expenseChartRef" class="w-full h-64"></div>
    </div>
    
    <!-- 类型分布饼图 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="chart-card rounded-2xl p-6" :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
        <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">
          📝 记录类型分布
        </h3>
        <div ref="typePieChartRef" class="w-full h-48"></div>
      </div>
      
      <!-- 标签词云 -->
      <div class="chart-card rounded-2xl p-6" :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
        <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">
          🏷️ 热门标签
        </h3>
        <div ref="tagChartRef" class="w-full h-48"></div>
      </div>
    </div>
    
    <!-- 时间热力图 -->
    <div class="chart-card rounded-2xl p-6" :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
      <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">
        🔥 活动热力图
      </h3>
      <div ref="heatmapChartRef" class="w-full h-48"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'
import { format, subDays, startOfDay } from 'date-fns'
import { useSettingsStore } from '../store/useSettingsStore.js'
import { useResizeObserver } from '@vueuse/core'

const props = defineProps({
  records: {
    type: Array,
    default: () => []
  }
})

const settingsStore = useSettingsStore()

const emotionChartRef = ref(null)
const expenseChartRef = ref(null)
const typePieChartRef = ref(null)
const tagChartRef = ref(null)
const heatmapChartRef = ref(null)

let emotionChart = null
let expenseChart = null
let typePieChart = null
let tagChart = null
let heatmapChart = null

// 使用 VueUse 的 resize 监听
useResizeObserver(emotionChartRef, () => emotionChart?.resize())
useResizeObserver(expenseChartRef, () => expenseChart?.resize())
useResizeObserver(typePieChartRef, () => typePieChart?.resize())
useResizeObserver(tagChartRef, () => tagChart?.resize())
useResizeObserver(heatmapChartRef, () => heatmapChart?.resize())

// 情绪趋势数据
const emotionTrendData = computed(() => {
  const last7Days = Array.from({ length: 7 }, (_, i) => {
    const date = subDays(new Date(), 6 - i)
    return {
      date: format(date, 'MM-dd'),
      fullDate: startOfDay(date),
      scores: []
    }
  })
  
  props.records.forEach(record => {
    if (record.emotionScore !== null && record.recordTime) {
      const recordDate = startOfDay(new Date(record.recordTime))
      const dayData = last7Days.find(d => 
        d.fullDate.getTime() === recordDate.getTime()
      )
      if (dayData) {
        dayData.scores.push(record.emotionScore)
      }
    }
  })
  
  return last7Days.map(d => ({
    date: d.date,
    avg: d.scores.length > 0 
      ? (d.scores.reduce((a, b) => a + b, 0) / d.scores.length).toFixed(1)
      : null
  }))
})

// 消费趋势数据
const expenseTrendData = computed(() => {
  const last7Days = Array.from({ length: 7 }, (_, i) => {
    const date = subDays(new Date(), 6 - i)
    return {
      date: format(date, 'MM-dd'),
      fullDate: startOfDay(date),
      amount: 0
    }
  })
  
  props.records.forEach(record => {
    if (record.recordType && record.recordType.includes('expense') && record.amount && record.recordTime) {
      const recordDate = startOfDay(new Date(record.recordTime))
      const dayData = last7Days.find(d => 
        d.fullDate.getTime() === recordDate.getTime()
      )
      if (dayData) {
        dayData.amount += parseFloat(record.amount)
      }
    }
  })
  
  return last7Days.map(d => ({
    date: d.date,
    amount: d.amount
  }))
})

// 类型分布数据
const typeDistributionData = computed(() => {
  const typeCount = {}
  props.records.forEach(record => {
    const type = record.recordType || 'diary'
    typeCount[type] = (typeCount[type] || 0) + 1
  })
  
  const typeNames = {
    expense: '消费',
    mood: '情绪',
    event: '事件',
    diary: '日记'
  }
  
  const colors = {
    expense: '#22c55e',
    mood: '#f59e0b',
    event: '#3b82f6',
    diary: '#8b5cf6'
  }
  
  return Object.entries(typeCount).map(([type, count]) => ({
    name: typeNames[type] || type,
    value: count,
    itemStyle: { color: colors[type] || '#999' }
  }))
})

// 标签数据
const tagData = computed(() => {
  const tagCount = {}
  props.records.forEach(record => {
    if (record.tags) {
      const tags = record.tags.split(',').map(t => t.trim()).filter(Boolean)
      tags.forEach(tag => {
        tagCount[tag] = (tagCount[tag] || 0) + 1
      })
    }
  })
  
  return Object.entries(tagCount)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10)
    .map(([name, value]) => ({ name, value }))
})

// 初始化情绪趋势图
const initEmotionChart = () => {
  if (!emotionChartRef.value) return
  
  emotionChart = echarts.init(emotionChartRef.value)
  updateEmotionChart()
}

const updateEmotionChart = () => {
  if (!emotionChart) return
  
  const data = emotionTrendData.value
  const themeColor = settingsStore.theme.colors.primaryColor
  
  const option = {
    grid: { top: 20, right: 20, bottom: 30, left: 40 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date),
      axisLine: { lineStyle: { color: settingsStore.theme.colors.textMuted } },
      axisLabel: { color: settingsStore.theme.colors.textSecondary }
    },
    yAxis: {
      type: 'value',
      min: -10,
      max: 10,
      axisLine: { lineStyle: { color: settingsStore.theme.colors.textMuted } },
      axisLabel: { color: settingsStore.theme.colors.textSecondary },
      splitLine: { lineStyle: { color: settingsStore.theme.colors.bgSecondary } }
    },
    series: [{
      data: data.map(d => d.avg),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: themeColor, width: 3 },
      itemStyle: { color: themeColor },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: themeColor + '40' },
          { offset: 1, color: themeColor + '00' }
        ])
      }
    }]
  }
  
  emotionChart.setOption(option)
}

// 初始化消费趋势图
const initExpenseChart = () => {
  if (!expenseChartRef.value) return
  
  expenseChart = echarts.init(expenseChartRef.value)
  updateExpenseChart()
}

const updateExpenseChart = () => {
  if (!expenseChart) return
  
  const data = expenseTrendData.value
  
  const option = {
    grid: { top: 20, right: 20, bottom: 30, left: 50 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date),
      axisLine: { lineStyle: { color: settingsStore.theme.colors.textMuted } },
      axisLabel: { color: settingsStore.theme.colors.textSecondary }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: settingsStore.theme.colors.textMuted } },
      axisLabel: { 
        color: settingsStore.theme.colors.textSecondary,
        formatter: '¥{value}'
      },
      splitLine: { lineStyle: { color: settingsStore.theme.colors.bgSecondary } }
    },
    series: [{
      data: data.map(d => d.amount),
      type: 'bar',
      barWidth: '60%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#22c55e' },
          { offset: 1, color: '#16a34a' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  }
  
  expenseChart.setOption(option)
}

// 初始化类型饼图
const initTypePieChart = () => {
  if (!typePieChartRef.value) return
  
  typePieChart = echarts.init(typePieChartRef.value)
  updateTypePieChart()
}

const updateTypePieChart = () => {
  if (!typePieChart) return
  
  const data = typeDistributionData.value
  
  const option = {
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: settingsStore.theme.colors.bgPrimary,
        borderWidth: 2
      },
      label: {
        show: true,
        color: settingsStore.theme.colors.textSecondary
      },
      labelLine: {
        lineStyle: { color: settingsStore.theme.colors.textMuted }
      },
      data: data
    }]
  }
  
  typePieChart.setOption(option)
}

// 初始化标签图
const initTagChart = () => {
  if (!tagChartRef.value) return
  
  tagChart = echarts.init(tagChartRef.value)
  updateTagChart()
}

const updateTagChart = () => {
  if (!tagChart) return
  
  const data = tagData.value
  const themeColor = settingsStore.theme.colors.primaryColor
  
  const option = {
    grid: { top: 10, right: 20, bottom: 20, left: 80 },
    xAxis: {
      type: 'value',
      show: false
    },
    yAxis: {
      type: 'category',
      data: data.map(d => d.name).reverse(),
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: settingsStore.theme.colors.textSecondary }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.value).reverse(),
      barWidth: 16,
      itemStyle: {
        color: themeColor,
        borderRadius: [0, 8, 8, 0]
      },
      label: {
        show: true,
        position: 'right',
        color: settingsStore.theme.colors.textSecondary
      }
    }]
  }
  
  tagChart.setOption(option)
}

// 初始化热力图
const initHeatmapChart = () => {
  if (!heatmapChartRef.value) return
  
  heatmapChart = echarts.init(heatmapChartRef.value)
  updateHeatmapChart()
}

const updateHeatmapChart = () => {
  if (!heatmapChart) return
  
  // 生成最近30天的热力图数据
  const data = []
  const today = new Date()
  
  for (let i = 29; i >= 0; i--) {
    const date = subDays(today, i)
    const dateStr = format(date, 'yyyy-MM-dd')
    const dayRecords = props.records.filter(r => {
      if (!r.recordTime) return false
      return format(new Date(r.recordTime), 'yyyy-MM-dd') === dateStr
    })
    data.push([dateStr, dayRecords.length])
  }
  
  const option = {
    visualMap: {
      min: 0,
      max: 5,
      show: false,
      inRange: {
        color: [settingsStore.theme.colors.bgSecondary, settingsStore.theme.colors.primaryColor]
      }
    },
    calendar: {
      range: [format(subDays(today, 29), 'yyyy-MM-dd'), format(today, 'yyyy-MM-dd')],
      cellSize: ['auto', 20],
      itemStyle: {
        borderWidth: 2,
        borderColor: settingsStore.theme.colors.bgPrimary
      },
      splitLine: { show: false },
      dayLabel: { color: settingsStore.theme.colors.textSecondary },
      monthLabel: { color: settingsStore.theme.colors.textSecondary }
    },
    series: [{
      type: 'heatmap',
      coordinateSystem: 'calendar',
      data: data
    }]
  }
  
  heatmapChart.setOption(option)
}

onMounted(() => {
  initEmotionChart()
  initExpenseChart()
  initTypePieChart()
  initTagChart()
  initHeatmapChart()
})

onUnmounted(() => {
  emotionChart?.dispose()
  expenseChart?.dispose()
  typePieChart?.dispose()
  tagChart?.dispose()
  heatmapChart?.dispose()
})

// 监听数据变化更新图表
watch(() => props.records, () => {
  updateEmotionChart()
  updateExpenseChart()
  updateTypePieChart()
  updateTagChart()
  updateHeatmapChart()
}, { deep: true })

// 监听主题变化
watch(() => settingsStore.currentTheme, () => {
  setTimeout(() => {
    updateEmotionChart()
    updateExpenseChart()
    updateTypePieChart()
    updateTagChart()
    updateHeatmapChart()
  }, 100)
})
</script>

<style scoped>
.chart-card {
  transition: all 0.3s ease;
}
</style>
