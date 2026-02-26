<template>
  <div class="record-card card cursor-pointer transition-all duration-300"
       :style="{ 
         '--hover-bg': settingsStore.theme.id === 'dark' || settingsStore.theme.id === 'midnight' || settingsStore.theme.id === 'forest' 
           ? 'rgba(30, 41, 59, 0.5)' 
           : 'rgba(249, 250, 251, 0.5)'
       }"
       @mouseenter="hovered = true"
       @mouseleave="hovered = false"
       :class="{ 'hovered': hovered }">
    <div class="flex items-start justify-between">
      <div class="flex-1">
        <!-- 类型标签 -->
        <div class="flex items-center space-x-2 mb-2">
          <span :class="typeBadgeClass" class="px-2 py-1 rounded-full text-xs font-medium">
            {{ typeLabel }}
          </span>
          <span v-if="record.amount" class="font-semibold" style="color: #22c55e">
            ¥{{ parseFloat(record.amount).toFixed(2) }}
          </span>
        </div>
        
        <!-- 原文 -->
        <p class="transition-colors duration-300"
           :style="{ color: settingsStore.theme.colors.textPrimary }">
          {{ record.originalText }}
        </p>
        
        <!-- 标签 -->
        <div v-if="parsedTags.length > 0" class="flex flex-wrap gap-1 mb-2 mt-2">
          <span 
            v-for="tag in parsedTags" 
            :key="tag" 
            class="px-2 py-0.5 rounded text-xs transition-colors duration-300"
            :style="{ 
              backgroundColor: settingsStore.theme.colors.bgSecondary,
              color: settingsStore.theme.colors.textSecondary
            }"
          >
            #{{ tag }}
          </span>
        </div>
        
        <!-- 时间和情绪 -->
        <div class="flex items-center justify-between text-sm mt-2"
             :style="{ color: settingsStore.theme.colors.textMuted }">
          <span>{{ formatTime(record.recordTime) }}</span>
          <div v-if="record.emotionScore !== null" class="flex items-center space-x-1">
            <span>{{ emotionEmoji }}</span>
            <span :class="emotionClass">{{ record.emotionScore > 0 ? '+' : '' }}{{ record.emotionScore }}</span>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="ml-4 flex flex-col space-y-2">
        <button 
          @click.stop="handleEdit" 
          class="p-2 rounded-lg transition-colors duration-200"
          :class="settingsStore.theme.id === 'dark' || settingsStore.theme.id === 'midnight' || settingsStore.theme.id === 'forest'
            ? 'text-slate-500 hover:text-blue-400 hover:bg-blue-900/20' 
            : 'text-gray-400 hover:text-blue-500 hover:bg-blue-50'"
          title="编辑记录"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
        </button>
        <button 
          @click.stop="handleDelete" 
          class="p-2 rounded-lg transition-colors duration-200"
          :class="settingsStore.theme.id === 'dark' || settingsStore.theme.id === 'midnight' || settingsStore.theme.id === 'forest'
            ? 'text-slate-500 hover:text-red-400 hover:bg-red-900/20' 
            : 'text-gray-400 hover:text-red-500 hover:bg-red-50'"
          title="删除记录"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
      </div>
    </div>
  </div>
  
  <!-- 删除确认弹窗 -->
  <Teleport to="body">
    <div v-if="showDeleteConfirm" class="fixed inset-0 z-50 flex items-center justify-center">
      <div class="absolute inset-0 bg-black/50" @click="cancelDelete"></div>
      <div class="relative w-full max-w-sm rounded-2xl p-6 shadow-2xl"
           :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
        <div class="text-center">
          <div class="w-12 h-12 mx-auto mb-4 rounded-full flex items-center justify-center"
               :style="{ backgroundColor: '#fee2e2' }">
            <svg class="w-6 h-6 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 class="text-lg font-semibold mb-2" :style="{ color: settingsStore.theme.colors.textPrimary }">
            确定删除记录？
          </h3>
          <p class="text-sm mb-6" :style="{ color: settingsStore.theme.colors.textSecondary }">
            删除后将无法恢复
          </p>
          <div class="flex space-x-3">
            <button 
              @click="cancelDelete"
              class="flex-1 px-4 py-2 rounded-lg transition-colors duration-200"
              :style="{ 
                backgroundColor: settingsStore.theme.colors.bgSecondary,
                color: settingsStore.theme.colors.textPrimary
              }"
            >
              取消
            </button>
            <button 
              @click="confirmDelete"
              class="flex-1 px-4 py-2 rounded-lg transition-colors duration-200 text-white"
              style="background-color: #ef4444"
            >
              删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useSettingsStore } from '../store/useSettingsStore.js'

const props = defineProps({
  record: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['delete', 'edit', 'click'])
const settingsStore = useSettingsStore()
const hovered = ref(false)

const typeLabel = computed(() => {
  const types = {
    expense: '消费',
    diary: '日记',
    event: '事件',
    mood: '情绪'
  }
  return types[props.record.recordType] || '记录'
})

const typeBadgeClass = computed(() => {
  const isDark = settingsStore.theme.id === 'dark' || settingsStore.theme.id === 'midnight' || settingsStore.theme.id === 'forest'
  const classes = {
    expense: isDark ? 'bg-green-900/50 text-green-300' : 'bg-green-100 text-green-700',
    diary: isDark ? 'bg-blue-900/50 text-blue-300' : 'bg-blue-100 text-blue-700',
    event: isDark ? 'bg-purple-900/50 text-purple-300' : 'bg-purple-100 text-purple-700',
    mood: isDark ? 'bg-yellow-900/50 text-yellow-300' : 'bg-yellow-100 text-yellow-700'
  }
  return classes[props.record.recordType] || (isDark ? 'bg-slate-700 text-slate-300' : 'bg-gray-100 text-gray-700')
})

const parsedTags = computed(() => {
  try {
    if (typeof props.record.tags === 'string') {
      return JSON.parse(props.record.tags)
    }
    return props.record.tags || []
  } catch {
    return []
  }
})

const emotionEmoji = computed(() => {
  const score = props.record.emotionScore
  if (score > 5) return '😊'
  if (score > 0) return '🙂'
  if (score === 0) return '😐'
  if (score > -5) return '😔'
  return '😢'
})

const emotionClass = computed(() => {
  const score = props.record.emotionScore
  if (score > 0) return 'text-green-500'
  if (score < 0) return 'text-red-500'
  return ''
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleDateString('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const showDeleteConfirm = ref(false)

const handleDelete = () => {
  showDeleteConfirm.value = true
}

const confirmDelete = () => {
  showDeleteConfirm.value = false
  emit('delete', props.record.id)
}

const cancelDelete = () => {
  showDeleteConfirm.value = false
}

const handleEdit = () => {
  emit('edit', props.record)
}
</script>

<style scoped>
.record-card {
  transition: all 0.2s ease;
}

.record-card.hovered {
  transform: translateY(-2px);
  background-color: var(--hover-bg) !important;
}
</style>
