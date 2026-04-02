<template>
  <div class="activity-management">
    <!-- 第一个 div：查询功能 -->
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入活动名称或地点进行搜索" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="activityStatus" class="status-select">
        <option value="">请选择活动状态</option>
        <option value="0">未开始</option>
        <option value="1">进行中</option>
        <option value="2">已结束</option>
        <option value="3">已取消</option>
      </select>
      <button @click="handleSearch" class="search-btn">搜索</button>
      <button @click="resetSearch" class="reset-btn">重置</button>
    </div>

    <!-- 第二个 div：新增和批量删除按钮 -->
    <div class="action-section">
      <button @click="openAddModal" class="add-btn">新增</button>
      <button @click="batchDelete" :disabled="selectedIds.length === 0" class="delete-btn">批量删除</button>
    </div>

    <!-- 第三个 div：数据展示 -->
    <div class="data-section">
      <table class="data-table">
        <thead>
          <tr>
            <th width="50">
              <input 
                type="checkbox" 
                @change="selectAll" 
                :checked="isAllSelected"
              />
            </th>
            <th>活动名称</th>
            <th>类型名称</th>
            <th>地点</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>参与人数</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in activities" :key="item.id">
            <td>
              <input 
                type="checkbox" 
                :value="item.id"
                v-model="selectedIds"
              />
            </td>
            <td>{{ item.activityName }}</td>
            <td>{{ item.typeName || item.typeId }}</td>
            <td>{{ item.location }}</td>
            <td>{{ formatDate(item.startTime) }}</td>
            <td>{{ formatDate(item.endTime) }}</td>
            <td>{{ item.currentParticipants }}/{{ item.maxParticipants }}</td>
            <td>
              <span class="status-tag" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </span>
            </td>
            <td>
              <button @click="editItem(item)" class="btn-edit">编辑</button>
              <button @click="deleteItem(item.id)" class="btn-delete">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 第四个 div：分页 -->
    <div class="pagination-section">
      <div class="pagination-info">
        共 {{ total }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页
      </div>
      <div class="pagination-controls">
        <button @click="goToPage(1)" :disabled="currentPage === 1" class="page-btn">首页</button>
        <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1" class="page-btn">上一页</button>
        <span class="page-numbers">
          <button 
            v-for="page in getPageNumbers()" 
            :key="page"
            @click="goToPage(page)"
            :class="['page-number', { active: page === currentPage }]"
          >
            {{ page }}
          </button>
        </span>
        <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages" class="page-btn">下一页</button>
        <button @click="goToPage(totalPages)" :disabled="currentPage === totalPages" class="page-btn">末页</button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h3>{{ modalTitle }}</h3>
        <form @submit.prevent="saveItem">
          <div class="form-group">
            <label>活动名称:</label>
            <input v-model="formData.activityName" required placeholder="请输入活动名称" />
          </div>
          <div class="form-group">
            <label>类型名称:</label>
            <select v-model="formData.typeId" required>
              <option value="">请选择活动类型</option>
              <option v-for="type in activityTypes" :key="type.id" :value="type.id">{{ type.typeName }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>描述:</label>
            <textarea v-model="formData.description" placeholder="请输入活动描述"></textarea>
          </div>
          <div class="form-group">
            <label>地点:</label>
            <input v-model="formData.location" required placeholder="请输入活动地点" />
          </div>
          <div class="form-group">
            <label>开始时间:</label>
            <input v-model="formData.startTime" type="datetime-local" required />
          </div>
          <div class="form-group">
            <label>结束时间:</label>
            <input v-model="formData.endTime" type="datetime-local" required />
          </div>
          <div class="form-group">
            <label>最大参与人数:</label>
            <input v-model.number="formData.maxParticipants" type="number" required min="1" placeholder="请输入最大参与人数" />
          </div>
          <div class="form-group">
            <label>状态:</label>
            <select v-model="formData.status">
              <option value="0">未开始</option>
              <option value="1">进行中</option>
              <option value="2">已结束</option>
              <option value="3">已取消</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="submit" class="confirm-btn">确定</button>
            <button type="button" @click="closeModal" class="cancel-btn">取消</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import * as publicActivityApi from '../../api/publicActivity'

// 响应式数据
const activities = ref<any[]>([])
const searchKeyword = ref('')
const activityStatus = ref('')
const selectedIds = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showModal = ref(false)
const modalMode = ref<'add' | 'edit'>('add') // 'add' 或 'edit'
const formData = ref({
  id: 0,
  activityName: '',
  typeId: 0,
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: 0,
  currentParticipants: 0,
  status: 0,
  creatorId: 5 // 默认管理员ID
})
const activityTypes = ref<{ id: number; typeName: string }[]>([])

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
const isAllSelected = computed(() => 
  activities.value.length > 0 && selectedIds.value.length === activities.value.length
)

const modalTitle = computed(() => 
  modalMode.value === 'add' ? '新增活动' : '编辑活动'
)

// 方法
const loadActivities = async () => {
  try {
    console.log('开始加载活动数据...')
    console.log('当前页:', currentPage.value, '每页条数:', pageSize.value)
    
    // 清空选中状态
    selectedIds.value = []
    
    // 加载活动类型
    const types = await publicActivityApi.getActivityTypes()
    activityTypes.value = types
    
    // 加载活动信息（使用分页）
    const result = await publicActivityApi.getActivitiesPage(currentPage.value, pageSize.value)
    console.log('后端返回的活动数据:', result)
    activities.value = result.records || []
    total.value = result.total || 0
    console.log('加载完成，数据条数:', activities.value.length, '总记录数:', total.value)
  } catch (error) {
    console.error('加载活动失败:', error)
    alert('加载数据失败，请刷新页面重试')
  }
}

const handleSearch = async () => {
  try {
    console.log('开始搜索活动...')
    console.log('搜索关键词:', searchKeyword.value)
    console.log('活动状态:', activityStatus.value)
    
    // 重置到第一页
    currentPage.value = 1
    
    // 加载所有活动
    let result = await publicActivityApi.getActivities()
    
    // 根据关键词过滤
    if (searchKeyword.value.trim()) {
      result = result.filter((item: any) => 
        (item.activityName || '').toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
        (item.location || '').toLowerCase().includes(searchKeyword.value.toLowerCase())
      )
    }
    
    // 根据状态过滤
    if (activityStatus.value !== '') {
      const status = Number(activityStatus.value)
      result = result.filter((item: any) => Number(item.status) === status)
    }
    
    activities.value = result
    total.value = result.length
    console.log('搜索完成，结果数量:', activities.value.length)
  } catch (error) {
    console.error('搜索活动失败:', error)
  }
}

const resetSearch = async () => {
  searchKeyword.value = ''
  activityStatus.value = ''
  currentPage.value = 1
  await loadActivities()
}

const selectAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    selectedIds.value = activities.value.map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    console.log('没有选中任何项目')
    return
  }
  
  console.log('batchDelete 被调用，选中数量:', selectedIds.value.length)
  
  const confirmed = window.confirm(`确定要删除选中的 ${selectedIds.value.length} 项吗？`)
  console.log('用户选择:', confirmed)
  
  if (!confirmed) {
    console.log('用户取消了批量删除操作')
    return
  }
  
  console.log('开始执行批量删除操作')
  try {
    const promises = selectedIds.value.map(id => publicActivityApi.deleteActivity(id))
    const results = await Promise.all(promises)
    console.log('批量删除结果:', results)
    if (results.some(result => !result)) {
      throw new Error('部分删除操作失败')
    }
    alert('批量删除成功！')
    await loadActivities()
    selectedIds.value = []
    console.log('批量删除成功，数据已重新加载')
  } catch (error) {
    console.error('批量删除失败:', error)
    if (isError(error)) {
      alert('批量删除失败: ' + error.message)
    } else {
      alert('批量删除失败: 未知错误')
    }
  }
}

