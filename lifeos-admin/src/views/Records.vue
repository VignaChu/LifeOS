<template>
  <div class="records-page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">记录管理</span>
            <el-input
              v-model="searchQuery"
              placeholder="搜索记录内容..."
              clearable
              style="width: 300px; margin-left: 20px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="filteredRecords"
        style="width: 100%"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="originalText" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="recordType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.recordType)" effect="dark">
              {{ getTypeLabel(row.recordType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span v-if="row.amount" class="amount">
              ¥{{ row.amount.toFixed(2) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="emotionScore" label="情绪分" width="100">
          <template #default="{ row }">
            <el-tag
              v-if="row.emotionScore !== null"
              :type="getEmotionTag(row.emotionScore)"
              size="small"
            >
              {{ row.emotionScore > 0 ? '+' : '' }}{{ row.emotionScore }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="标签" width="150">
          <template #default="{ row }">
            <el-tag
              v-for="tag in parseTags(row.tags)"
              :key="tag"
              size="small"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordTime" label="记录时间" width="180" sortable />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑记录"
      width="600px"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="内容">
          <el-input
            v-model="editForm.originalText"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="editForm.recordType" style="width: 100%">
            <el-option label="消费" value="expense" />
            <el-option label="情绪" value="mood" />
            <el-option label="事件" value="event" />
            <el-option label="日记" value="diary" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number
            v-model="editForm.amount"
            :precision="2"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="情绪分">
          <el-slider
            v-model="editForm.emotionScore"
            :min="-10"
            :max="10"
            :step="1"
            show-stops
            show-input
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { recordApi } from '../api'

const loading = ref(false)
const records = ref([])
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedRecords = ref([])

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  originalText: '',
  recordType: '',
  amount: null,
  emotionScore: 0
})

// 过滤后的记录
const filteredRecords = computed(() => {
  if (!searchQuery.value) return records.value
  const query = searchQuery.value.toLowerCase()
  return records.value.filter(r =>
    r.originalText?.toLowerCase().includes(query) ||
    r.tags?.toLowerCase().includes(query)
  )
})

// 获取类型标签样式
const getTypeTag = (type) => {
  const tags = { expense: 'danger', mood: 'warning', event: 'success', diary: 'info' }
  return tags[type] || 'info'
}

const getTypeLabel = (type) => {
  const labels = { expense: '消费', mood: '情绪', event: '事件', diary: '日记' }
  return labels[type] || type
}

// 获取情绪标签样式
const getEmotionTag = (score) => {
  if (score >= 5) return 'success'
  if (score >= 0) return 'info'
  if (score >= -5) return 'warning'
  return 'danger'
}

// 解析标签
const parseTags = (tags) => {
  if (!tags) return []
  try {
    return JSON.parse(tags)
  } catch {
    return tags.split(',').map(t => t.trim())
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await recordApi.getAll()
    if (res.success) {
      records.value = res.data
      total.value = res.data.length
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = () => {
  loadData()
  ElMessage.success('数据已刷新')
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRecords.value = selection
}

// 编辑
const handleEdit = (row) => {
  editForm.value = { ...row }
  editDialogVisible.value = true
}

// 保存编辑
const saveEdit = async () => {
  try {
    await recordApi.update(editForm.value.id, editForm.value)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      type: 'warning'
    })
    await recordApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.records-page {
  padding-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.amount {
  color: #f56c6c;
  font-weight: 500;
}

.tag-item {
  margin-right: 5px;
  margin-bottom: 5px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
