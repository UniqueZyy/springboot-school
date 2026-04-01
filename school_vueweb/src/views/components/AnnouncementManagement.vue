<template>
  <div class="announcement-management">
    <!-- 第一个 div：查询功能 -->
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入公告标题或内容进行搜索" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="statusFilter" class="status-select">
        <option value="">全部状态</option>
        <option value="1">有效</option>
        <option value="0">过期</option>
      </select>
      <button @click="handleSearch" class="search-btn">搜索</button>
      <button @click="resetSearch" class="reset-btn">重置</button>
    </div>

    <!-- 第二个 div：新增和批量操作按钮 -->
    <div class="action-section">
      <button @click="openAddModal" class="add-btn">发布公告</button>
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
            <th width="50">
              <span>置顶</span>
            </th>
            <th>ID</th>
            <th>标题</th>
            <th>发布人</th>
            <th>发布时间</th>
            <th>过期时间</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in announcements" :key="item.id">
            <td>
              <input 
                type="checkbox" 
                :value="item.id"
                v-model="selectedIds"
              />
            </td>
            <td>
              <button 
                @click="toggleTop(item)" 
                :class="['top-btn', { active: item.isTop === 1 }]"
                :title="item.isTop === 1 ? '取消置顶' : '置顶'"
              >
                ⭐
              </button>
            </td>
            <td>{{ item.id }}</td>
            <td class="announcement-title">
              <span v-if="item.isTop === 1" class="top-tag">置顶</span>
              {{ item.title }}
            </td>
            <td>{{ item.publisherName }}</td>
            <td>{{ formatDate(item.publishTime) }}</td>
            <td>{{ formatDate(item.expireTime) }}</td>
            <td>
              <span class="status-tag" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </span>
            </td>
            <td>
              <button @click="viewDetail(item)" class="btn-view">查看</button>
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

    <!-- 查看详情弹窗 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content detail-modal" @click.stop>
        <h3>{{ detailData.title }}</h3>
        <div class="detail-header">
          <span class="publisher">发布人: {{ detailData.publisherName }}</span>
          <span class="publish-time">发布时间: {{ formatDate(detailData.publishTime) }}</span>
          <span class="expire-time">过期时间: {{ formatDate(detailData.expireTime) }}</span>
        </div>
        <div class="detail-content" v-html="detailData.content"></div>
        <div class="form-actions">
          <button @click="closeDetailModal" class="cancel-btn">关闭</button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content announcement-modal" @click.stop>
        <h3>{{ modalTitle }}</h3>
        <div class="form-group">
          <label>标题:</label>
          <input v-model="formData.title" type="text" placeholder="请输入公告标题" required />
        </div>
        <div class="form-group">
          <label>内容:</label>
          <div class="editor-container">
            <textarea v-model="formData.content" placeholder="请输入公告内容" rows="8" required></textarea>
            <div class="editor-tips">支持HTML标签</div>
          </div>
        </div>
        <div class="form-group">
          <label>过期时间:</label>
          <input v-model="formData.expireTime" type="datetime-local" required />
        </div>
        <div class="form-group">
          <label>置顶:</label>
          <input v-model="formData.isTop" type="checkbox" />
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
import { getAnnouncementsPage, createAnnouncement, updateAnnouncement, deleteAnnouncement, updateAnnouncementTop } from '@/api/announcement'
import type { Announcement } from '@/api/announcement'

// 响应式数据
const announcements = ref<any[]>([])
const searchKeyword = ref('')
const statusFilter = ref('')
const selectedIds = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const showModal = ref(false)
const showDetailModal = ref(false)
const modalMode = ref<'add' | 'edit'>('add') // 'add' 或 'edit'
const formData = ref({
  id: 0,
  title: '',
  content: '',
  expireTime: new Date().toISOString().slice(0, 16), // 默认当前时间
  isTop: 0,
  publisherId: 1, // 默认发布人ID
  publisherName: '管理员' // 默认发布人名称
})
const detailData = ref<Announcement>({} as Announcement)

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))
const isAllSelected = computed(() => 
  announcements.value.length > 0 && selectedIds.value.length === announcements.value.length
)

const modalTitle = computed(() => 
  modalMode.value === 'add' ? '发布公告' : '编辑公告'
)

// 方法
const loadAnnouncements = async () => {
  try {
    console.log('开始加载公告数据...')
    console.log('当前页:', currentPage.value, '每页条数:', pageSize.value)
    
    // 加载公告
    const result = await getAnnouncementsPage(currentPage.value, pageSize.value, searchKeyword.value, statusFilter.value)
    console.log('后端返回的数据:', result)
    announcements.value = result.records || []
    total.value = result.total || 0
    console.log('加载完成，数据条数:', announcements.value.length, '总记录数:', total.value)
  } catch (error) {
    console.error('加载公告失败:', error)
  }
}

const handleSearch = async () => {
  // 重置到第一页
  currentPage.value = 1
  await loadAnnouncements()
}

const resetSearch = async () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  currentPage.value = 1
  await loadAnnouncements()
}

const selectAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    selectedIds.value = announcements.value.map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

