<template>
  <div class="donation-distribution-management">
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入标题或关键词进行搜索" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <button @click="handleSearch" class="search-btn">搜索</button>
      <button @click="resetSearch" class="reset-btn">重置</button>
    </div>

    <div class="action-section">
      <button @click="openAddModal" class="add-btn">新增</button>
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
            <th>标题</th>
            <th>分配对象</th>
            <th>物资类型</th>
            <th>分配数量</th>
            <th>分配时间</th>
            <th>物资分配详细</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="story in distributions" :key="story.id">
            <td>
              <input 
                type="checkbox" 
                :value="story.id"
                v-model="selectedIds"
              />
            </td>
            <td>{{ story.title || story.distributionObject }}</td>
            <td>{{ story.distributionObject }}</td>
            <td>{{ story.donationTypeName || '-' }}</td>
            <td>{{ story.quantity }}</td>
            <td>{{ formatDate(story.time || story.distributionTime) }}</td>
            <td>
              <button @click="viewDetail(story)" class="btn-view">查看详情</button>
            </td>
            <td>
              <button @click="editItem(story)" class="btn-edit">编辑</button>
              <button @click="deleteItem(story.id)" class="btn-delete">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="distributions.length === 0" class="empty-state">
        <p v-if="loading">加载中...</p>
        <p v-else-if="error">{{ error }}</p>
        <p v-else>暂无爱心事迹分配记录</p>
      </div>
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

    <div v-if="showDetailModal" class="modal-overlay" @click="closeDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ selectedStory?.title || selectedStory?.distributionObject }}</h3>
          <button class="close-button" @click="closeDetailModal">×</button>
        </div>
        <div class="modal-body">
          <div class="story-detail-time">{{ formatDate(selectedStory?.time || selectedStory?.distributionTime) }}</div>
          <div class="story-detail-content">
            {{ selectedStory?.details || selectedStory?.reason }}
          </div>
          <div v-if="selectedStory?.donationTypeName" class="story-detail-meta">
            <strong>物资类型：</strong>{{ selectedStory.donationTypeName }}
          </div>
          <div v-if="selectedStory?.quantity" class="story-detail-meta">
            <strong>分配数量：</strong>{{ selectedStory.quantity }}
          </div>
          <div v-if="selectedStory?.operatorName" class="story-detail-meta">
            <strong>操作人：</strong>{{ selectedStory.operatorName }}
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-primary" @click="closeDetailModal">关闭</button>
        </div>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="close-button" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>标题:</label>
            <input v-model="formData.title" type="text" placeholder="请输入爱心事迹标题" required />
          </div>
          <div class="form-group">
            <label>物资类型:</label>
            <select v-model="formData.typeId" required>
              <option value="">请选择物资类型</option>
              <option v-for="type in donationTypes" :key="type.id" :value="type.id">
                {{ type.typeName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>分配数量:</label>
            <input v-model.number="formData.quantity" type="number" min="1" placeholder="请输入分配数量" required />
          </div>
          <div class="form-group">
            <label>分配对象:</label>
            <input v-model="formData.distributionObject" type="text" placeholder="请输入分配对象" required />
          </div>
          <div class="form-group">
            <label>事迹描述:</label>
            <textarea v-model="formData.description" placeholder="请输入事迹描述" rows="3" required></textarea>
          </div>
          <div class="form-group">
            <label>详细内容:</label>
            <textarea v-model="formData.details" placeholder="请输入详细内容" rows="5" required></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeModal" class="btn-secondary">取消</button>
          <button @click="submitForm" class="btn-primary">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import * as donationApi from '../../api/donation'

interface DonationDistribution {
  id: number
  title?: string
  description?: string
  details?: string
  time?: string
  typeId: number
  donationTypeName?: string
  quantity: number
  distributionObject: string
  reason: string
  distributionTime: string
  operatorId: number
  operatorName?: string
}

const distributions = ref<DonationDistribution[]>([])
const donationTypes = ref<any[]>([])
const searchKeyword = ref('')
const selectedIds = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const loading = ref(false)
const error = ref('')
const showModal = ref(false)
const showDetailModal = ref(false)
const modalMode = ref<'add' | 'edit'>('add')
const selectedStory = ref<DonationDistribution | null>(null)

const formData = ref({
  id: 0,
  title: '',
  description: '',
  details: '',
  typeId: 0,
  quantity: 1,
  distributionObject: '',
  reason: '',
  operatorId: 1
})

const isAllSelected = computed(() => 
  distributions.value.length > 0 && selectedIds.value.length === distributions.value.length
)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const modalTitle = computed(() => 
  modalMode.value === 'add' ? '新增爱心事迹' : '编辑爱心事迹'
)

const loadDonationTypes = async () => {
  try {
    const types = await donationApi.getDonationTypes()
    donationTypes.value = types
  } catch (error) {
    console.error('加载捐赠物资类型失败:', error)
  }
}

const loadDistributions = async () => {
  loading.value = true
  error.value = ''
  try {
    console.log('开始加载爱心事迹分配数据...')
    const result = await donationApi.getDonationDistributionsPage(currentPage.value, pageSize.value, searchKeyword.value)
    console.log('后端返回的数据:', result)
    
    distributions.value = (result.records || []).map((item: any) => {
      const donationType = donationTypes.value.find(type => type.id === item.typeId)
      return {
        id: item.id,
        title: item.title || item.distributionObject,
        description: item.description || item.reason,
        details: item.details || item.reason,
        time: item.time || item.distributionTime,
        typeId: item.typeId,
        donationTypeName: item.donationTypeName || donationType?.typeName || '-',
        quantity: item.quantity,
        distributionObject: item.distributionObject,
        reason: item.reason,
        distributionTime: item.distributionTime,
        operatorId: item.operatorId,
        operatorName: item.operatorName
      }
    })
    
    total.value = result.total || 0
    console.log('加载完成，数据条数:', distributions.value.length, '总记录数:', total.value)
  } catch (err) {
    error.value = '加载爱心事迹分配记录失败'
    console.error('加载爱心事迹分配记录失败:', err)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  currentPage.value = 1
  await loadDistributions()
}

const resetSearch = async () => {
  searchKeyword.value = ''
  currentPage.value = 1
  await loadDistributions()
}

const selectAll = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.checked) {
    selectedIds.value = distributions.value.map(item => item.id)
  } else {
    selectedIds.value = []
  }
}

const viewDetail = (story: DonationDistribution) => {
  selectedStory.value = story
  showDetailModal.value = true
}

const closeDetailModal = () => {
  showDetailModal.value = false
  selectedStory.value = null
}

const openAddModal = () => {
  modalMode.value = 'add'
  formData.value = {
    id: 0,
    title: '',
    description: '',
    details: '',
    typeId: 0,
    quantity: 1,
    distributionObject: '',
    reason: '',
    operatorId: 1
  }
  showModal.value = true
}

const editItem = (item: DonationDistribution) => {
  modalMode.value = 'edit'
  formData.value = {
    id: item.id,
    title: item.title || item.distributionObject,
    description: item.description || item.reason,
    details: item.details || item.reason,
    typeId: item.typeId,
    quantity: item.quantity,
    distributionObject: item.distributionObject,
    reason: item.reason,
    operatorId: item.operatorId
  }
  showModal.value = true
}

const deleteItem = async (id: number) => {
  const confirmed = window.confirm('确定要删除此项吗？')
  if (!confirmed) {
    return
  }
  
  try {
    const result = await donationApi.deleteDonationDistribution(id)
    console.log('删除结果:', result)
    if (!result) {
      throw new Error('删除失败')
    }
    await loadDistributions()
    selectedIds.value = []
  } catch (err) {
    error.value = '删除失败'
    console.error('删除失败:', err)
  }
}

const batchDelete = async () => {
  if (selectedIds.value.length === 0) return
  
  const confirmed = window.confirm(`确定要删除选中的 ${selectedIds.value.length} 项吗？`)
  if (!confirmed) {
    return
  }
  
  try {
    const promises = selectedIds.value.map(id => donationApi.deleteDonationDistribution(id))
    const results = await Promise.all(promises)
    console.log('批量删除结果:', results)
    if (results.some(result => !result)) {
      throw new Error('部分删除操作失败')
    }
    await loadDistributions()
    selectedIds.value = []
  } catch (err) {
    error.value = '批量删除失败'
    console.error('批量删除失败:', err)
  }
}

const submitForm = async () => {
  if (!formData.value.title.trim()) {
    alert('请输入标题')
    return
  }
  if (!formData.value.typeId) {
    alert('请选择物资类型')
    return
  }
  if (!formData.value.quantity || formData.value.quantity < 1) {
    alert('请输入有效的分配数量')
    return
  }
  if (!formData.value.distributionObject.trim()) {
    alert('请输入分配对象')
    return
  }
  if (!formData.value.description.trim()) {
    alert('请输入事迹描述')
    return
  }
  if (!formData.value.details.trim()) {
    alert('请输入详细内容')
    return
  }
  
  try {
    const distributionData = {
      id: formData.value.id,
      typeId: formData.value.typeId,
      quantity: formData.value.quantity,
      distributionObject: formData.value.distributionObject,
      reason: formData.value.details,
      operatorId: formData.value.operatorId
    }
    
    let result
    if (modalMode.value === 'add') {
      result = await donationApi.createDonationDistribution(distributionData)
      console.log('新增结果:', result)
      if (!result) {
        throw new Error('新增失败')
      }
    } else {
      result = await donationApi.updateDonationDistribution(formData.value.id, distributionData)
      console.log('更新结果:', result)
      if (!result) {
        throw new Error('更新失败')
      }
    }
    
    await loadDistributions()
    closeModal()
  } catch (err) {
    error.value = '保存失败'
    console.error('保存失败:', err)
  }
}

const closeModal = () => {
  showModal.value = false
}

const goToPage = async (page: number) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  
  currentPage.value = page
  await loadDistributions()
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

const formatDate = (dateString: string | undefined) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

onMounted(async () => {
  await loadDonationTypes()
  await loadDistributions()
})
</script>

<style scoped>
.donation-distribution-management {
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

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  background: #f9f9f9;
  border-radius: 8px;
}

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
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.close-button {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  transition: color 0.3s;
}

.close-button:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.story-detail-time {
  font-size: 14px;
  color: #999;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.story-detail-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  white-space: pre-line;
  margin-bottom: 20px;
}

.story-detail-meta {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
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
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e9ecef;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn-primary, .btn-secondary {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-primary {
  background-color: #409eff;
  color: white;
}

.btn-primary:hover {
  background-color: #66b1ff;
}

.btn-secondary {
  background-color: #909399;
  color: white;
}

.btn-secondary:hover {
  background-color: #a6a9ad;
}

@media (max-width: 768px) {
  .query-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input {
    width: 100%;
  }
  
  .pagination-section {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
