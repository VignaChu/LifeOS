<template>
  <div class="database-page">
    <el-row :gutter="20">
      <!-- 数据库状态卡片 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>数据库状态</span>
              <el-tag :type="dbStatus.type">{{ dbStatus.text }}</el-tag>
            </div>
          </template>
          <div class="db-info">
            <div class="info-item">
              <span class="label">数据库类型:</span>
              <span class="value">{{ dbInfo.databaseProductName || 'MySQL' }}</span>
            </div>
            <div class="info-item">
              <span class="label">版本:</span>
              <span class="value">{{ dbInfo.databaseProductVersion || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">表数量:</span>
              <span class="value">{{ dbInfo.tableCount || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="label">驱动:</span>
              <span class="value">{{ dbInfo.driverName || 'HikariCP' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 表统计 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>数据表列表</span>
              <el-button type="primary" link @click="refreshStats">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          <el-table :data="tableStats" style="width: 100%" v-loading="loading">
            <el-table-column prop="tableName" label="表名" />
            <el-table-column prop="comment" label="注释" show-overflow-tooltip />
            <el-table-column prop="rowCount" label="记录数" sortable width="120">
              <template #default="{ row }">
                <el-tag type="info">{{ row.rowCount?.toLocaleString() || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="dataSize" label="数据大小" sortable width="120">
              <template #default="{ row }">
                {{ formatSize(row.dataSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="indexSize" label="索引大小" sortable width="120">
              <template #default="{ row }">
                {{ formatSize(row.indexSize) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewTableStructure(row)">
                  结构
                </el-button>
                <el-button type="success" link @click="viewTableData(row)">
                  数据
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- SQL 查询工具 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>SQL 查询工具</span>
              <div>
                <el-button type="primary" @click="executeQuery" :loading="executing">
                  <el-icon><VideoPlay /></el-icon>
                  执行
                </el-button>
                <el-button @click="clearQuery">
                  <el-icon><Delete /></el-icon>
                  清空
                </el-button>
              </div>
            </div>
          </template>
          <el-input
            v-model="sqlQuery"
            type="textarea"
            :rows="6"
            placeholder="输入 SQL 查询语句 (仅支持 SELECT 查询)..."
            class="sql-editor"
          />
          
          <!-- 查询结果 -->
          <div v-if="queryResult.length > 0" class="query-result">
            <el-divider />
            <h4>查询结果 ({{ queryResult.length }} 行)</h4>
            <el-table :data="queryResult" style="width: 100%" max-height="400" border>
              <el-table-column
                v-for="key in Object.keys(queryResult[0])"
                :key="key"
                :prop="key"
                :label="key"
                show-overflow-tooltip
              />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 表结构对话框 -->
    <el-dialog
      v-model="structureDialogVisible"
      :title="currentTable + ' - 表结构'"
      width="70%"
    >
      <el-table :data="tableStructure" style="width: 100%" border>
        <el-table-column prop="columnName" label="字段名" width="150" />
        <el-table-column prop="dataType" label="数据类型" width="120" />
        <el-table-column prop="nullable" label="可空" width="80">
          <template #default="{ row }">
            <el-tag :type="row.nullable === 'YES' ? 'success' : 'danger'" size="small">
              {{ row.nullable === 'YES' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="defaultValue" label="默认值" width="120" />
        <el-table-column prop="extra" label="额外信息" width="150" />
        <el-table-column prop="comment" label="注释" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <!-- 表数据对话框 -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="currentTable + ' - 数据管理'"
      width="90%"
      top="5vh"
    >
      <!-- 工具栏 -->
      <div class="data-toolbar">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          新增记录
        </el-button>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalRecords"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>

      <!-- 数据表格 -->
      <el-table 
        :data="tableData" 
        style="width: 100%; margin-top: 15px;" 
        max-height="500" 
        border
        v-loading="dataLoading"
      >
        <el-table-column type="index" width="50" />
        <el-table-column
          v-for="col in tableColumns"
          :key="col"
          :prop="col"
          :label="col"
          show-overflow-tooltip
          min-width="120"
          sortable
        />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="editRow(row)">
              编辑
            </el-button>
            <el-button type="danger" link @click="deleteRow(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 新增/编辑数据对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="isEditing ? '编辑记录' : '新增记录'"
      width="50%"
    >
      <el-form :model="editForm" label-width="120px">
        <el-form-item 
          v-for="col in editableColumns" 
          :key="col.columnName"
          :label="col.columnName"
        >
          <el-input 
            v-if="isTextType(col.dataType)"
            v-model="editForm[col.columnName]" 
            type="textarea"
            :rows="3"
          />
          <el-input-number 
            v-else-if="isNumberType(col.dataType)"
            v-model="editForm[col.columnName]"
            :precision="col.dataType.includes('decimal') ? 2 : 0"
          />
          <el-date-picker
            v-else-if="isDateTimeType(col.dataType)"
            v-model="editForm[col.columnName]"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
          <el-switch
            v-else-if="isBooleanType(col.dataType)"
            v-model="editForm[col.columnName]"
          />
          <el-input v-else v-model="editForm[col.columnName]" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveData" :loading="saving">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { databaseApi } from '../api'

const loading = ref(false)
const dataLoading = ref(false)
const saving = ref(false)
const executing = ref(false)

const dbStatus = ref({ type: 'success', text: '正常' })
const dbInfo = ref({})
const tableStats = ref([])

const sqlQuery = ref('SELECT * FROM life_records LIMIT 10')
const queryResult = ref([])

const structureDialogVisible = ref(false)
const dataDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentTable = ref('')
const tableStructure = ref([])
const tableData = ref([])
const tableColumns = ref([])

// 分页
const currentPage = ref(1)
const pageSize = ref(20)
const totalRecords = ref(0)

// 编辑表单
const isEditing = ref(false)
const editForm = ref({})
const currentRowId = ref(null)

// 计算可编辑的列（排除自增ID）
const editableColumns = computed(() => {
  return tableStructure.value.filter(col => {
    // 排除自增主键
    if (col.extra && col.extra.includes('auto_increment')) {
      return false
    }
    return true
  })
})

// 判断数据类型
const isTextType = (type) => {
  const textTypes = ['text', 'varchar', 'char', 'longtext', 'mediumtext']
  return textTypes.some(t => type.toLowerCase().includes(t))
}

const isNumberType = (type) => {
  const numberTypes = ['int', 'decimal', 'float', 'double', 'bigint', 'smallint']
  return numberTypes.some(t => type.toLowerCase().includes(t))
}

const isDateTimeType = (type) => {
  const dateTypes = ['datetime', 'timestamp', 'date', 'time']
  return dateTypes.some(t => type.toLowerCase().includes(t))
}

const isBooleanType = (type) => {
  return type.toLowerCase().includes('tinyint') || type.toLowerCase().includes('bit')
}

// 格式化大小
const formatSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 刷新统计
const refreshStats = async () => {
  loading.value = true
  try {
    // 获取数据库状态
    const statusRes = await databaseApi.getStatus()
    if (statusRes.success) {
      dbInfo.value = statusRes.data
    }
    
    // 获取表列表
    const tablesRes = await databaseApi.getTables()
    if (tablesRes.success) {
      tableStats.value = tablesRes.data
    }
    
    ElMessage.success('数据已刷新')
  } catch (error) {
    console.error('刷新失败:', error)
    ElMessage.error('刷新失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 查看表结构
const viewTableStructure = async (row) => {
  currentTable.value = row.tableName
  structureDialogVisible.value = true
  
  try {
    const res = await databaseApi.getTableStructure(row.tableName)
    if (res.success) {
      tableStructure.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载表结构失败: ' + error.message)
  }
}

// 查看表数据
const viewTableData = async (row) => {
  currentTable.value = row.tableName
  dataDialogVisible.value = true
  currentPage.value = 1
  await loadTableData()
}

// 加载表数据
const loadTableData = async () => {
  dataLoading.value = true
  try {
    const res = await databaseApi.getTableData(
      currentTable.value, 
      currentPage.value, 
      pageSize.value
    )
    if (res.success) {
      tableData.value = res.data.data
      totalRecords.value = res.data.total
      if (tableData.value.length > 0) {
        tableColumns.value = Object.keys(tableData.value[0])
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message)
  } finally {
    dataLoading.value = false
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadTableData()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadTableData()
}

// 打开新增对话框
const openAddDialog = async () => {
  // 先获取表结构
  try {
    const res = await databaseApi.getTableStructure(currentTable.value)
    if (res.success) {
      tableStructure.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取表结构失败')
    return
  }
  
  isEditing.value = false
  editForm.value = {}
  // 初始化默认值
  editableColumns.value.forEach(col => {
    if (col.defaultValue !== null) {
      editForm.value[col.columnName] = col.defaultValue
    } else if (isNumberType(col.dataType)) {
      editForm.value[col.columnName] = 0
    } else if (isBooleanType(col.dataType)) {
      editForm.value[col.columnName] = false
    } else {
      editForm.value[col.columnName] = ''
    }
  })
  editDialogVisible.value = true
}

// 编辑行
const editRow = async (row) => {
  // 先获取表结构
  try {
    const res = await databaseApi.getTableStructure(currentTable.value)
    if (res.success) {
      tableStructure.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取表结构失败')
    return
  }
  
  isEditing.value = true
  currentRowId.value = row.id
  editForm.value = { ...row }
  editDialogVisible.value = true
}

// 保存数据
const saveData = async () => {
  saving.value = true
  try {
    let res
    if (isEditing.value) {
      res = await databaseApi.updateData(currentTable.value, currentRowId.value, editForm.value)
    } else {
      res = await databaseApi.insertData(currentTable.value, editForm.value)
    }
    
    if (res.success) {
      ElMessage.success(isEditing.value ? '更新成功' : '新增成功')
      editDialogVisible.value = false
      loadTableData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}

// 删除行
const deleteRow = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await databaseApi.deleteData(currentTable.value, row.id)
    if (res.success) {
      ElMessage.success('删除成功')
      loadTableData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

// 执行查询
const executeQuery = async () => {
  if (!sqlQuery.value.trim()) {
    ElMessage.warning('请输入 SQL 语句')
    return
  }
  
  executing.value = true
  try {
    const res = await databaseApi.executeQuery(sqlQuery.value)
    if (res.success) {
      queryResult.value = res.data
      ElMessage.success(`查询成功，返回 ${res.data.length} 条记录`)
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch (error) {
    ElMessage.error('查询失败: ' + error.message)
  } finally {
    executing.value = false
  }
}

// 清空查询
const clearQuery = () => {
  sqlQuery.value = ''
  queryResult.value = []
}

onMounted(() => {
  refreshStats()
})
</script>

<style scoped>
.database-page {
  padding-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.db-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: #606266;
  font-weight: 500;
}

.info-item .value {
  color: #303133;
  font-weight: 600;
}

.sql-editor {
  font-family: 'Consolas', 'Monaco', monospace;
}

.query-result {
  margin-top: 20px;
}

.query-result h4 {
  margin-bottom: 15px;
  color: #303133;
}

.data-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
</style>
