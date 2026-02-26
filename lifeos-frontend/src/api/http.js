import ky from 'ky'
import { useUserStore } from '../store/useUserStore.js'

const http = ky.create({
  prefixUrl: 'http://localhost:8080/api',
  timeout: 30000,
  hooks: {
    beforeRequest: [
      (request) => {
        request.headers.set('Content-Type', 'application/json')
        request.headers.set('Accept', 'application/json')
        
        // 添加认证头
        const userStore = useUserStore()
        const authHeader = userStore.getAuthHeader()
        if (authHeader) {
          request.headers.set('Authorization', authHeader)
        }
      }
    ],
    beforeError: [
      (error) => {
        console.error('HTTP请求错误:', error.message)
        // 如果是401错误，清除登录状态
        if (error.response && error.response.status === 401) {
          const userStore = useUserStore()
          userStore.clearUser()
        }
        return error
      }
    ]
  }
})

export const api = {
  // User APIs
  login: (username, password) =>
    http.post('user/login', { json: { username, password } }).json(),

  register: (username, password, email = null) =>
    http.post('user/register', { json: { username, password, email } }).json(),

  getProfile: () =>
    http.get('user/profile').json(),

  // Track APIs
  track: (text, recordTime = null) =>
    http.post('track', { json: { text, recordTime } }).json(),

  query: (text) =>
    http.post('query', { json: { text } }).json(),

  getRecords: () =>
    http.get('records').json(),

  getRecordById: (id) =>
    http.get(`records/${id}`).json(),

  deleteRecord: (id) =>
    http.delete(`records/${id}`).json(),

  updateRecord: (id, record) =>
    http.put(`records/${id}`, { json: record }).json(),

  healthCheck: () =>
    http.get('health').json(),

  test: () =>
    http.get('test').json(),

  // LLM Config APIs
  getLlmConfig: () =>
    http.get('llm-config').json(),

  saveLlmConfig: (config) =>
    http.post('llm-config', { json: config }).json(),

  deleteLlmConfig: () =>
    http.delete('llm-config').json(),

  getWeeklyReport: () =>
    http.get('report/weekly').json(),

  getMonthlyReport: () =>
    http.get('report/monthly').json(),

  getCareMessage: () =>
    http.get('care').json()
}

export default http
