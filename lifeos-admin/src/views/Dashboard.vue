<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="24" color="#fff">
              <component :is="stat.icon" />
            </el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">{{ stat.title }}</div>
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-change" :class="stat.trend">
              {{ stat.change }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>记录类型分布</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">全年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="pieChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>记录趋势</span>
              <el-radio-group v-model="trendTimeRange" size="small" @change="updateTrendChart">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">全年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="lineChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近记录和系统信息 -->
    <el-row :gutter="20" class="info-row">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近记录</span>
              <el-button type="primary" link @click="$router.push('/records')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="recentRecords" style="width: 100%">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
            <el-table-column prop="recordType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getTypeTag(row.recordType)">
                  {{ getTypeLabel(row.recordType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recordTime" label="时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <div class="system-info">
            <div class="info-item">
              <span class="label">应用名称:</span>
              <span class="value">{{ systemInfo.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">版本:</span>
              <span class="value">{{ systemInfo.version }}</span>
            </div>
            <div class="info-item">
              <span class="label">运行时间:</span>
              <span class="value">{{ uptime }}</span>
            </div>
            <div class="info-item">
              <span class="label">API 调用次数:</span>
              <span class="value">{{ apiCalls }} 次</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { recordApi, actuatorApi } from '../api'

const timeRange = ref('week')
const trendTimeRange = ref('week')
const pieChart = ref(null)
const lineChart = ref(null)
let pieChartInstance = null
let lineChartInstance = null
let allRecords = ref([])

// 统计数据
const stats = ref([
  { title: '总记录数', value: 0, change: '+12%', trend: 'up', icon: 'Document', color: '#409EFF' },
  { title: '今日新增', value: 0, change: '+5%', trend: 'up', icon: 'Plus', color: '#67C23A' },
  { title: '消费总额', value: '¥0', change: '-3%', trend: 'down', icon: 'Money', color: '#E6A23C' },
  { title: '平均情绪', value: '0', change: '+8%', trend: 'up', icon: 'HeartFilled', color: '#F56C6C' }
])

// 最近记录
const recentRecords = ref([])

// 系统信息
const systemInfo = ref({ name: '-', version: '-' })
const uptime = ref('-')
const apiCalls = ref(0)

// 获取类型标签
const getTypeTag = (type) => {
  const tags = { expense: 'danger', mood: 'warning', event: 'success', diary: 'info' }
  return tags[type] || 'info'
}

const getTypeLabel = (type) => {
  const labels = { expense: '消费', mood: '情绪', event: '事件', diary: '日记' }
  return labels[type] || type
}

// 初始化图表
const initCharts = () => {
  // 饼图
  pieChartInstance = echarts.init(pieChart.value)
  pieChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
      data: [
        { value: 0, name: '消费', itemStyle: { color: '#F56C6C' } },
        { value: 0, name: '情绪', itemStyle: { color: '#E6A23C' } },
        { value: 0, name: '事件', itemStyle: { color: '#67C23A' } },
        { value: 0, name: '日记', itemStyle: { color: '#409EFF' } }
      ]
    }]
  })

  // 折线图
  lineChartInstance = echarts.init(lineChart.value)
  lineChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: { type: 'value' },
    series: [{
      data: [0, 0, 0, 0, 0, 0, 0],
      type: 'line',
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ]
        }
      },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

// 获取本地日期字符串 YYYY-MM-DD
const getLocalDateStr = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 更新趋势图表
const updateTrendChart = () => {
  if (!lineChartInstance || !allRecords.value.length) return
  
  const data = allRecords.value
  const now = new Date()
  let labels = []
  let counts = []
  
  if (trendTimeRange.value === 'week') {
    // 最近7天
    for (let i = 6; i >= 0; i--) {
      const date = new Date(now)
      date.setDate(date.getDate() - i)
      const dateStr = getLocalDateStr(date)
      labels.push(date.getDate() + '日')
      
      const count = data.filter(r => {
        if (!r.recordTime) return false
        // 兼容多种日期格式
        const recordDateStr = r.recordTime.split('T')[0].split(' ')[0]
        return recordDateStr === dateStr
      }).length
      counts.push(count)
    }
  } else if (trendTimeRange.value === 'month') {
    // 最近30天，按周汇总
    for (let i = 3; i >= 0; i--) {
      const endDate = new Date(now)
      endDate.setDate(endDate.getDate() - i * 7)
      const startDate = new Date(endDate)
      startDate.setDate(startDate.getDate() - 6)
      
      labels.push(`${startDate.getDate()}-${endDate.getDate()}日`)
      
      const count = data.filter(r => {
        if (!r.recordTime) return false
        const recordDate = new Date(r.recordTime)
        return recordDate >= startDate && recordDate <= endDate
      }).length
      counts.push(count)
    }
  } else if (trendTimeRange.value === 'year') {
    // 最近12个月
    for (let i = 11; i >= 0; i--) {
      const date = new Date(now)
      date.setMonth(date.getMonth() - i)
      const yearMonth = getLocalDateStr(date).slice(0, 7) // YYYY-MM
      labels.push(date.getMonth() + 1 + '月')
      
      const count = data.filter(r => {
        if (!r.recordTime) return false
        return r.recordTime.slice(0, 7) === yearMonth
      }).length
      counts.push(count)
    }
  }
  
  lineChartInstance.setOption({
    xAxis: { data: labels },
    series: [{ data: counts }]
  })
}

// 加载数据
const loadData = async () => {
  try {
    // 加载记录
    const records = await recordApi.getAll()
    console.log('Records data:', records)
    
    if (records.success && records.data) {
      const data = records.data
      allRecords.value = data
      recentRecords.value = data.slice(0, 5)
      
      // 获取今天的日期（YYYY-MM-DD格式，本地时间）
      const today = getLocalDateStr(new Date())
      console.log('Today:', today)
      
      // 计算今日新增
      const todayCount = data.filter(r => {
        if (!r.recordTime) return false
        // 兼容多种日期格式：2025-02-25T10:00:00 或 2025-02-25 10:00:00
        const recordDate = r.recordTime.split('T')[0].split(' ')[0]
        console.log('Record date:', recordDate, 'Today:', today, 'Match:', recordDate === today)
        return recordDate === today
      }).length
      
      // 计算消费总额
      const totalExpense = data
        .filter(r => r.recordType === 'expense' && r.amount)
        .reduce((sum, r) => sum + parseFloat(r.amount), 0)
      
      // 计算平均情绪
      const moodRecords = data.filter(r => r.recordType === 'mood' && r.emotionScore !== null)
      const avgEmotion = moodRecords.length > 0 
        ? (moodRecords.reduce((sum, r) => sum + r.emotionScore, 0) / moodRecords.length).toFixed(1)
        : 0
      
      // 更新统计数据
      stats.value[0].value = data.length
      stats.value[1].value = todayCount
      stats.value[2].value = '¥' + totalExpense.toFixed(2)
      stats.value[3].value = avgEmotion
      
      // 更新饼图
      const typeCount = { expense: 0, mood: 0, event: 0, diary: 0 }
      data.forEach(r => { 
        if (r.recordType) typeCount[r.recordType]++ 
      })
      
      if (pieChartInstance) {
        pieChartInstance.setOption({
          series: [{ data: [
            { value: typeCount.expense, name: '消费', itemStyle: { color: '#F56C6C' } },
            { value: typeCount.mood, name: '情绪', itemStyle: { color: '#E6A23C' } },
            { value: typeCount.event, name: '事件', itemStyle: { color: '#67C23A' } },
            { value: typeCount.diary, name: '日记', itemStyle: { color: '#409EFF' } }
          ]}]
        })
      }
      
      // 更新趋势图表
      updateTrendChart()
    }

    // 加载系统信息
    try {
      const info = await actuatorApi.getInfo()
      if (info.data) {
        systemInfo.value = info.data.app || { name: 'LifeOS', version: '1.0.0' }
      }
    } catch (e) {
      systemInfo.value = { name: 'LifeOS', version: '1.0.0' }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

onMounted(() => {
  initCharts()
  loadData()
  window.addEventListener('resize', () => {
    pieChartInstance?.resize()
    lineChartInstance?.resize()
  })
})

onUnmounted(() => {
  pieChartInstance?.dispose()
  lineChartInstance?.dispose()
})
</script>

<style scoped>
.dashboard {
  padding-bottom: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
}

.stat-change.up {
  color: #67c23a;
}

.stat-change.down {
  color: #f56c6c;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-row {
  margin-bottom: 20px;
}

.system-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  color: #606266;
}

.value {
  color: #303133;
  font-weight: 500;
}
</style>
