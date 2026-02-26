import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  // State
  const token = ref(localStorage.getItem('lifeos_token') || null)
  const username = ref(localStorage.getItem('lifeos_username') || null)
  const userId = ref(localStorage.getItem('lifeos_userId') || null)
  const isLoginModalOpen = ref(false)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => username.value || '未登录')

  // Actions
  const setUser = (userData) => {
    token.value = userData.token
    username.value = userData.username
    userId.value = userData.userId
    
    localStorage.setItem('lifeos_token', userData.token)
    localStorage.setItem('lifeos_username', userData.username)
    localStorage.setItem('lifeos_userId', userData.userId)
  }

  const clearUser = () => {
    token.value = null
    username.value = null
    userId.value = null
    
    localStorage.removeItem('lifeos_token')
    localStorage.removeItem('lifeos_username')
    localStorage.removeItem('lifeos_userId')
  }

  const openLoginModal = () => {
    isLoginModalOpen.value = true
  }

  const closeLoginModal = () => {
    isLoginModalOpen.value = false
  }

  const logout = () => {
    clearUser()
    window.location.reload()
  }

  // 获取认证头
  const getAuthHeader = () => {
    return token.value ? `Bearer ${token.value}` : null
  }

  return {
    token,
    username,
    userId,
    isLoginModalOpen,
    isLoggedIn,
    displayName,
    setUser,
    clearUser,
    openLoginModal,
    closeLoginModal,
    logout,
    getAuthHeader
  }
})
