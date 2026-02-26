import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器 - 添加token
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理401错误
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      // Token过期或无效，清除登录状态并跳转登录页
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
      localStorage.removeItem('admin_role')
      ElMessage.error('登录已过期，请重新登录')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api

// 记录相关 API
export const recordApi = {
  getAll: () => api.get('/admin/records'),
  delete: (id) => api.delete(`/records/${id}`),
  update: (id, data) => api.put(`/records/${id}`, data)
}

// LLM 配置 API
export const llmApi = {
  getConfig: () => api.get('/llm-config'),
  saveConfig: (data) => api.post('/llm-config', data)
}

// 统计 API
export const statsApi = {
  getStats: () => api.get('/stats'),
  getUsage: () => api.get('/stats/usage')
}

// Actuator 监控 API
export const actuatorApi = {
  getHealth: () => axios.get('/actuator/health'),
  getMetrics: () => axios.get('/actuator/metrics'),
  getInfo: () => axios.get('/actuator/info'),
  getLoggers: () => axios.get('/actuator/loggers')
}

// 数据库管理 API
export const databaseApi = {
  // 获取所有表
  getTables: () => api.get('/admin/database/tables'),
  // 获取表结构
  getTableStructure: (tableName) => api.get(`/admin/database/tables/${tableName}/structure`),
  // 获取表数据
  getTableData: (tableName, page = 1, size = 20, sortField = '', sortOrder = '') => 
    api.get(`/admin/database/tables/${tableName}/data`, {
      params: { page, size, sortField, sortOrder }
    }),
  // 插入数据
  insertData: (tableName, data) => api.post(`/admin/database/tables/${tableName}/data`, data),
  // 更新数据
  updateData: (tableName, id, data) => api.put(`/admin/database/tables/${tableName}/data/${id}`, data),
  // 删除数据
  deleteData: (tableName, id) => api.delete(`/admin/database/tables/${tableName}/data/${id}`),
  // 执行SQL查询
  executeQuery: (sql) => api.post('/admin/database/query', { sql }),
  // 获取数据库状态
  getStatus: () => api.get('/admin/database/status')
}

// 认证 API
export const authApi = {
  // 登录
  login: (data) => api.post('/admin/auth/login', data),
  // 验证token
  verify: () => api.get('/admin/auth/verify'),
  // 获取当前用户信息
  getCurrentUser: () => api.get('/admin/auth/me'),
  // 退出登录
  logout: () => api.post('/admin/auth/logout')
}

// 音乐歌单 API
export const musicApi = {
  // 获取所有歌单
  getPlaylists: () => api.get('/music/playlists'),
  // 根据情绪获取歌单
  getPlaylistByEmotion: (emotionScore) => api.get('/music/playlist/by-emotion', {
    params: { emotionScore }
  }),
  // 获取默认歌单
  getDefaultPlaylist: () => api.get('/music/playlist/default'),
  // 创建歌单
  createPlaylist: (data) => api.post('/music/playlists', data),
  // 更新歌单
  updatePlaylist: (id, data) => api.put(`/music/playlists/${id}`, data),
  // 删除歌单
  deletePlaylist: (id) => api.delete(`/music/playlists/${id}`)
}
