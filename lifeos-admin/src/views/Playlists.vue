<template>
  <div class="playlists-container">
    <el-card class="playlists-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon :size="24" color="#409EFF"><Headset /></el-icon>
            <span class="title">音乐歌单管理</span>
          </div>
          <el-button type="primary" @click="handleAdd" :icon="Plus">
            添加歌单
          </el-button>
        </div>
      </template>

      <!-- 说明提示 -->
      <el-alert
        title="使用说明"
        type="info"
        :closable="false"
        class="info-alert"
      >
        <template #default>
          <div class="info-content">
            <p>1. 从网易云音乐网页版打开歌单，复制URL中的ID（如：https://music.163.com/#/playlist?id=<b>60198</b>）</p>
            <p>2. 设置情绪范围，系统会根据用户当前情绪自动匹配合适的歌单</p>
            <p>3. 只能设置一个默认歌单，当情绪不匹配任何范围时使用</p>
          </div>
        </template>
      </el-alert>

      <!-- 歌单列表 -->
      <el-table :data="playlists" v-loading="loading" stripe class="playlists-table">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="歌单名称" min-width="150">
          <template #default="{ row }">
            <div class="playlist-name">
              <el-icon><Headset /></el-icon>
              <span>{{ row.name }}</span>
              <el-tag v-if="row.isDefault" type="warning" size="small" class="default-tag">默认</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="网易云ID" prop="neteasePlaylistId" width="120" />
        <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
        <el-table-column label="情绪范围" width="150">
          <template #default="{ row }">
            <div class="emotion-range">
              <el-tag :type="getEmotionType(row.emotionMin)" size="small">{{ row.emotionMin }}</el-tag>
              <span class="range-separator">~</span>
              <el-tag :type="getEmotionType(row.emotionMax)" size="small">{{ row.emotionMax }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button type="primary" size="small" @click="handleEdit(row)" :icon="Edit">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)" :icon="Delete">
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty v-if="!loading && playlists.length === 0" description="暂无歌单数据">
        <el-button type="primary" @click="handleAdd">添加第一个歌单</el-button>
      </el-empty>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑歌单' : '添加歌单'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="playlist-form"
      >
        <el-form-item label="歌单名称" prop="name">
          <el-input v-model="form.name" placeholder="如：轻松愉快" />
        </el-form-item>

        <el-form-item label="网易云ID" prop="neteasePlaylistId">
          <el-input v-model="form.neteasePlaylistId" placeholder="如：60198">
            <template #append>
              <el-link href="https://music.163.com" target="_blank" :icon="Link">打开网易云</el-link>
            </template>
          </el-input>
          <div class="form-tip">从网易云音乐歌单URL中复制ID</div>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="如：适合心情好的时候"
          />
        </el-form-item>

        <el-form-item label="情绪范围" required>
          <div class="emotion-slider-container">
            <div class="slider-labels">
              <span>难过(-10)</span>
              <span>平静(0)</span>
              <span>开心(10)</span>
            </div>
            <el-slider
              v-model="emotionRange"
              range
              :min="-10"
              :max="10"
              :marks="{ '-10': '-10', '-5': '-5', '0': '0', '5': '5', '10': '10' }"
            />
            <div class="emotion-values">
              <el-tag :type="getEmotionType(form.emotionMin)">最小: {{ form.emotionMin }}</el-tag>
              <el-tag :type="getEmotionType(form.emotionMax)">最大: {{ form.emotionMax }}</el-tag>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" active-text="是" inactive-text="否" />
          <div class="form-tip">
            当情绪不匹配任何范围时使用默认歌单
            <el-tag v-if="form.isDefault" type="warning" size="small" style="margin-left: 8px;">
              设为默认将取消其他歌单的默认状态
            </el-tag>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Headset,
  Link
} from '@element-plus/icons-vue'
import { musicApi } from '../api'

// 数据
const playlists = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 表单
const form = reactive({
  id: null,
  name: '',
  neteasePlaylistId: '',
  description: '',
  emotionMin: -10,
  emotionMax: 10,
  isDefault: false
})

// 情绪范围滑块
const emotionRange = computed({
  get: () => [form.emotionMin, form.emotionMax],
  set: (val) => {
    form.emotionMin = val[0]
    form.emotionMax = val[1]
  }
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入歌单名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  neteasePlaylistId: [
    { required: true, message: '请输入网易云歌单ID', trigger: 'blur' }
  ],
  description: [
    { max: 255, message: '描述不能超过255个字符', trigger: 'blur' }
  ]
}

// 获取情绪标签类型
const getEmotionType = (score) => {
  if (score >= 5) return 'success'
  if (score >= 0) return 'info'
  if (score >= -5) return 'warning'
  return 'danger'
}

// 加载歌单列表
const loadPlaylists = async () => {
  loading.value = true
  try {
    const res = await musicApi.getPlaylists()
    if (res.success) {
      playlists.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载歌单失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 添加歌单
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑歌单
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除歌单
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除歌单 "${row.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await musicApi.deletePlaylist(row.id)
    if (res.success) {
      ElMessage.success('删除成功')
      loadPlaylists()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 验证情绪范围
  if (form.emotionMin > form.emotionMax) {
    ElMessage.error('最小情绪值不能大于最大情绪值')
    return
  }

  submitting.value = true
  try {
    const api = isEdit.value
      ? () => musicApi.updatePlaylist(form.id, form)
      : () => musicApi.createPlaylist(form)

    const res = await api()
    if (res.success) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadPlaylists()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.name = ''
  form.neteasePlaylistId = ''
  form.description = ''
  form.emotionMin = -10
  form.emotionMax = 10
  form.isDefault = false
  formRef.value?.resetFields()
}

onMounted(() => {
  loadPlaylists()
})
</script>

<style scoped>
.playlists-container {
  padding: 20px;
}

.playlists-card {
  min-height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title {
  font-size: 18px;
  font-weight: 600;
}

.info-alert {
  margin-bottom: 20px;
}

.info-content {
  font-size: 13px;
  line-height: 1.8;
}

.info-content p {
  margin: 5px 0;
}

.playlists-table {
  margin-top: 10px;
}

.playlist-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.default-tag {
  margin-left: 5px;
}

.emotion-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-separator {
  color: #909399;
}

.playlist-form {
  padding: 20px 0;
}

.emotion-slider-container {
  padding: 10px 0;
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}

.emotion-values {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
