<template>
  <div class="activity-type-management">
    <!-- 第一个 div：查询功能 -->
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入活动类型名称或描述进行搜索" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
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
            <th>类型名称</th>
            <th>描述</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in activityTypes" :key="item.id">
            <td>
              <input 
                type="checkbox" 
                :value="item.id"
                v-model="selectedIds"
              />
            </td>
            <td>{{ item.typeName }}</td>
            <td>{{ item.description }}</td>
            <td>
              <span class="status-tag" :class="item.status === 1 ? 'status-active' : 'status-inactive'">
                {{ item.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(item.createTime) }}</td>
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
            <label>类型名称:</label>
            <input v-model="formData.typeName" required />
          </div>
          <div class="form-group">
            <label>描述:</label>
            <textarea v-model="formData.description"></textarea>
          </div>
          <div class="form-group">
            <label>状态:</label>
            <select v-model="formData.status">
              <option value="1">启用</option>
              <option value="0">禁用</option>
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
const activityTypes = ref<any[]>([])
const searchKeyword = ref('')
const selectedIds = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showModal = ref(false)
const modalMode = ref<'add' | 'edit'>('add') // 'add' 或 'edit'
const formData = ref({
  id: 0,
  typeName: '',
  description: '',
  status: 1
})

// 计算属性
const isAllSelected = computed(() => 
  activityTypes.value.length > 0 && selectedIds.value.length === activityTypes.value.length
)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const modalTitle = computed(() => 
  modalMode.value === 'add' ? '新增活动类型' : '编辑活动类型'
)

// 方法
const loadActivityTypes = async () => {
  try {
    console.log('开始加载活动类型数据...')
    const result = await publicActivityApi.getActivityTypesPage(currentPage.value, pageSize.value)
    console.log('后端返回的数据:', result)
    activityTypes.value = result.records || []
    total.value = result.total || 0
    console.log('加载完成，数据条数:', activityTypes.value.length, '总记录数:', total.value)
  } catch (error) {
    console.error('加载活动类型失败:', error)
  }
}

const handleSearch = async () => {
  try {
    console.log('开始搜索活动类型...')
    console.log('搜索关键词:', searchKeyword.value)
    
    // 重置到第一页
    currentPage.value = 1
    
    // 使用分页API进行搜索
    let url = `/api/activity-types/page?current=${currentPage.value}&size=${pageSize.value}`
    if (searchKeyword.value.trim()) {
      url += `&keyword=${encodeURIComponent(searchKeyword.value)}`
    }
    
    const response = await fetch(url)
    const result = await response.json()
    
    activityTypes.value = result.records || []
    total.value = result.total || 0
    
    console.log('搜索完成，结果数量:', activityTypes.value.length)
  } catch (error) {
    console.error('搜索活动类型失败:', error)
  }
}

const resetSearch = async () => {
  searchKeyword.value = ''
  await loadActivityTypes()
}

const selectAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    selectedIds.value = activityTypes.value.map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

const batchDelete = async () => {
  if (selectedIds.value.length === 0) return
  
  if (!confirm(`确定要删除选中的 ${selectedIds.value.length} 项吗？`)) {
    return
  }
  
  try {
    const promises = selectedIds.value.map(id => publicActivityApi.deleteActivityType(id))
    await Promise.all(promises)
    await loadActivityTypes()
    selectedIds.value = []
  } catch (error) {
    console.error('批量删除失败:', error)
  }
}

const openAddModal = () => {
  modalMode.value = 'add'
  formData.value = {
    id: 0,
    typeName: '',
    description: '',
    status: 1
  }
  showModal.value = true
}

const editItem = (item: any) => {
  modalMode.value = 'edit'
  formData.value = { ...item }
  showModal.value = true
}

const deleteItem = async (id: number) => {
  if (!confirm('确定要删除此项吗？')) {
    return
  }
  
  try {
    await publicActivityApi.deleteActivityType(id)
    await loadActivityTypes()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const saveItem = async () => {
  try {
    console.log('保存数据，模式:', modalMode.value, '表单数据:', formData.value)
    let result
    if (modalMode.value === 'add') {
      result = await publicActivityApi.createActivityType(formData.value)
      console.log('创建结果:', result)
    } else {
      result = await publicActivityApi.updateActivityType(formData.value.id, formData.value)
      console.log('更新结果:', result)
    }
    
    console.log('保存成功，重新加载数据')
    await loadActivityTypes()
    closeModal()
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败：' + error)
  }
}

const closeModal = () => {
  showModal.value = false
}

const goToPage = async (page: number) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  
  currentPage.value = page
  await loadActivityTypes()
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

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 初始化加载数据
onMounted(async () => {
  console.log('组件已挂载，开始加载数据')
  await loadActivityTypes()
  console.log('数据加载完成')
})
</script>

<style scoped>
.activity-type-management {
  padding: 20px;
}

.query-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  width: 300px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
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

.status-active {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-inactive {
  background-color: #fff1f0;
  color: #f5222d;
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
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
}

.page-btn:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
  background-color: #f5f5f5;
}

.page-numbers {
  display: flex;
  gap: 2px;
}

.page-number {
  padding: 6px 10px;
  border: 1px solid #dcdfe6;
  background-color: white;
  color: #606266;
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
  width: 500px;
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