import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../views/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '数据概览', icon: 'Odometer' }
      },
      {
        path: 'records',
        name: 'Records',
        component: () => import('../views/Records.vue'),
        meta: { title: '记录管理', icon: 'Document' }
      },
      {
        path: 'database',
        name: 'Database',
        component: () => import('../views/Database.vue'),
        meta: { title: '数据库管理', icon: 'Coin' }
      },
      {
        path: 'api',
        name: 'Api',
        component: () => import('../views/Api.vue'),
        meta: { title: 'API 接口', icon: 'Connection' }
      },
      {
        path: 'monitor',
        name: 'Monitor',
        component: () => import('../views/Monitor.vue'),
        meta: { title: '系统监控', icon: 'Monitor' }
      },
      {
        path: 'llm',
        name: 'LLM',
        component: () => import('../views/LLM.vue'),
        meta: { title: 'LLM 配置', icon: 'Cpu' }
      },
      {
        path: 'playlists',
        name: 'Playlists',
        component: () => import('../views/Playlists.vue'),
        meta: { title: '音乐歌单', icon: 'Headset' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 检查是否有token
  const token = localStorage.getItem('admin_token')
  
  if (!token) {
    // 没有token，跳转到登录页
    next('/login')
    return
  }

  // 有token就放行，具体验证由API拦截器处理
  next()
})

export default router
