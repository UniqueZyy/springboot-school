<template>
  <div class="donation-type-management">
    <!-- 第一个 div：查询功能 -->
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入类型名称或描述进行搜索" 
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
          <tr v-for="item in donationTypes" :key="item.id">
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
              <span class="status-tag" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
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
        <div class="form-group">
          <label>类型名称:</label>
          <input v-model="formData.typeName" type="text" placeholder="请输入类型名称" />
        </div>
        <div class="form-group">
          <label>描述:</label>
          <textarea v-model="formData.description" placeholder="请输入描述"></textarea>
        </div>
        <div class="form-group">
          <label>状态:</label>
          <select v-model="formData.status">
            <option value="1">启用</option>
            <option value="0">禁用</option>
          </select>
        </div>
        <div class="form-actions">
          <button @click="submitForm" class="confirm-btn">保存</button>
          <button @click="closeModal" class="cancel-btn">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import * as donationApi from '../../api/donation'

// 响应式数据
const donationTypes = ref<any[]>([])
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
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
const isAllSelected = computed(() => 
  donationTypes.value.length > 0 && selectedIds.value.length === donationTypes.value.length
)

const modalTitle = computed(() => 
  modalMode.value === 'add' ? '新增捐赠物资类型' : '编辑捐赠物资类型'
)

// 方法
const loadDonationTypes = async () => {
  try {
    console.log('开始加载捐赠物资类型数据...')
    console.log('当前页:', currentPage.value, '每页条数:', pageSize.value)
    
    // 加载捐赠物资类型
    const result = await donationApi.getDonationTypesPage(currentPage.value, pageSize.value)
    console.log('后端返回的数据:', result)
    donationTypes.value = result.records || []
    total.value = result.total || 0
    console.log('加载完成，数据条数:', donationTypes.value.length, '总记录数:', total.value)
  } catch (error) {
    console.error('加载捐赠物资类型失败:', error)
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    await loadDonationTypes()
    return
  }
  
  try {
    console.log('开始搜索捐赠物资类型...')
    console.log('搜索关键词:', searchKeyword.value)
    
    // 重置到第一页
    currentPage.value = 1
    
    // 使用分页API进行搜索
    const result = await donationApi.searchDonationTypes(searchKeyword.value)
    donationTypes.value = result
    total.value = result.length
    console.log('搜索完成，结果数量:', donationTypes.value.length)
  } catch (error) {
    console.error('搜索捐赠物资类型失败:', error)
  }
}

const resetSearch = async () => {
  searchKeyword.value = ''
  currentPage.value = 1
  await loadDonationTypes()
}

const selectAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    selectedIds.value = donationTypes.value.map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

const batchDelete = async () => {
  if (selectedIds.value.length === 0) return
  
  const confirmed = confirm(`确定要删除选中的 ${selectedIds.value.length} 项吗？`)
  if (!confirmed) {
    return
  }
  
  try {
    const promises = selectedIds.value.map(id => donationApi.deleteDonationType(id))
    const results = await Promise.all(promises)
    console.log('批量删除结果:', results)
    if (results.some(result => !result)) {
      throw new Error('部分删除操作失败')
    }
    await loadDonationTypes()
    selectedIds.value = []
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
  const confirmed = confirm('确定要删除此项吗？')
  if (!confirmed) {
    return
  }
  
  try {
    const result = await donationApi.deleteDonationType(id)
    console.log('删除结果:', result)
    if (!result) {
      throw new Error('删除失败')
    }
    await loadDonationTypes()
  } catch (error) {
    console.error('删除失败:', error)
    if (isError(error)) {
      alert('删除失败: ' + error.message)
    } else {
      alert('删除失败: 未知错误')
    }
  }
}

const submitForm = async () => {
  if (!formData.value.typeName.trim()) {
    alert('请输入类型名称')
    return
  }
  
  try {
    let result
    if (modalMode.value === 'add') {
      result = await donationApi.createDonationType(formData.value)
      console.log('新增结果:', result)
      if (!result) {
        throw new Error('新增失败')
      }
    } else {
      result = await donationApi.updateDonationType(formData.value.id, formData.value)
      console.log('更新结果:', result)
      if (!result) {
        throw new Error('更新失败')
      }
    }
    
    await loadDonationTypes()
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
  await loadDonationTypes()
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
  return status === 1 ? '启用' : '禁用'
}

const getStatusClass = (status: number) => {
  return status === 1 ? 'status-enabled' : 'status-disabled'
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toISOString().slice(0, 19).replace('T', ' ')
}

// 初始化加载数据
onMounted(async () => {
  await loadDonationTypes()
})
</script>

<style scoped>
.donation-type-management {
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
  background-color: #606266;
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
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

.data-table th {
  background-color: #f5f7fa;
  font-weight: 600;
}

.data-table tr:hover {
  background-color: #f5f7fa;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.status-enabled {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-disabled {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.btn-edit, .btn-delete {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
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
  margin-top: 20px;
  padding: 10px 0;
}

.pagination-info {
  font-size: 14px;
  color: #606266;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-btn, .page-number {
  padding: 4px 10px;
  border: 1px solid #dcdfe6;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
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
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
  width: 400px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
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
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
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
  background-color: #606266;
  color: white;
}
</style>
