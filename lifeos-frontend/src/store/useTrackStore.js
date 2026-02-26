import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '../api/http.js'

export const useTrackStore = defineStore('track', () => {
  const records = ref([])
  const isLoading = ref(false)
  const error = ref(null)
  const connectionStatus = ref('disconnected')
  const queryResult = ref('')

  const sortedRecords = computed(() => {
    return [...records.value].sort((a, b) => {
      return new Date(b.recordTime) - new Date(a.recordTime)
    })
  })

  const sendText = async (text, recordTime = null) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await api.track(text, recordTime)
      
      if (response.success && response.data) {
        records.value.unshift(response.data)
        return { success: true, data: response.data }
      } else {
        throw new Error(response.message || '保存失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('发送文本失败:', err)
      return { success: false, error: err.message }
    } finally {
      isLoading.value = false
    }
  }

  const sendQuery = async (text) => {
    isLoading.value = true
    error.value = null
    
    try {
      const response = await api.query(text)
      
      if (response.success && response.data) {
        queryResult.value = response.data
        return { success: true, data: response.data }
      } else {
        throw new Error(response.message || '查询失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('查询失败:', err)
      return { success: false, error: err.message }
    } finally {
      isLoading.value = false
    }
  }

  const fetchRecords = async () => {
    isLoading.value = true
    
    try {
      const response = await api.getRecords()
      
      if (response.success && response.data) {
        records.value = response.data
        return { success: true, data: response.data }
      } else {
        throw new Error(response.message || '获取失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('获取记录失败:', err)
      return { success: false, error: err.message }
    } finally {
      isLoading.value = false
    }
  }

  const deleteRecord = async (id) => {
    try {
      const response = await api.deleteRecord(id)
      
      if (response.success) {
        const index = records.value.findIndex(r => r.id === id)
        if (index !== -1) {
          records.value.splice(index, 1)
        }
        return { success: true }
      } else {
        throw new Error(response.message || '删除失败')
      }
    } catch (err) {
      error.value = err.message
      console.error('删除记录失败:', err)
      return { success: false, error: err.message }
    }
  }

  const checkConnection = async () => {
    try {
      const response = await api.healthCheck()
      connectionStatus.value = response.success ? 'connected' : 'disconnected'
      return connectionStatus.value
    } catch (err) {
      connectionStatus.value = 'disconnected'
      return connectionStatus.value
    }
  }

  return {
    records,
    isLoading,
    error,
    connectionStatus,
    queryResult,
    sortedRecords,
    sendText,
    sendQuery,
    fetchRecords,
    deleteRecord,
    checkConnection
  }
})
