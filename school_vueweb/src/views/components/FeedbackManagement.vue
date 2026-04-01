<template>
  <div class="feedback-management">
    <!-- 第一个 div：查询功能 -->
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入反馈内容或提交人进行搜索" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="typeFilter" class="type-select">
        <option value="">全部类型</option>
        <option value="1">建议</option>
        <option value="2">问题</option>
        <option value="3">投诉</option>
        <option value="4">其他</option>
      </select>
      <select v-model="statusFilter" class="status-select">
        <option value="">全部状态</option>
        <option value="0">未处理</option>
        <option value="1">已处理</option>
      </select>
      <button @click="handleSearch" class="search-btn">搜索</button>
      <button @click="resetSearch" class="reset-btn">重置</button>
    </div>

    <!-- 第二个 div：统计信息 -->
    <div class="stats-section">
      <div class="stat-card">
        <div class="stat-number">{{ totalFeedbacks }}</div>
        <div class="stat-label">总反馈数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ pendingFeedbacks }}</div>
        <div class="stat-label">未处理</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ processedFeedbacks }}</div>
        <div class="stat-label">已处理</div>
      </div>
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
            <th>ID</th>
            <th>反馈类型</th>
            <th>反馈内容</th>
            <th>提交人</th>
            <th>提交时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in feedbacks" :key="item.id">
            <td>
              <input 
                type="checkbox" 
                :value="item.id"
                v-model="selectedIds"
                :disabled="item.status === 1"
              />
            </td>
            <td>{{ item.id }}</td>
            <td>
              <span class="type-tag" :class="getTypeClass(item.type)">
                {{ getTypeText(item.type) }}
              </span>
            </td>
            <td class="feedback-content">{{ item.content }}</td>
            <td>{{ item.submitterName }}</td>
            <td>{{ formatDate(item.submitTime) }}</td>
            <td>
              <span class="status-tag" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </span>
            </td>
            <td>
              <button @click="viewDetail(item)" class="btn-view">查看</button>
              <button 
                v-if="item.status === 0"
                @click="replyItem(item)" 
                class="btn-reply"
              >回复</button>
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

    <!-- 查看详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content detail-modal" @click.stop>
        <h3>反馈详情</h3>
        <div class="detail-section">
          <div class="detail-item">
            <label>反馈ID:</label>
            <span>{{ detailData.id }}</span>
          </div>
          <div class="detail-item">
            <label>反馈类型:</label>
            <span class="type-tag" :class="getTypeClass(detailData.type)">
              {{ getTypeText(detailData.type) }}
            </span>
          </div>
          <div class="detail-item">
            <label>反馈内容:</label>
            <span>{{ detailData.content }}</span>
          </div>
          <div class="detail-item">
            <label>提交人:</label>
            <span>{{ detailData.submitterName }}</span>
          </div>
          <div class="detail-item">
            <label>提交时间:</label>
            <span>{{ formatDate(detailData.submitTime) }}</span>
          </div>
          <div class="detail-item">
            <label>状态:</label>
            <span class="status-tag" :class="getStatusClass(detailData.status)">
              {{ getStatusText(detailData.status) }}
            </span>
          </div>
          <div v-if="detailData.status === 1" class="detail-item">
            <label>回复内容:</label>
            <span>{{ detailData.replyContent || '-' }}</span>
          </div>
          <div v-if="detailData.status === 1" class="detail-item">
            <label>回复人:</label>
            <span>{{ detailData.replyName || '-' }}</span>
          </div>
          <div v-if="detailData.status === 1" class="detail-item">
            <label>回复时间:</label>
            <span>{{ detailData.replyTime ? formatDate(detailData.replyTime) : '-' }}</span>
          </div>
        </div>
        <div class="form-actions">
          <button @click="closeDetailModal" class="cancel-btn">关闭</button>
        </div>
      </div>
    </div>

    <!-- 回复弹窗 -->
    <div v-if="showReplyModal" class="modal-overlay" @click="closeReplyModal">
      <div class="modal-content" @click.stop>
        <h3>回复反馈</h3>
        <div class="form-group">
          <label>反馈内容:</label>
          <div class="feedback-preview">{{ currentFeedback?.content }}</div>
        </div>
        <div class="form-group">
          <label>回复内容:</label>
          <textarea v-model="replyContent" placeholder="请输入回复内容" rows="4" required></textarea>
        </div>
        <div class="form-actions">
          <button @click="submitReply" class="confirm-btn">提交回复</button>
          <button @click="closeReplyModal" class="cancel-btn">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getFeedbacksPage, replyFeedback } from '@/api/feedback'
import type { Feedback } from '@/api/feedback'

