<template>
  <div v-if="userStore.isLoginModalOpen" class="fixed inset-0 z-50 flex items-center justify-center">
    <!-- 遮罩层 -->
    <div class="absolute inset-0 bg-black/50 backdrop-blur-sm" @click="closeModal"></div>
    
    <!-- 登录框 -->
    <div class="relative w-full max-w-md p-6 rounded-2xl shadow-2xl"
         :style="{ backgroundColor: settingsStore.theme.colors.bgPrimary }">
      <!-- 关闭按钮 -->
      <button @click="closeModal" class="absolute top-4 right-4 p-1 rounded-lg transition-colors hover:bg-gray-100/10"
              :style="{ color: settingsStore.theme.colors.textMuted }">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <!-- 标题 -->
      <div class="text-center mb-6">
        <h2 class="text-2xl font-bold mb-2" :style="{ color: settingsStore.theme.colors.textPrimary }">
          {{ isRegister ? '注册账号' : '欢迎回来' }}
        </h2>
        <p class="text-sm" :style="{ color: settingsStore.theme.colors.textMuted }">
          {{ isRegister ? '创建一个新账号开始使用' : '登录以继续使用 LifeOS' }}
        </p>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleSubmit" class="space-y-4">
        <!-- 用户名 -->
        <div>
          <label class="block text-sm font-medium mb-1" :style="{ color: settingsStore.theme.colors.textSecondary }">
            用户名
          </label>
          <input v-model="form.username" type="text" required
                 class="w-full px-4 py-2 rounded-lg border transition-all duration-200 focus:outline-none focus:ring-2"
                 :style="{ 
                   backgroundColor: settingsStore.theme.colors.bgSecondary,
                   borderColor: settingsStore.theme.colors.border,
                   color: settingsStore.theme.colors.textPrimary
                 }"
                 :class="`focus:ring-[${settingsStore.theme.colors.accentColor}]`"
                 placeholder="请输入用户名">
        </div>

        <!-- 邮箱（仅注册） -->
        <div v-if="isRegister">
          <label class="block text-sm font-medium mb-1" :style="{ color: settingsStore.theme.colors.textSecondary }">
            邮箱（可选）
          </label>
          <input v-model="form.email" type="email"
                 class="w-full px-4 py-2 rounded-lg border transition-all duration-200 focus:outline-none focus:ring-2"
                 :style="{ 
                   backgroundColor: settingsStore.theme.colors.bgSecondary,
                   borderColor: settingsStore.theme.colors.border,
                   color: settingsStore.theme.colors.textPrimary
                 }"
                 placeholder="请输入邮箱">
        </div>

        <!-- 密码 -->
        <div>
          <label class="block text-sm font-medium mb-1" :style="{ color: settingsStore.theme.colors.textSecondary }">
            密码
          </label>
          <input v-model="form.password" type="password" required
                 class="w-full px-4 py-2 rounded-lg border transition-all duration-200 focus:outline-none focus:ring-2"
                 :style="{ 
                   backgroundColor: settingsStore.theme.colors.bgSecondary,
                   borderColor: settingsStore.theme.colors.border,
                   color: settingsStore.theme.colors.textPrimary
                 }"
                 placeholder="请输入密码">
        </div>

        <!-- 确认密码（仅注册） -->
        <div v-if="isRegister">
          <label class="block text-sm font-medium mb-1" :style="{ color: settingsStore.theme.colors.textSecondary }">
            确认密码
          </label>
          <input v-model="form.confirmPassword" type="password" required
                 class="w-full px-4 py-2 rounded-lg border transition-all duration-200 focus:outline-none focus:ring-2"
                 :style="{ 
                   backgroundColor: settingsStore.theme.colors.bgSecondary,
                   borderColor: settingsStore.theme.colors.border,
                   color: settingsStore.theme.colors.textPrimary
                 }"
                 placeholder="请再次输入密码">
        </div>

        <!-- 错误提示 -->
        <div v-if="error" class="p-3 rounded-lg text-sm"
             :style="{ backgroundColor: '#fef2f2', color: '#dc2626' }">
          {{ error }}
        </div>

        <!-- 提交按钮 -->
        <button type="submit" :disabled="loading"
                class="w-full py-2.5 rounded-lg font-medium transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                :style="{ 
                  backgroundColor: settingsStore.theme.colors.accentColor,
                  color: '#ffffff'
                }">
          <span v-if="loading">{{ isRegister ? '注册中...' : '登录中...' }}</span>
          <span v-else>{{ isRegister ? '注册' : '登录' }}</span>
        </button>
      </form>

      <!-- 切换登录/注册 -->
      <div class="mt-4 text-center">
        <button @click="toggleMode" class="text-sm transition-colors hover:underline"
                :style="{ color: settingsStore.theme.colors.accentColor }">
          {{ isRegister ? '已有账号？去登录' : '还没有账号？去注册' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '../store/useUserStore.js'
import { useSettingsStore } from '../store/useSettingsStore.js'
import { api } from '../api/http.js'

const userStore = useUserStore()
const settingsStore = useSettingsStore()

const isRegister = ref(false)
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const closeModal = () => {
  userStore.closeLoginModal()
  resetForm()
}

const resetForm = () => {
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.email = ''
  error.value = ''
  isRegister.value = false
}

const toggleMode = () => {
  isRegister.value = !isRegister.value
  error.value = ''
}

const handleSubmit = async () => {
  error.value = ''
  loading.value = true

  try {
    if (isRegister.value) {
      // 注册验证
      if (form.password !== form.confirmPassword) {
        error.value = '两次输入的密码不一致'
        return
      }
      if (form.password.length < 6) {
        error.value = '密码长度至少6位'
        return
      }

      const response = await api.register(form.username, form.password, form.email)
      if (response.success) {
        // 注册成功，自动登录
        await login()
      } else {
        error.value = response.message || '注册失败'
      }
    } else {
      // 直接登录
      await login()
    }
  } catch (err) {
    error.value = err.message || (isRegister.value ? '注册失败' : '登录失败')
  } finally {
    loading.value = false
  }
}

const login = async () => {
  const response = await api.login(form.username, form.password)
  if (response.success && response.data) {
    userStore.setUser({
      token: response.data.token,
      username: response.data.username,
      userId: response.data.userId
    })
    closeModal()
    // 刷新页面以获取当前用户的数据
    window.location.reload()
  } else {
    error.value = response.message || '登录失败'
  }
}
</script>
