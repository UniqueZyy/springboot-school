<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import * as publicActivityApi from '../../api/publicActivity'
import type { PublicActivityApplication } from '../../api/publicActivity'
import { useUserStore } from '../../stores/user'

const applications = ref<PublicActivityApplication[]>([])
const loading = ref(false)
const error = ref('')

// 编辑相关
const isEditing = ref(false)
const currentEditId = ref(0)
const editForm = ref({
  applicationReason: ''
})

// 操作反馈
const successMessage = ref('')

// 分页和搜索
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

// 初始化用户store
const userStore = useUserStore()

// 计算用户角色和权限
const userRole = computed(() => userStore.userInfo?.roleType || 0)
const isAdmin = computed(() => userRole.value === 5)
const isStudent = computed(() => userRole.value === 1)
const currentUserId = computed(() => userStore.userInfo?.id || 0)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const loadApplications = async (retryCount = 0) => {
  loading.value = true
  error.value = ''
  try {
    if (isAdmin.value) {
      // 管理员加载所有申请
      const response = await publicActivityApi.getApplicationsPage(currentPage.value, pageSize.value, searchKeyword.value)
      applications.value = response.records || []
      total.value = response.total || 0
    } else if (currentUserId.value) {
      // 普通用户只加载自己的申请
      if (searchKeyword.value.trim()) {
        // 有搜索关键词时，获取所有数据后在前端过滤和分页
        const allResponse = await publicActivityApi.getApplicationsByUserId(currentUserId.value, 1, 1000)
        let allApplications: PublicActivityApplication[] = allResponse.records || []
        
        // 前端过滤
        allApplications = allApplications.filter(app => 
          app.activityName?.includes(searchKeyword.value!) || 
          app.applicationReason?.includes(searchKeyword.value!)
        )
        
        // 更新总数
        total.value = allApplications.length
        
        // 前端分页
        const startIndex = (currentPage.value - 1) * pageSize.value
        const endIndex = startIndex + pageSize.value
        applications.value = allApplications.slice(startIndex, endIndex)
      } else {
        // 无搜索关键词时，直接使用后端分页
        const response = await publicActivityApi.getApplicationsByUserId(currentUserId.value, currentPage.value, pageSize.value)
        applications.value = response.records || []
        total.value = response.total || 0
      }
    } else {
      // 未登录用户显示空列表
      applications.value = []
      total.value = 0
    }
  } catch (err) {
    if (retryCount < 3) {
      // 网络异常时重试，最多重试3次
      setTimeout(() => {
        loadApplications(retryCount + 1)
      }, 1000)
      return
    }
    error.value = '加载申请失败，请检查网络连接'
    console.error('加载申请失败:', err)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadApplications()
}

const resetSearch = async () => {
  searchKeyword.value = ''
  currentPage.value = 1
  await loadApplications()
}

const goToPage = async (page: number) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  
  currentPage.value = page
  await loadApplications()
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

const handleApprove = async (id: number) => {
  if (!isAdmin.value) {
    error.value = '权限不足，只有管理员可以批准申请'
    return
  }
  if (confirm('确定要批准该申请吗？')) {
    try {
      // 使用当前登录用户的ID作为审批人ID
      await publicActivityApi.approveApplication(id, currentUserId.value, '批准申请')
      loadApplications()
      successMessage.value = '批准申请成功'
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      error.value = '批准申请失败'
      console.error('批准申请失败:', err)
    }
  }
}

const handleReject = async (id: number) => {
  if (!isAdmin.value) {
    error.value = '权限不足，只有管理员可以拒绝申请'
    return
  }
  if (confirm('确定要拒绝该申请吗？')) {
    try {
      // 使用当前登录用户的ID作为审批人ID
      await publicActivityApi.rejectApplication(id, currentUserId.value, '拒绝申请')
      loadApplications()
      successMessage.value = '拒绝申请成功'
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      error.value = '拒绝申请失败'
      console.error('拒绝申请失败:', err)
    }
  }
}

const handleCancel = async (id: number) => {
  if (confirm('确定要取消该申请吗？')) {
    try {
      await publicActivityApi.cancelApplication(id)
      loadApplications()
      successMessage.value = '取消申请成功'
      setTimeout(() => {
        successMessage.value = ''
      }, 3000)
    } catch (err) {
      error.value = '取消申请失败'
      console.error('取消申请失败:', err)
    }
  }
}

// 编辑处理
const startEdit = (app: PublicActivityApplication) => {
  currentEditId.value = app.id
  editForm.value = {
    applicationReason: app.applicationReason
  }
  isEditing.value = true
}

const saveEdit = async () => {
  if (!editForm.value.applicationReason.trim()) {
    error.value = '申请理由不能为空'
    return
  }
  try {
    await publicActivityApi.updateApplication(currentEditId.value, {
      applicationReason: editForm.value.applicationReason
    })
    isEditing.value = false
    loadApplications()
    successMessage.value = '编辑申请成功'
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (err) {
    error.value = '编辑失败'
    console.error('编辑失败:', err)
  }
}

const cancelEdit = () => {
  isEditing.value = false
  currentEditId.value = 0
  editForm.value = {
    applicationReason: ''
  }
}

const getStatusText = (status: number) => {
  const statusMap = ['待审批', '已通过', '已拒绝', '已取消']
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  const classMap = ['status-pending', 'status-approved', 'status-rejected', 'status-canceled']
  return classMap[status] || ''
}

onMounted(() => {
  loadApplications()
})
</script>

<template>
  <div class="activity-application-management">
    <div class="section-header">
      <h2>公益活动申请管理</h2>
    </div>
    
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
    
    <!-- 搜索功能 -->
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入活动名称或申请理由进行搜索" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <button @click="handleSearch" class="search-btn">搜索</button>
      <button @click="resetSearch" class="reset-btn">重置</button>
    </div>
    
    <div v-if="loading" class="loading">
      加载中...
    </div>
    
    <div v-else class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>活动名称</th>
            <th v-if="isAdmin">活动负责人</th>
            <th>活动状态</th>
            <th v-if="isAdmin">申请学生名称</th>
            <th>申请理由</th>
            <th>活动开始时间</th>
            <th>活动结束时间</th>
            <th>申请状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="app in applications" :key="app.id">
            <td>{{ app.activityName }}</td>
            <td v-if="isAdmin">{{ app.activityCreatorName }}</td>
            <td>{{ app.activityStatus === 0 ? '未开始' : app.activityStatus === 1 ? '进行中' : app.activityStatus === 2 ? '已结束' : '未知' }}</td>
            <td v-if="isAdmin">{{ app.userName }}</td>
            <td>{{ app.applicationReason }}</td>
            <td>{{ app.activityStartTime?.split('T')[0] }}</td>
            <td>{{ app.activityEndTime?.split('T')[0] }}</td>
            <td>
              <span :class="['status-tag', getStatusClass(app.status)]">
                {{ getStatusText(app.status) }}
              </span>
            </td>
            <td class="action-cell">
              <!-- 编辑按钮 -->
              <button v-if="app.status === 0 && (isAdmin || app.userId === currentUserId)" @click="startEdit(app)" class="btn-edit">
                编辑
              </button>
              <!-- 管理员操作 -->
              <button v-if="isAdmin && app.status === 0" @click="handleApprove(app.id)" class="btn-approve">
                批准
              </button>
              <button v-if="isAdmin && app.status === 0" @click="handleReject(app.id)" class="btn-reject">
                拒绝
              </button>
              <!-- 普通用户操作 -->
              <button v-if="!isAdmin && app.status === 0 && app.userId === currentUserId" @click="handleCancel(app.id)" class="btn-delete">
                取消申请
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="applications.length === 0" class="empty-state">
        <p>暂无申请记录</p>
      </div>
    </div>
    
    <!-- 分页 -->
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
    
    <!-- 编辑模态框 -->
    <div v-if="isEditing" class="modal-overlay" @click="cancelEdit">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑申请理由</h3>
          <button class="close-button" @click="cancelEdit">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>申请理由：</label>
            <textarea v-model="editForm.applicationReason" class="form-textarea" rows="4"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="cancelEdit" class="btn-secondary">取消</button>
          <button @click="saveEdit" class="btn-primary">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.activity-application-management {
  padding: 20px;
}

.section-header {
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.error-message {
  background-color: #fff1f0;
  color: #ff4d4f;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.success-message {
  background-color: #f6ffed;
  color: #52c41a;
  padding: 10px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.table-container {
  overflow-x: auto;
  margin-bottom: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  color: #333;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
  color: #333;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.data-table tbody tr:hover {
  background-color: #f8f9fa;
}

.action-cell {
  text-align: center;
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: #999;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-approved {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-rejected {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.status-canceled {
  background-color: #f0f0f0;
  color: #999;
}

.btn-edit {
  background: #67c23a;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
}

.btn-edit:hover {
  background: #5daf34;
}

.btn-approve {
  background: #67c23a;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
}

.btn-approve:hover {
  background: #5daf34;
}

.btn-reject {
  background: #f56c6c;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
}

.btn-reject:hover {
  background: #e64c4c;
}

.btn-delete {
  background: #f56c6c;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-delete:hover {
  background: #e64c4c;
}

/* 搜索和分页样式 */
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

.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
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

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.close-button:hover {
  color: #666;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 500;
}

.form-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-textarea:focus {
  outline: none;
  border-color: #67c23a;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  text-align: right;
}

.btn-primary {
  background: #67c23a;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary:hover {
  background: #5daf34;
}

.btn-secondary {
  background: #f0f0f0;
  color: #666;
  border: 1px solid #ddd;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-right: 10px;
}

.btn-secondary:hover {
  background: #e0e0e0;
}
</style>