// 响应式数据
const feedbacks = ref<any[]>([])
const searchKeyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')
const selectedIds = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showDetailModal = ref(false)
const showReplyModal = ref(false)
const detailData = ref<Feedback>({} as Feedback)
const replyContent = ref('')
const currentFeedback = ref<any>(null)

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
const isAllSelected = computed(() => 
  feedbacks.value.length > 0 && selectedIds.value.length === feedbacks.value.filter(item => item.status === 0).length
)

const totalFeedbacks = computed(() => total.value)
const pendingFeedbacks = computed(() => feedbacks.value.filter(item => item.status === 0).length)
const processedFeedbacks = computed(() => feedbacks.value.filter(item => item.status === 1).length)

// 方法
const loadFeedbacks = async () => {
  try {
    console.log('开始加载反馈数据...')
    console.log('当前页:', currentPage.value, '每页条数:', pageSize.value)
    
    // 加载反馈
    const result = await getFeedbacksPage(currentPage.value, pageSize.value, searchKeyword.value, typeFilter.value, statusFilter.value)
    console.log('后端返回的数据:', result)
    feedbacks.value = result.records || []
    total.value = result.total || 0
    console.log('加载完成，数据条数:', feedbacks.value.length, '总记录数:', total.value)
  } catch (error) {
    console.error('加载反馈失败:', error)
  }
}

const handleSearch = async () => {
  // 重置到第一页
  currentPage.value = 1
  await loadFeedbacks()
}

const resetSearch = async () => {
  searchKeyword.value = ''
  typeFilter.value = ''
  statusFilter.value = ''
  currentPage.value = 1
  await loadFeedbacks()
}

const selectAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    selectedIds.value = feedbacks.value
      .filter(item => item.status === 0)
      .map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

const viewDetail = (item: any) => {
  detailData.value = { ...item }
  showDetailModal.value = true
}

const replyItem = (item: any) => {
  currentFeedback.value = item
  replyContent.value = ''
  showReplyModal.value = true
}

const submitReply = async () => {
  if (!replyContent.value.trim()) {
    alert('请输入回复内容')
    return
  }
  
  try {
    const result = await replyFeedback(currentFeedback.value.id, {
      replyContent: replyContent.value,
      replyId: 1, // 默认回复人ID
      replyName: '管理员' // 默认回复人名称
    })
    console.log('回复结果:', result)
    if (!result) {
      throw new Error('回复失败')
    }
    
    await loadFeedbacks()
    closeReplyModal()
  } catch (error) {
    console.error('回复失败:', error)
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

const closeDetailModal = () => {
  showDetailModal.value = false
}

const closeReplyModal = () => {
  showReplyModal.value = false
  replyContent.value = ''
  currentFeedback.value = null
}

const goToPage = async (page: number) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  
  currentPage.value = page
  await loadFeedbacks()
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

const getTypeText = (type: number) => {
  const typeMap = ['', '建议', '问题', '投诉', '其他']
  return typeMap[type] || '其他'
}

const getTypeClass = (type: number) => {
  const classMap = ['', 'type-suggestion', 'type-problem', 'type-complaint', 'type-other']
  return classMap[type] || 'type-other'
}

const getStatusText = (status: number) => {
  return status === 0 ? '未处理' : '已处理'
}

const getStatusClass = (status: number) => {
  return status === 0 ? 'status-pending' : 'status-processed'
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toISOString().slice(0, 19).replace('T', ' ')
}

// 初始化加载数据
onMounted(async () => {
  await loadFeedbacks()
})
</script>

<style scoped>
.feedback-management {
  padding: 20px;
}

.query-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input, .type-select, .status-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.search-input {
  width: 300px;
}

.type-select, .status-select {
  width: 120px;
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

.stats-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background-color: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
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

.feedback-content {
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.type-tag, .status-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.type-suggestion {
  background-color: #f0f9eb;
  color: #67c23a;
}

.type-problem {
  background-color: #fef0f0;
  color: #f56c6c;
}

.type-complaint {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.type-other {
  background-color: #ecf5ff;
  color: #409eff;
}

.status-pending {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-processed {
  background-color: #f0f9eb;
  color: #67c23a;
}

.btn-view, .btn-reply {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
  font-size: 12px;
}

.btn-view {
  background-color: #909399;
  color: white;
}

.btn-reply {
  background-color: #409eff;
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

.detail-modal {
  width: 500px;
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 10px;
  align-items: flex-start;
}

.detail-item label {
  width: 100px;
  font-weight: 500;
  color: #606266;
  margin-right: 20px;
}

.detail-item span {
  flex: 1;
  color: #303133;
  word-break: break-word;
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

.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  resize: vertical;
  box-sizing: border-box;
}

.feedback-preview {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  min-height: 60px;
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
