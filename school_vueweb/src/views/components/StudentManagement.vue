<template>
  <div class="student-management">
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入账号、名称或学号进行搜索" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <button @click="handleSearch" class="search-btn">搜索</button>
      <button @click="resetSearch" class="reset-btn">重置</button>
    </div>

    <div class="action-section">
      <button @click="openAddModal" class="add-btn">新增用户</button>
      <button @click="batchDelete" :disabled="selectedIds.length === 0" class="delete-btn">批量删除</button>
    </div>

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
            <th>学号/工号</th>
            <th>名称</th>
            <th>电话</th>
            <th>邮箱</th>
            <th>角色</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in users" :key="item.id">
            <td>
              <input 
                type="checkbox" 
                :value="item.id"
                v-model="selectedIds"
              />
            </td>
            <td>{{ item.code || '-' }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.phone || '-' }}</td>
            <td>{{ item.email || '-' }}</td>
            <td>{{ getRoleName(item.roleType) }}</td>
            <td>
              <button @click="editItem(item)" class="btn-edit">编辑</button>
              <button @click="deleteItem(item.id)" class="btn-delete">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

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

    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h3>{{ modalTitle }}</h3>
        <div class="form-group">
          <label>账号:</label>
          <input v-model="formData.username" type="text" placeholder="请输入账号" :disabled="modalMode === 'edit'" required />
        </div>
        <div class="form-group">
          <label>密码:</label>
          <input v-model="formData.password" type="password" placeholder="请输入密码" :disabled="modalMode === 'edit'" required />
        </div>
        <div class="form-group">
          <label>名称:</label>
          <input v-model="formData.name" type="text" placeholder="请输入名称" required />
        </div>
        <div class="form-group">
          <label>电话:</label>
          <input v-model="formData.phone" type="text" placeholder="请输入电话" />
        </div>
        <div class="form-group">
          <label>邮箱:</label>
          <input v-model="formData.email" type="email" placeholder="请输入邮箱" />
        </div>
        <div class="form-group">
          <label>角色:</label>
          <select v-model="formData.roleType" required>
            <option value="1">学生</option>
            <option value="2">教师</option>
            <option value="3">校友</option>
            <option value="4">校外爱心人士</option>
          </select>
        </div>
        <div class="form-group">
          <label>学号/工号:</label>
          <input v-model="formData.code" type="text" placeholder="请输入学号/工号" />
        </div>
        <div class="form-group">
          <label>性别:</label>
          <select v-model="formData.gender" required>
            <option value="1">男</option>
            <option value="0">女</option>
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
import { getUsersPage, createUser, updateUser, deleteUser } from '@/api/auth'

const users = ref<any[]>([])
const searchKeyword = ref('')
const selectedIds = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showModal = ref(false)
const modalMode = ref<'add' | 'edit'>('add')
const formData = ref({
  id: 0,
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  roleType: 1,
  code: '',
  gender: 1
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
const isAllSelected = computed(() => 
  users.value.length > 0 && selectedIds.value.length === users.value.length
)

const modalTitle = computed(() => 
  modalMode.value === 'add' ? '新增用户' : '编辑用户'
)

const getRoleName = (roleType: number) => {
  const roleMap: { [key: number]: string } = {
    1: '学生',
    2: '教师',
    3: '校友',
    4: '校外爱心人士'
  }
  return roleMap[roleType] || '未知'
}

const loadUsers = async () => {
  try {
    console.log('开始加载用户数据...')
    const result = await getUsersPage(currentPage.value, pageSize.value, searchKeyword.value)
    console.log('后端返回的数据:', result)
    users.value = (result.records || []).filter((user: any) => user.roleType !== 5)
    total.value = result.total || 0
    console.log('加载完成，数据条数:', users.value.length, '总记录数:', total.value)
  } catch (error) {
    console.error('加载用户失败:', error)
  }
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadUsers()
}

const resetSearch = async () => {
  searchKeyword.value = ''
  currentPage.value = 1
  await loadUsers()
}

const selectAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    selectedIds.value = users.value.map(item => item.id)
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
    const promises = selectedIds.value.map(id => deleteUser(id))
    const results = await Promise.all(promises)
    console.log('批量删除结果:', results)
    if (results.some((result: boolean) => !result)) {
      throw new Error('部分删除操作失败')
    }
    await loadUsers()
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
    username: '',
    password: '',
    name: '',
    email: '',
    phone: '',
    roleType: 1,
    code: '',
    gender: 1
  }
  showModal.value = true
}

const editItem = (item: any) => {
  modalMode.value = 'edit'
  formData.value = { ...item, password: '' }
  showModal.value = true
}

const deleteItem = async (id: number) => {
  const confirmed = confirm('确定要删除此项吗？')
  if (!confirmed) {
    return
  }
  
  try {
    const result = await deleteUser(id)
    console.log('删除结果:', result)
    if (!result) {
      throw new Error('删除失败')
    }
    await loadUsers()
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
  if (!formData.value.username.trim()) {
    alert('请输入账号')
    return
  }
  if (modalMode.value === 'add' && !formData.value.password.trim()) {
    alert('请输入密码')
    return
  }
  if (!formData.value.name.trim()) {
    alert('请输入名称')
    return
  }
  
  try {
    let result
    if (modalMode.value === 'add') {
      result = await createUser(formData.value)
      console.log('新增结果:', result)
      if (!result) {
        throw new Error('新增失败')
      }
    } else {
      const updateData = {
        name: formData.value.name,
        email: formData.value.email,
        phone: formData.value.phone,
        roleType: formData.value.roleType,
        code: formData.value.code,
        gender: formData.value.gender
      }
      result = await updateUser(formData.value.id, updateData)
      console.log('更新结果:', result)
      if (!result) {
        throw new Error('更新失败')
      }
    }
    
    await loadUsers()
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
  await loadUsers()
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

onMounted(async () => {
  await loadUsers()
})
</script>

<style scoped>
.student-management {
  padding: 20px;
}

.query-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
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
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
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
