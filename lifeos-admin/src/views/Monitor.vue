<template>
  <div class="monitor-page">
    <!-- 系统状态卡片 -->
    <el-row :gutter="20" class="status-cards">
      <el-col :span="6" v-for="status in systemStatus" :key="status.name">
        <el-card shadow="hover" :class="['status-card', status.status]">
          <div class="status-icon">
            <el-icon :size="40" :color="status.color">
              <component :is="status.icon" />
            </el-icon>
          </div>
          <div class="status-info">
            <div class="status-name">{{ status.name }}</div>
            <div class="status-value" :style="{ color: status.color }">
              {{ status.value }}
            </div>
            <div class="status-label">{{ status.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时监控图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>CPU 使用率</span>
              <el-tag type="success">实时</el-tag>
            </div>
          </template>
          <div ref="cpuChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>内存使用</span>
              <el-tag type="success">实时</el-tag>
            </div>
          </template>
          <div ref="memoryChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志和指标 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>系统日志</span>
              <el-button type="primary" link @click="refreshLogs">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="log-container">
            <div
              v-for="(log, index) in logs"
              :key="index"
              class="log-item"
              :class="log.level"
            >
              <span class="log-time">{{ log.time }}</span>
              <el-tag :type="getLogType(log.level)" size="small" class="log-level">
                {{ log.level }}
              </el-tag>
              <span class="log-message">{{ log.message }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>性能指标</span>
            </div>
          </template>
          <div class="metrics-list">
            <div v-for="metric in metrics" :key="metric.name" class="metric-item">
              <div class="metric-header">
                <span class="metric-name">{{ metric.name }}</span>
                <span class="metric-value">{{ metric.value }}</span>
              </div>
              <el-progress
                :percentage="metric.percentage"
                :color="getProgressColor(metric.percentage)"
                :stroke-width="8"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Spring Boot Admin 链接 -->
    <el-row style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>Spring Boot Admin</span>
              <el-button type="primary" @click="openAdminPanel">
                打开监控面板
              </el-button>
            </div>
          </template>
          <p class="admin-desc">
            Spring Boot Admin 提供更详细的应用监控功能，包括：
          </p>
          <el-row :gutter="20" class="admin-features">
            <el-col :span="6" v-for="feature in adminFeatures" :key="feature">
              <div class="feature-item">
                <el-icon color="#409EFF"><Check /></el-icon>
                <span>{{ feature }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { actuatorApi } from '../api'

const cpuChart = ref(null)
const memoryChart = ref(null)
let cpuInstance = null
let memoryInstance = null

// 系统状态
const systemStatus = ref([
  { name: '系统状态', value: '运行中', label: '正常', status: 'online', icon: 'CircleCheck', color: '#67c23a' },
  { name: 'API 响应', value: '45ms', label: '平均', status: 'normal', icon: 'Timer', color: '#409eff' },
  { name: '内存使用', value: '68%', label: '4.2GB / 8GB', status: 'warning', icon: 'Cpu', color: '#e6a23c' },
  { name: '磁盘空间', value: '72%', label: '360GB / 500GB', status: 'normal', icon: 'Disc', color: '#409eff' }
])

// 日志
const logs = ref([
  { time: '10:23:45', level: 'INFO', message: 'Application started successfully' },
  { time: '10:24:12', level: 'INFO', message: 'Health check passed' },
  { time: '10:25:33', level: 'WARN', message: 'High memory usage detected: 75%' },
  { time: '10:26:01', level: 'INFO', message: 'Database connection pool: 5 active, 3 idle' },
  { time: '10:27:15', level: 'ERROR', message: 'Failed to connect to Redis: Connection refused' },
  { time: '10:28:22', level: 'INFO', message: 'Auto-recovered Redis connection' }
])

// 性能指标
const metrics = ref([
  { name: 'JVM 堆内存', value: '512MB / 2GB', percentage: 25 },
  { name: '非堆内存', value: '128MB / 256MB', percentage: 50 },
  { name: '线程使用率', value: '15 / 50', percentage: 30 },
  { name: 'GC 暂停时间', value: '12ms', percentage: 5 },
  { name: '连接池使用率', value: '8 / 20', percentage: 40 }
])

// Admin 功能
const adminFeatures = [
  '应用健康检查',
  '实时指标监控',
  '日志级别管理',
  '环境属性查看',
  '计划任务管理',
  'HTTP 跟踪',
  '线程 Dump',
  '堆 Dump 分析'
]

// 获取日志类型
const getLogType = (level) => {
  const types = { INFO: 'info', WARN: 'warning', ERROR: 'danger', DEBUG: '' }
  return types[level] || 'info'
}

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

// 刷新日志
const refreshLogs = async () => {
  try {
    const res = await actuatorApi.getLoggers()
    console.log('Loggers:', res)
  } catch (error) {
    console.error('Failed to fetch loggers:', error)
  }
}

// 打开 Admin 面板
const openAdminPanel = () => {
  window.open('http://localhost:8080/actuator', '_blank')
}

// 初始化图表
const initCharts = () => {
  // CPU 图表
  cpuInstance = echarts.init(cpuChart.value)
  const cpuData = Array(20).fill(0).map(() => Math.floor(Math.random() * 30) + 20)
  cpuInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Array(20).fill('').map((_, i) => i), show: false },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      data: cpuData,
      type: 'line',
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ]
        }
      },
      lineStyle: { color: '#409eff' },
      itemStyle: { color: '#409eff' }
    }]
  })

  // 内存图表
  memoryInstance = echarts.init(memoryChart.value)
  const memoryData = Array(20).fill(0).map(() => Math.floor(Math.random() * 20) + 60)
  memoryInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: Array(20).fill('').map((_, i) => i), show: false },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{
      data: memoryData,
      type: 'line',
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ]
        }
      },
      lineStyle: { color: '#67c23a' },
      itemStyle: { color: '#67c23a' }
    }]
  })

  // 模拟实时更新
  setInterval(() => {
    cpuData.shift()
    cpuData.push(Math.floor(Math.random() * 30) + 20)
    cpuInstance.setOption({ series: [{ data: cpuData }] })

    memoryData.shift()
    memoryData.push(Math.floor(Math.random() * 20) + 60)
    memoryInstance.setOption({ series: [{ data: memoryData }] })
  }, 2000)
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', () => {
    cpuInstance?.resize()
    memoryInstance?.resize()
  })
})

onUnmounted(() => {
  cpuInstance?.dispose()
  memoryInstance?.dispose()
})
</script>

<style scoped>
.monitor-page {
  padding-bottom: 20px;
}

.status-cards {
  margin-bottom: 20px;
}

.status-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.status-card.online {
  border-left: 4px solid #67c23a;
}

.status-card.warning {
  border-left: 4px solid #e6a23c;
}

.status-card.error {
  border-left: 4px solid #f56c6c;
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.status-info {
  flex: 1;
}

.status-name {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.status-value {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.status-label {
  font-size: 12px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 250px;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
  font-family: 'Courier New', monospace;
  font-size: 13px;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #909399;
  margin-right: 12px;
  min-width: 80px;
}

.log-level {
  margin-right: 12px;
  min-width: 60px;
  text-align: center;
}

.log-message {
  color: #303133;
}

.metrics-list {
  padding: 10px 0;
}

.metric-item {
  margin-bottom: 20px;
}

.metric-item:last-child {
  margin-bottom: 0;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.metric-name {
  color: #606266;
}

.metric-value {
  color: #303133;
  font-weight: 500;
}

.admin-desc {
  color: #606266;
  margin-bottom: 15px;
}

.admin-features {
  margin-top: 15px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  color: #606266;
}
</style>
