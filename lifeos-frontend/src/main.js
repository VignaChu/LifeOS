import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './app.vue'
import './assets/index.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.mount('#app')

console.log('🚀 LifeOS 前端应用已启动')