const openAddModal = () => {
  modalMode.value = 'add'
  formData.value = {
    id: 0,
    activityName: '',
    typeId: 0,
    description: '',
    location: '',
    startTime: '',
    endTime: '',
    maxParticipants: 0,
    currentParticipants: 0,
    status: 0,
    creatorId: 5
  }
  showModal.value = true
}

const editItem = (item: any) => {
  modalMode.value = 'edit'
  formData.value = { ...item }
  showModal.value = true
}

const deleteItem = async (id: number) => {
  console.log('deleteItem 被调用，ID:', id)
  
  const confirmed = window.confirm('确定要删除此项吗？删除后将无法恢复。')
  console.log('用户选择:', confirmed)
  
  if (!confirmed) {
    console.log('用户取消了删除操作')
    return
  }
  
  console.log('开始执行删除操作')
  try {
    const result = await publicActivityApi.deleteActivity(id)
    console.log('删除结果:', result)
    if (!result) {
      throw new Error('删除操作失败')
    }
    alert('删除成功！')
    await loadActivities()
    console.log('删除成功，数据已重新加载')
  } catch (error) {
    console.error('删除失败:', error)
    if (isError(error)) {
      alert('删除失败: ' + error.message)
    } else {
      alert('删除失败: 未知错误')
    }
  }
}

