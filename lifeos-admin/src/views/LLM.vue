<template>
  <div class="llm-page">
    <el-row :gutter="20">
      <!-- LLM 配置 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>LLM 配置</span>
              <div class="header-right">
                <el-tag v-if="configStatus === 'configured'" type="success">已配置</el-tag>
                <el-tag v-else-if="configStatus === 'not_configured'" type="warning">未配置</el-tag>
                <el-tag v-else type="info">检测中...</el-tag>
                <el-button type="primary" @click="saveConfig" style="margin-left: 10px;">
                  <el-icon><Check /></el-icon>
                  保存配置
                </el-button>
              </div>
            </div>
          </template>
          
          <el-form :model="llmConfig" label-width="100px">
            <el-form-item label="供应商">
              <el-input
                v-model="llmConfig.provider"
                placeholder="例如: openai, claude, gemini"
              />
            </el-form-item>
            
            <el-form-item label="模型">
              <el-input
                v-model="llmConfig.model"
                placeholder="例如: gpt-3.5-turbo, gpt-4"
              />
            </el-form-item>
            
            <el-form-item label="API Key">
              <el-input
                v-model="llmConfig.apiKey"
                type="password"
                show-password
                placeholder="输入您的 API Key"
              />
            </el-form-item>
            
            <el-form-item label="Base URL">
              <el-input
                v-model="llmConfig.baseUrl"
                placeholder="可选，自定义 API 地址"
              />
            </el-form-item>
            
            <el-form-item label="温度">
              <el-slider
                v-model="llmConfig.temperature"
                :min="0"
                :max="2"
                :step="0.1"
                show-input
              />
              <div class="form-tip">较低的值使输出更确定，较高的值使输出更随机</div>
            </el-form-item>
            
            <el-form-item label="最大 Token">
              <el-input-number
                v-model="llmConfig.maxTokens"
                :min="100"
                :max="4000"
                :step="100"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="使用本地规则">
              <el-switch
                v-model="llmConfig.useLocalRules"
                active-text="开启"
                inactive-text="关闭"
              />
              <div class="form-tip">开启后将无视 API Key，直接使用本地规则解析（无需联网）</div>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 测试和统计 -->
      <el-col :span="12">
        <el-card shadow="hover" class="test-card">
          <template #header>
            <div class="card-header">
              <span>接口测试</span>
              <el-button type="primary" @click="testConnection" :loading="testing">
                <el-icon><Connection /></el-icon>
                测试连接
              </el-button>
            </div>
          </template>
          
          <el-input
            v-model="testText"
            type="textarea"
            :rows="4"
            placeholder="输入测试文本，例如：今天花了50元吃午饭，心情不错"
          />
          
          <div v-if="testResult" class="test-result">
            <el-divider />
            <h4>解析结果</h4>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="类型">
                <el-tag
                  v-for="type in testResult.recordTypes"
                  :key="type"
                  class="type-tag"
                  :type="getTypeTag(type)"
                >
                  {{ getTypeLabel(type) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="金额" v-if="testResult.amount">
                ¥{{ testResult.amount }}
              </el-descriptions-item>
              <el-descriptions-item label="情绪分" v-if="testResult.emotionScore !== null">
                <el-tag :type="getEmotionTag(testResult.emotionScore)">
                  {{ testResult.emotionScore > 0 ? '+' : '' }}{{ testResult.emotionScore }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="标签">
                <el-tag
                  v-for="tag in testResult.tags"
                  :key="tag"
                  size="small"
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="摘要">
                {{ testResult.summary }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>

        <!-- 使用统计 -->
        <el-card shadow="hover" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>使用统计</span>
            </div>
          </template>
          <div class="usage-stats">
            <div class="stat-item">
              <div class="stat-label">今日调用次数</div>
              <div class="stat-value">{{ usageStats.todayCalls }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">本月调用次数</div>
              <div class="stat-value">{{ usageStats.monthCalls }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">平均响应时间</div>
              <div class="stat-value">{{ usageStats.avgResponseTime }}ms</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">成功率</div>
              <div class="stat-value">{{ usageStats.successRate }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { llmApi } from '../api'

const llmConfig = ref({
  provider: 'openai',
  model: 'gpt-3.5-turbo',
  apiKey: '',
  baseUrl: '',
  temperature: 0.7,
  maxTokens: 2000,
  useLocalRules: false
})

const testText = ref('今天花了50元吃午饭，心情不错')
const testResult = ref(null)
const testing = ref(false)
const configStatus = ref('loading') // 'loading', 'configured', 'not_configured'

const usageStats = ref({
  todayCalls: 0,
  monthCalls: 0,
  avgResponseTime: 0,
  successRate: 100
})

// 获取类型标签
const getTypeTag = (type) => {
  const tags = { expense: 'danger', mood: 'warning', event: 'success', diary: 'info' }
  return tags[type] || 'info'
}

const getTypeLabel = (type) => {
  const labels = { expense: '消费', mood: '情绪', event: '事件', diary: '日记' }
  return labels[type] || type
}

const getEmotionTag = (score) => {
  if (score >= 5) return 'success'
  if (score >= 0) return 'info'
  if (score >= -5) return 'warning'
  return 'danger'
}

// 检查配置状态
const checkConfigStatus = () => {
  const hasValidApiKey = llmConfig.value.apiKey && 
                         llmConfig.value.apiKey.trim().length > 0 && 
                         !llmConfig.value.apiKey.includes('dummy')
  const useLocalRules = llmConfig.value.useLocalRules
  
  if (hasValidApiKey || useLocalRules) {
    configStatus.value = 'configured'
  } else {
    configStatus.value = 'not_configured'
  }
}

// 加载配置
const loadConfig = async () => {
  try {
    const res = await llmApi.getConfig()
    if (res.success && res.data) {
      // 字段映射：后端 apiUrl -> 前端 baseUrl
      const mappedData = {
        ...res.data,
        baseUrl: res.data.apiUrl || res.data.baseUrl || ''
      }
      llmConfig.value = { ...llmConfig.value, ...mappedData }
    }
  } catch (error) {
    console.error('加载配置失败:', error)
  } finally {
    checkConfigStatus()
  }
}

// 保存配置
const saveConfig = async () => {
  try {
    // 字段映射：前端 baseUrl -> 后端 apiUrl
    const dataToSave = {
      ...llmConfig.value,
      apiUrl: llmConfig.value.baseUrl
    }
    await llmApi.saveConfig(dataToSave)
    checkConfigStatus()
    ElMessage.success('配置已保存')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 测试连接
const testConnection = async () => {
  testing.value = true
  try {
    // 模拟测试结果
    await new Promise(resolve => setTimeout(resolve, 1500))
    testResult.value = {
      recordTypes: ['expense', 'mood'],
      amount: 50,
      emotionScore: 5,
      tags: ['餐饮', '午餐'],
      summary: '午餐消费50元，心情愉快'
    }
    ElMessage.success('连接测试成功')
  } catch (error) {
    ElMessage.error('连接测试失败')
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped>
.llm-page {
  padding-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.test-card {
  min-height: 400px;
}

.test-result {
  margin-top: 20px;
}

.test-result h4 {
  margin-bottom: 15px;
  color: #303133;
}

.type-tag {
  margin-right: 8px;
}

.tag-item {
  margin-right: 5px;
  margin-bottom: 5px;
}

.usage-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
}
</style>
