<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import * as publicActivityApi from '../../api/publicActivity'
import type { PublicActivityApplication } from '../../api/publicActivity'
import { useUserStore } from '../../stores/user'

const applications = ref<PublicActivityApplication[]>([])
const loading = ref(false)
const error = ref('')

// 分页和搜索
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const selectedStatus = ref<number | null>(null)

// 初始化用户store
const userStore = useUserStore()

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const loadApplications = async (retryCount = 0) => {
  loading.value = true
  error.value = ''
  try {
    let statusParam = selectedStatus.value !== null ? selectedStatus.value.toString() : undefined
    const response = await publicActivityApi.getApplicationsPage(currentPage.value, pageSize.value, searchKeyword.value, statusParam)
    applications.value = response.records || []
    total.value = response.total || 0
  } catch (err) {
    if (retryCount < 3) {
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
  selectedStatus.value = null
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
  if (confirm('确定要批准该申请吗？')) {
    try {
      const currentUserId = userStore.userInfo?.id || 1
      await publicActivityApi.approveApplication(id, currentUserId, '批准申请')
      loadApplications()
    } catch (err) {
      error.value = '批准申请失败'
      console.error('批准申请失败:', err)
    }
  }
}

const handleReject = async (id: number) => {
  if (confirm('确定要拒绝该申请吗？')) {
    try {
      const currentUserId = userStore.userInfo?.id || 1
      await publicActivityApi.rejectApplication(id, currentUserId, '拒绝申请')
      loadApplications()
    } catch (err) {
      error.value = '拒绝申请失败'
      console.error('拒绝申请失败:', err)
    }
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
  <div class="application-management">
    <div class="section-header">
      <h2>公益活动申请管理</h2>
    </div>
    
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    
    <!-- 搜索功能 -->
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入活动名称或申请理由进行搜索" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="selectedStatus" class="status-select">
        <option :value="null">全部状态</option>
        <option :value="0">待审批</option>
        <option :value="1">已通过</option>
        <option :value="2">已拒绝</option>
        <option :value="3">已取消</option>
      </select>
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
            <th>活动负责人</th>
            <th>申请人姓名</th>
            <th>申请理由</th>
            <th>申请时间</th>
            <th>申请状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="app in applications" :key="app.id">
            <td>{{ app.activityName }}</td>
            <td>{{ app.activityCreatorName }}</td>
            <td>{{ app.userName }}</td>
            <td>{{ app.applicationReason }}</td>
            <td>{{ app.applyTime?.split('T')[0] }}</td>
            <td>
              <span :class="['status-tag', getStatusClass(app.status)]">
                {{ getStatusText(app.status) }}
              </span>
            </td>
            <td>
              <button v-if="app.status === 0" @click="handleApprove(app.id)" class="btn-approve btn-small">
                批准
              </button>
              <button v-if="app.status === 0" @click="handleReject(app.id)" class="btn-reject btn-small">
                拒绝
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
  </div>
</template>

<style scoped>
.application-management {
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

.btn-small {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  margin-right: 5px;
  border: none;
}

.btn-approve {
  background-color: #67c23a;
  color: white;
}

.btn-approve:hover {
  background-color: #529b2e;
}

.btn-reject {
  background-color: #f56c6c;
  color: white;
}

.btn-reject:hover {
  background-color: #dd6161;
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

.status-select {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: white;
  color: #606266;
  cursor: pointer;
  min-width: 120px;
}

.status-select:focus {
  outline: none;
  border-color: #409eff;
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
</style>