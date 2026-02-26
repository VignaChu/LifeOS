<template>
  <div class="stats-panel">
    <h3 class="text-lg font-semibold mb-4" :style="{ color: settingsStore.theme.colors.textPrimary }">
      数据统计
    </h3>
    
    <div class="grid grid-cols-2 gap-4">
      <div class="stat-card p-4 rounded-xl" :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
        <div class="text-sm mb-1" :style="{ color: settingsStore.theme.colors.textMuted }">总记录</div>
        <div class="text-2xl font-bold" :style="{ color: settingsStore.theme.colors.primaryColor }">{{ stats.totalRecords }}</div>
      </div>
      
      <div class="stat-card p-4 rounded-xl" :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
        <div class="text-sm mb-1" :style="{ color: settingsStore.theme.colors.textMuted }">总消费</div>
        <div class="text-2xl font-bold" style="color: #22c55e">¥{{ stats.totalExpenses.toFixed(0) }}</div>
      </div>
      
      <div class="stat-card p-4 rounded-xl" :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
        <div class="text-sm mb-1" :style="{ color: settingsStore.theme.colors.textMuted }">平均情绪</div>
        <div class="flex items-center gap-2">
          <svg class="w-6 h-6" style="color: white; fill: white;" viewBox="0 0 24 24">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
          </svg>
          <div class="text-2xl font-bold" :class="emotionColorClass">{{ stats.avgEmotion.toFixed(1) }}</div>
        </div>
      </div>
      
      <div class="stat-card p-4 rounded-xl" :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
        <div class="text-sm mb-1" :style="{ color: settingsStore.theme.colors.textMuted }">标签数</div>
        <div class="text-2xl font-bold" :style="{ color: '#8b5cf6' }">{{ stats.uniqueTags }}</div>
      </div>
    </div>
    
    <div v-if="expenseByDay.length > 0" class="mt-6">
      <h4 class="text-sm font-medium mb-2" :style="{ color: settingsStore.theme.colors.textSecondary }">
        近7天消费趋势
      </h4>
      <div class="flex items-end justify-between h-24 space-x-1">
        <div 
          v-for="(day, index) in expenseByDay" 
          :key="index"
          class="flex-1 rounded-t transition-all duration-300"
          :style="{ 
            height: getBarHeight(day.amount) + '%', 
            backgroundColor: settingsStore.theme.colors.primaryColor,
            minHeight: day.amount > 0 ? '4px' : '0'
          }"
          :title="day.label + ': ¥' + day.amount.toFixed(0)"
        ></div>
      </div>
      <div class="flex justify-between mt-1">
        <span v-for="(day, index) in expenseByDay" :key="index" class="text-xs" :style="{ color: settingsStore.theme.colors.textMuted }">
          {{ day.shortLabel }}
        </span>
      </div>
    </div>
    
    <div v-if="typeBreakdown.length > 0" class="mt-6">
      <h4 class="text-sm font-medium mb-2" :style="{ color: settingsStore.theme.colors.textSecondary }">
        记录类型分布
      </h4>
      <div class="space-y-2">
        <div v-for="type in typeBreakdown" :key="type.name" class="flex items-center">
          <span class="text-xs w-12" :style="{ color: settingsStore.theme.colors.textMuted }">{{ type.name }}</span>
          <div class="flex-1 mx-2 h-2 rounded-full overflow-hidden" :style="{ backgroundColor: settingsStore.theme.colors.bgSecondary }">
            <div 
              class="h-full rounded-full transition-all duration-500"
              :style="{ width: type.percent + '%', backgroundColor: type.color }"
            ></div>
          </div>
          <span class="text-xs w-8 text-right" :style="{ color: settingsStore.theme.colors.textSecondary }">{{ type.count }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useSettingsStore } from '../store/useSettingsStore.js'

const props = defineProps({
  records: {
    type: Array,
    default: () => []
  }
})

const settingsStore = useSettingsStore()

const stats = computed(() => {
  const records = props.records
  const totalRecords = records.length
  
  const totalExpenses = records
    .filter(r => r.recordType && r.recordType.includes('expense') && r.amount)
    .reduce((sum, r) => sum + parseFloat(r.amount), 0)
  
  const emotionScores = records
    .filter(r => r.emotionScore !== null && r.emotionScore !== undefined)
    .map(r => parseFloat(r.emotionScore) || 0)
  const avgEmotion = emotionScores.length > 0
    ? emotionScores.reduce((a, b) => a + b, 0) / emotionScores.length
    : 0
  
  const allTags = records
    .filter(r => r.tags)
    .flatMap(r => {
      try {
        return typeof r.tags === 'string' ? JSON.parse(r.tags) : r.tags
      } catch {
        return []
      }
    })
  const uniqueTags = new Set(allTags).size
  
  return { totalRecords, totalExpenses, avgEmotion, uniqueTags }
})

const emotionColorClass = computed(() => {
  const score = stats.value.avgEmotion
  if (score > 3) return 'text-green-500'
  if (score > 0) return 'text-lime-500'
  if (score === 0) return 'text-gray-500'
  if (score > -3) return 'text-orange-500'
  return 'text-red-500'
})

const getEmotionIconColor = () => {
  const score = stats.value.avgEmotion
  if (score > 3) return '#22c55e'
  if (score > 0) return '#84cc16'
  if (score === 0) return '#6b7280'
  if (score > -3) return '#f97316'
  return '#ef4444'
}

const expenseByDay = computed(() => {
  const days = []
  const now = new Date()
  
  for (let i = 6; i >= 0; i--) {
    const date = new Date(now)
    date.setDate(date.getDate() - i)
    const dateStr = date.toISOString().split('T')[0]
    
    const dayExpenses = props.records
      .filter(r => {
        if (!r.recordType || !r.recordType.includes('expense') || !r.amount) return false
        const recordDate = new Date(r.recordTime).toISOString().split('T')[0]
        return recordDate === dateStr
      })
      .reduce((sum, r) => sum + parseFloat(r.amount), 0)
    
    days.push({
      label: `${date.getMonth() + 1}/${date.getDate()}`,
      shortLabel: `${date.getDate()}`,
      amount: dayExpenses
    })
  }
  
  return days
})

const maxExpense = computed(() => {
  return Math.max(...expenseByDay.value.map(d => d.amount), 1)
})

const getBarHeight = (amount) => {
  return (amount / maxExpense.value) * 100
}

const typeBreakdown = computed(() => {
  const typeMap = {
    expense: { name: '消费', color: '#22c55e' },
    diary: { name: '日记', color: '#3b82f6' },
    event: { name: '事件', color: '#8b5cf6' },
    mood: { name: '情绪', color: '#f59e0b' }
  }
  
  const counts = {}
  props.records.forEach(r => {
    const type = r.recordType || 'unknown'
    counts[type] = (counts[type] || 0) + 1
  })
  
  const total = props.records.length || 1
  return Object.entries(counts)
    .map(([key, count]) => ({
      name: typeMap[key]?.name || key,
      color: typeMap[key]?.color || '#6b7280',
      count,
      percent: (count / total) * 100
    }))
    .sort((a, b) => b.count - a.count)
})
</script>