const saveItem = async () => {
  if (!formData.value.activityName.trim()) {
    alert('请填写活动名称')
    return
  }
  if (!formData.value.typeId) {
    alert('请选择活动类型')
    return
  }
  if (!formData.value.location.trim()) {
    alert('请填写活动地点')
    return
  }
  if (!formData.value.startTime) {
    alert('请选择开始时间')
    return
  }
  if (!formData.value.endTime) {
    alert('请选择结束时间')
    return
  }
  if (!formData.value.maxParticipants || formData.value.maxParticipants <= 0) {
    alert('请填写有效的最大参与人数')
    return
  }
  
  try {
    console.log('开始保存活动:', formData.value)
    if (modalMode.value === 'add') {
      const result = await publicActivityApi.createActivity(formData.value)
      console.log('新增活动结果:', result)
      if (!result) {
        throw new Error('新增活动失败')
      }
    } else {
      const result = await publicActivityApi.updateActivity(formData.value.id, formData.value)
      console.log('更新活动结果:', result)
      if (!result) {
        throw new Error('更新活动失败')
      }
    }
    
    await loadActivities()
    closeModal()
  } catch (error) {
    console.error('保存失败:', error)
    if (isError(error)) {
      alert('操作失败: ' + error.message)
    } else {
      alert('操作失败: 未知错误')
    }
  }
}

function isError(error: unknown): error is Error {
  return error instanceof Error;
}

const closeModal = () => {
  showModal.value = false
}

const goToPage = async (page: number) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  
  currentPage.value = page
  await loadActivities()
}

const getPageNumbers = () => {
  const pages = []
  const maxVisiblePages = 5
  let startPage = Math.max(1, currentPage.value - Math.floor(maxVisiblePages / 2))
  let endPage = Math.min(totalPages.value, startPage + maxVisiblePages - 1)
  
  if (endPage - startPage + 1 < maxVisiblePages) {
    startPage = Math.max(1, endPage - maxVisiblePages + 1)
  }
  
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i)
  }
  
  return pages
}

const getStatusText = (status: number) => {
  const statusMap = ['未开始', '进行中', '已结束', '已取消']
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  const classMap = ['status-not-started', 'status-ongoing', 'status-ended', 'status-cancelled']
  return classMap[status] || ''
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toISOString().slice(0, 16) // YYYY-MM-DDTHH:mm 格式，适用于 datetime-local
}

// 初始化加载数据
onMounted(async () => {
  await loadActivities()
})
</script>

<style scoped>
.activity-management {
  padding: 20px;
}

.query-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input, .status-select {
  width: 300px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.status-select {
  width: 150px;
  min-width: 150px;
  background: white;
  font-size: 14px;
}

.search-btn, .reset-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.search-btn {
  background-color: #409eff;
  color: white;
}

.reset-btn {
  background-color: #909399;
  color: white;
}

.action-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.add-btn, .delete-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn {
  background-color: #67c23a;
  color: white;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
}

.delete-btn:disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

.data-section {
  margin-bottom: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  border-radius: 4px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  color: #333;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
  color: #333;
}

.data-table th {
  background-color: #f5f7fa;
  font-weight: 500;
  color: #333;
}

.status-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-not-started {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-ongoing {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-ended {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.status-cancelled {
  background-color: #f5f5f5;
  color: #666;
}

.btn-edit, .btn-delete {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
  font-size: 12px;
}

.btn-edit {
  background-color: #409eff;
  color: white;
}

.btn-delete {
  background-color: #f56c6c;
  color: white;
}

.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-info {
  color: #606266;
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  background-color: white;
  cursor: pointer;
  border-radius: 4px;
}

.page-btn:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 2px;
}

.page-number {
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  background-color: white;
  cursor: pointer;
  border-radius: 4px;
}

.page-number.active {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  width: 600px;
  max-width: 90vw;
  color: #333;
}

.modal-content h3 {
  color: #333;
  margin-top: 0;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #606266;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  color: #606266;
  font-size: 14px;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  border-color: #409eff;
  outline: none;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.confirm-btn, .cancel-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.confirm-btn {
  background-color: #409eff;
  color: white;
}

.cancel-btn {
  background-color: #909399;
  color: white;
}
</style>