<template>
  <div class="api-page">
    <el-row :gutter="20">
      <!-- API 概览 -->
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>API 接口文档</span>
              <el-button type="primary" @click="testAllApis">
                <el-icon><VideoPlay /></el-icon>
                测试所有接口
              </el-button>
            </div>
          </template>
          
          <el-collapse v-model="activeNames">
            <el-collapse-item
              v-for="api in apiList"
              :key="api.name"
              :name="api.name"
            >
              <template #title>
                <div class="api-title">
                  <el-tag :type="getMethodType(api.method)" size="small">
                    {{ api.method }}
                  </el-tag>
                  <span class="api-path">{{ api.path }}</span>
                  <span class="api-name">{{ api.name }}</span>
                  <el-tag
                    v-if="api.status"
                    :type="api.status === 'ok' ? 'success' : 'danger'"
                    size="small"
                    class="status-tag"
                  >
                    {{ api.status === 'ok' ? '正常' : '异常' }}
                  </el-tag>
                </div>
              </template>
              
              <div class="api-detail">
                <p class="description">{{ api.description }}</p>
                
                <!-- 请求参数 -->
                <div v-if="api.params && api.params.length > 0" class="section">
                  <h4>请求参数</h4>
                  <el-table :data="api.params" size="small" border>
                    <el-table-column prop="name" label="参数名" width="150" />
                    <el-table-column prop="type" label="类型" width="100" />
                    <el-table-column prop="required" label="必填" width="80">
                      <template #default="{ row }">
                        <el-tag :type="row.required ? 'danger' : 'info'" size="small">
                          {{ row.required ? '是' : '否' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="description" label="说明" />
                  </el-table>
                </div>
                
                <!-- 响应示例 -->
                <div class="section">
                  <h4>响应示例</h4>
                  <pre class="code-block">{{ JSON.stringify(api.response, null, 2) }}</pre>
                </div>
                
                <!-- 测试按钮 -->
                <div class="section">
                  <el-button type="primary" size="small" @click="testApi(api)">
                    测试接口
                  </el-button>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </el-col>
    </el-row>

    <!-- API 调用统计 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>API 调用统计</span>
          </template>
          <div ref="apiStatsChart" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span>响应时间趋势</span>
          </template>
          <div ref="responseTimeChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 测试结果对话框 -->
    <el-dialog
      v-model="testDialogVisible"
      title="接口测试结果"
      width="600px"
    >
      <div v-if="testResult">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="状态码">
            <el-tag :type="testResult.status >= 200 && testResult.status < 300 ? 'success' : 'danger'">
              {{ testResult.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应时间">
            {{ testResult.duration }}ms
          </el-descriptions-item>
          <el-descriptions-item label="响应数据">
            <pre class="code-block">{{ JSON.stringify(testResult.data, null, 2) }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { recordApi, actuatorApi } from '../api'

const activeNames = ref(['health'])
const apiStatsChart = ref(null)
const responseTimeChart = ref(null)
let apiStatsInstance = null
let responseTimeInstance = null

const testDialogVisible = ref(false)
const testResult = ref(null)

// API 列表
const apiList = ref([
  {
    name: 'health',
    path: '/api/health',
    method: 'GET',
    description: '健康检查接口',
    response: { success: true, data: 'ok', message: null },
    status: null
  },
  {
    name: 'track',
    path: '/api/track',
    method: 'POST',
    description: '提交生活记录',
    params: [
      { name: 'text', type: 'string', required: true, description: '记录内容' }
    ],
    response: { success: true, data: { id: 1, content: '...' }, message: null },
    status: null
  },
  {
    name: 'records',
    path: '/api/records',
    method: 'GET',
    description: '获取所有记录',
    response: { success: true, data: [], message: null },
    status: null
  },
  {
    name: 'deleteRecord',
    path: '/api/records/{id}',
    method: 'DELETE',
    description: '删除记录',
    params: [
      { name: 'id', type: 'long', required: true, description: '记录ID' }
    ],
    response: { success: true, data: null, message: null },
    status: null
  },
  {
    name: 'updateRecord',
    path: '/api/records/{id}',
    method: 'PUT',
    description: '更新记录',
    params: [
      { name: 'id', type: 'long', required: true, description: '记录ID' },
      { name: 'record', type: 'object', required: true, description: '记录对象' }
    ],
    response: { success: true, data: null, message: null },
    status: null
  },
  {
    name: 'query',
    path: '/api/query',
    method: 'POST',
    description: '自然语言查询',
    params: [
      { name: 'text', type: 'string', required: true, description: '查询内容' }
    ],
    response: { success: true, data: '查询结果', message: null },
    status: null
  },
  {
    name: 'weeklyReport',
    path: '/api/report/weekly',
    method: 'GET',
    description: '生成周报',
    response: { success: true, data: '周报内容', message: null },
    status: null
  },
  {
    name: 'monthlyReport',
    path: '/api/report/monthly',
    method: 'GET',
    description: '生成月报',
    response: { success: true, data: '月报内容', message: null },
    status: null
  }
])

// 获取方法标签类型
const getMethodType = (method) => {
  const types = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return types[method] || 'info'
}

// 测试单个 API
const testApi = async (api) => {
  const startTime = Date.now()
  try {
    let res
    switch (api.name) {
      case 'health':
        res = await actuatorApi.getHealth()
        break
      case 'records':
        res = await recordApi.getAll()
        break
      default:
        // 其他接口暂不测试
        return
    }
    
    testResult.value = {
      status: 200,
      duration: Date.now() - startTime,
      data: res
    }
    api.status = 'ok'
  } catch (error) {
    testResult.value = {
      status: error.response?.status || 500,
      duration: Date.now() - startTime,
      data: { error: error.message }
    }
    api.status = 'error'
  }
  testDialogVisible.value = true
}

// 测试所有 API
const testAllApis = async () => {
  for (const api of apiList.value) {
    await testApi(api)
    await new Promise(resolve => setTimeout(resolve, 200))
  }
}

// 初始化图表
const initCharts = () => {
  // API 调用统计饼图
  apiStatsInstance = echarts.init(apiStatsChart.value)
  apiStatsInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: '50%',
      data: [
        { value: 35, name: 'GET 请求' },
        { value: 25, name: 'POST 请求' },
        { value: 20, name: 'PUT 请求' },
        { value: 10, name: 'DELETE 请求' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  })

  // 响应时间趋势图
  responseTimeInstance = echarts.init(responseTimeChart.value)
  responseTimeInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'] },
    yAxis: { type: 'value', name: 'ms' },
    series: [{
      data: [120, 132, 101, 134, 90, 230],
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
      itemStyle: { color: '#67c23a' }
    }]
  })
}

onMounted(() => {
  initCharts()
  window.addEventListener('resize', () => {
    apiStatsInstance?.resize()
    responseTimeInstance?.resize()
  })
})

onUnmounted(() => {
  apiStatsInstance?.dispose()
  responseTimeInstance?.dispose()
})
</script>

<style scoped>
.api-page {
  padding-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.api-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.api-path {
  font-family: 'Courier New', monospace;
  color: #409eff;
  font-weight: 500;
}

.api-name {
  color: #606266;
}

.status-tag {
  margin-left: auto;
}

.api-detail {
  padding: 10px 0;
}

.description {
  color: #606266;
  margin-bottom: 15px;
}

.section {
  margin-top: 15px;
}

.section h4 {
  margin-bottom: 10px;
  color: #303133;
}

.code-block {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
}

.chart-container {
  height: 300px;
}
</style>
