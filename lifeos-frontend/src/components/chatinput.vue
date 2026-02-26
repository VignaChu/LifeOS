<template>
  <div class="chat-input">
    <div class="relative">
      <textarea
        ref="inputRef"
        v-model="inputText"
        :placeholder="placeholder"
        :disabled="isLoading"
        @keydown.enter.exact.prevent="handleSubmit"
        class="w-full px-4 py-3 pr-24 rounded-lg border resize-none min-h-[56px] max-h-[120px] transition-all duration-200 focus:outline-none"
        :style="{ 
          backgroundColor: settingsStore.theme.colors.inputBg,
          borderColor: settingsStore.theme.colors.inputBorder,
          color: settingsStore.theme.colors.textPrimary
        }"
        rows="1"
      ></textarea>
      
      <!-- 语音输入按钮 -->
      <button
        @click="toggleVoiceInput"
        :disabled="isLoading"
        class="absolute right-20 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full flex items-center justify-center transition-all duration-200 disabled:opacity-50"
        :class="isRecording ? 'bg-red-500 animate-pulse' : ''"
        :style="{ 
          backgroundColor: isRecording 
            ? '#ef4444' 
            : settingsStore.theme.colors.bgSecondary,
          color: isRecording ? 'white' : settingsStore.theme.colors.textSecondary
        }"
        title="语音输入"
      >
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z" />
        </svg>
      </button>
      
      <!-- 发送按钮 -->
      <button
        @click="handleSubmit"
        :disabled="isLoading || !inputText.trim()"
        class="absolute right-3 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full flex items-center justify-center transition-all duration-200 disabled:opacity-50"
        :style="{ 
          backgroundColor: isLoading || !inputText.trim() 
            ? settingsStore.theme.colors.textMuted 
            : settingsStore.theme.colors.accentColor
        }"
      >
        <svg v-if="!isTrackLoading" class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
        </svg>
        <svg v-else class="w-5 h-5 text-white animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
      </button>
    </div>
    
    <!-- 底部提示 -->
    <div class="mt-2 flex items-center justify-between text-xs transition-colors duration-300"
         :style="{ color: settingsStore.theme.colors.textMuted }">
      <div class="flex items-center space-x-4">
        <span>Enter 发送</span>
        <span>Shift + Enter 换行</span>
      </div>
      <div class="flex items-center space-x-2">
        <span>试试：</span>
        <span @click="setExample('今天打车花了30元')" 
              class="cursor-pointer transition-colors duration-200 hover:opacity-80"
              :style="{ color: settingsStore.theme.colors.accentColor }">"今天打车花了30元"</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { useTrackStore } from '../store/useTrackStore.js'
import { useSettingsStore } from '../store/useSettingsStore.js'
import { useUserStore } from '../store/useUserStore.js'

const props = defineProps({
  placeholder: {
    type: String,
    default: '告诉 AI 你做了什么... 例如：今天打车花了30元，然后去吃了一顿150元的烤肉'
  }
})

const emit = defineEmits(['success'])

const trackStore = useTrackStore()
const settingsStore = useSettingsStore()
const userStore = useUserStore()
const inputRef = ref(null)
const inputText = ref('')

const isTrackLoading = computed(() => trackStore.isLoading)
const isQueryLoading = computed(() => trackStore.isLoading)
const isLoading = computed(() => trackStore.isLoading)
const isRecording = ref(false)
let recognition = null

const toggleVoiceInput = () => {
  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

const startRecording = () => {
  if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
    alert('您的浏览器不支持语音识别功能，请使用 Chrome 浏览器')
    return
  }
  
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  recognition = new SpeechRecognition()
  recognition.lang = 'zh-CN'
  recognition.interimResults = true
  recognition.continuous = false
  
  recognition.onstart = () => {
    isRecording.value = true
  }
  
  recognition.onresult = (event) => {
    const transcript = Array.from(event.results)
      .map(result => result[0].transcript)
      .join('')
    inputText.value = transcript
  }
  
  recognition.onerror = (event) => {
    console.error('语音识别错误:', event.error)
    isRecording.value = false
  }
  
  recognition.onend = () => {
    isRecording.value = false
  }
  
  recognition.start()
}

const stopRecording = () => {
  if (recognition) {
    recognition.stop()
    isRecording.value = false
  }
}

const handleSubmit = async () => {
  // 检查是否登录
  if (!userStore.isLoggedIn) {
    userStore.openLoginModal()
    return
  }
  
  const text = inputText.value.trim()
  if (!text || isLoading.value) return
  
  const result = await trackStore.sendText(text)
  
  if (result.success) {
    inputText.value = ''
    emit('success', result.data)
  }
}

const handleQuery = async () => {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return
  
  const result = await trackStore.sendQuery(text)
  
  if (result.success) {
    // 查询结果在store中，可以通过其他方式显示
    console.log('查询结果:', result.data)
  }
}

const setExample = (text) => {
  inputText.value = text
  inputRef.value?.focus()
}

watch(inputText, () => {
  nextTick(() => {
    const textarea = inputRef.value
    if (textarea) {
      textarea.style.height = 'auto'
      textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px'
    }
  })
})
</script>

<style scoped>
.chat-input {
  width: 100%;
}
</style>