const batchDelete = async () => {
  if (selectedIds.value.length === 0) {
    alert('请先选择要删除的公告')
    return
  }
  
  const deleteCount = selectedIds.value.length
  const confirmed = confirm(`确定要删除选中的 ${deleteCount} 项吗？此操作不可恢复。`)
  if (!confirmed) {
    return
  }
  
  try {
    const promises = selectedIds.value.map(id => deleteAnnouncement(id))
    const results = await Promise.all(promises)
    console.log('批量删除结果:', results)
    if (results.some((result: boolean) => !result)) {
      throw new Error('部分删除操作失败')
    }
    await loadAnnouncements()
    selectedIds.value = []
    alert(`成功删除 ${deleteCount} 项公告`)
  } catch (error) {
    console.error('批量删除失败:', error)
    if (isError(error)) {
      alert('批量删除失败: ' + error.message)
    } else {
      alert('批量删除失败: 未知错误')
    }
  }
}

const toggleTop = async (item: any) => {
  try {
    const newTopStatus = item.isTop === 1 ? 0 : 1
    const result = await updateAnnouncementTop(item.id, newTopStatus)
    console.log('置顶操作结果:', result)
    if (!result) {
      throw new Error('操作失败')
    }
    await loadAnnouncements()
  } catch (error) {
    console.error('置顶操作失败:', error)
    if (isError(error)) {
      alert('操作失败: ' + error.message)
    } else {
      alert('操作失败: 未知错误')
    }
  }
}

const viewDetail = (item: any) => {
  detailData.value = { ...item }
  showDetailModal.value = true
}

const openAddModal = () => {
  modalMode.value = 'add'
  formData.value = {
    id: 0,
    title: '',
    content: '',
    expireTime: new Date().toISOString().slice(0, 16),
    isTop: 0,
    publisherId: 1,
    publisherName: '管理员'
  }
  showModal.value = true
}

const editItem = (item: any) => {
  modalMode.value = 'edit'
  formData.value = { ...item }
  // 转换日期格式
  if (item.expireTime) {
    formData.value.expireTime = new Date(item.expireTime).toISOString().slice(0, 16)
  }
  showModal.value = true
}

const deleteItem = async (id: number) => {
  console.log('=== 开始删除流程 ===')
  console.log('删除ID:', id)
  
  const confirmed = confirm('确定要删除此项吗？删除后将无法恢复。')
  console.log('用户确认结果:', confirmed)
  
  if (!confirmed) {
    console.log('用户取消删除操作')
    return
  }
  
  console.log('开始执行删除API调用')
  try {
    const result = await deleteAnnouncement(id)
    console.log('删除API返回结果:', result)
    if (!result) {
      throw new Error('删除失败')
    }
    console.log('删除成功，开始重新加载数据')
    await loadAnnouncements()
    alert('删除成功')
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
  if (!formData.value.title.trim()) {
    alert('请输入公告标题')
    return
  }
  if (!formData.value.content.trim()) {
    alert('请输入公告内容')
    return
  }
  if (!formData.value.expireTime) {
    alert('请设置过期时间')
    return
  }
  
  try {
    let result
    if (modalMode.value === 'add') {
      result = await createAnnouncement(formData.value)
      console.log('发布结果:', result)
      if (!result) {
        throw new Error('发布失败')
      }
    } else {
      result = await updateAnnouncement(formData.value.id, formData.value)
      console.log('更新结果:', result)
      if (!result) {
        throw new Error('更新失败')
      }
    }
    
    await loadAnnouncements()
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

const closeDetailModal = () => {
  showDetailModal.value = false
}

const goToPage = async (page: number) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  
  currentPage.value = page
  await loadAnnouncements()
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
  return status === 1 ? '有效' : '过期'
}

const getStatusClass = (status: number) => {
  return status === 1 ? 'status-active' : 'status-expired'
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toISOString().slice(0, 19).replace('T', ' ')
}

// 初始化加载数据
onMounted(async () => {
  await loadAnnouncements()
})
</script>

<style scoped>
.announcement-management {
  padding: 20px;
}

.query-section {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input, .status-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.search-input {
  width: 300px;
}

.status-select {
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

.announcement-title {
  position: relative;
  padding-left: 40px;
}

.top-tag {
  position: absolute;
  left: 0;
  top: 0;
  background-color: #f56c6c;
  color: white;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 2px;
  margin-right: 5px;
}

.top-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.top-btn.active {
  color: #e6a23c;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.status-active {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-expired {
  background-color: #fef0f0;
  color: #f56c6c;
}

.btn-view, .btn-edit, .btn-delete {
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

.detail-modal, .announcement-modal {
  width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}

.detail-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  color: #606266;
  flex-wrap: wrap;
}

.detail-content {
  margin-bottom: 20px;
  line-height: 1.6;
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
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
}

.editor-container {
  position: relative;
}

.editor-tips {
  position: absolute;
  bottom: 5px;
  right: 10px;
  font-size: 12px;
  color: #909399;
}

.form-group input[type="checkbox"] {
  width: auto;
  margin-right: 5px;
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
