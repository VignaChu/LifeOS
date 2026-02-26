<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="32" color="#409EFF"><Management /></el-icon>
        <span class="logo-text">LifeOS Admin</span>
      </div>
      
      <el-menu
        :default-active="$route.path"
        router
        class="sidebar-menu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item v-for="route in routes" :key="route.path" :index="route.path">
          <el-icon>
            <component :is="route.meta.icon" />
          </el-icon>
          <span>{{ route.meta.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <breadcrumb />
        </div>
        <div class="header-right">
          <el-tooltip content="系统状态">
            <el-icon :size="20" :class="['status-icon', systemStatus]">
              <CircleCheck v-if="systemStatus === 'online'" />
              <CircleClose v-else />
            </el-icon>
          </el-tooltip>
          <span class="status-text">{{ systemStatus === 'online' ? '运行中' : '离线' }}</span>
          
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              {{ username }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { actuatorApi, authApi } from '../api'

const route = useRoute()
const router = useRouter()
const systemStatus = ref('offline')
const username = ref(localStorage.getItem('admin_username') || 'Admin')

// 获取路由配置
const routes = computed(() => {
  return router.getRoutes()
    .find(r => r.path === '/')
    ?.children || []
})

// 检查系统状态
const checkSystemStatus = async () => {
  try {
    await actuatorApi.getHealth()
    systemStatus.value = 'online'
  } catch {
    systemStatus.value = 'offline'
  }
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      // 调用退出登录API
      await authApi.logout()
      
      // 清除本地存储
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_username')
      localStorage.removeItem('admin_role')
      
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('Logout error:', error)
      }
    }
  }
}

onMounted(() => {
  checkSystemStatus()
  setInterval(checkSystemStatus, 30000) // 每30秒检查一次
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  border-bottom: 1px solid #1f2d3d;
}

.logo-text {
  margin-left: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.sidebar-menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-icon {
  font-size: 20px;
}

.status-icon.online {
  color: #67c23a;
}

.status-icon.offline {
  color: #f56c6c;
}

.status-text {
  font-size: 14px;
  color: #606266;
}

.user-info {
  margin-left: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.user-info:hover {
  color: #409EFF;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
