<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import * as donationApi from '../../api/donation'

interface DonationRecord {
  id: number
  donorId: number
  donorName: string
  donationType: string
  itemType?: string
  itemName?: string
  quantity?: number
  amount?: number
  donationDate: string
  remark: string
  status: number
  type: 'item' | 'fund'
}

const donations = ref<DonationRecord[]>([])
const loading = ref(false)
const error = ref('')
const searchKeyword = ref('')
const selectedDonationType = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const showApprovalModal = ref(false)
const currentDonation = ref<DonationRecord | null>(null)
const approvalComment = ref('')

const itemTypes = [
  { value: '书籍', label: '书籍' },
  { value: '衣物', label: '衣物' },
  { value: '学习用品', label: '学习用品' },
  { value: '生活用品', label: '生活用品' },
  { value: '其他', label: '其他' }
]

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const loadDonations = async () => {
  loading.value = true
  error.value = ''
  try {
    const [itemResponse, fundResponse] = await Promise.all([
      donationApi.getItemDonationsPage(1, 1000, searchKeyword.value, selectedDonationType.value === 'fund' ? undefined : selectedDonationType.value),
      donationApi.getFundDonationsPage(1, 1000, searchKeyword.value)
    ])
    
    const itemDonations: DonationRecord[] = (itemResponse.records || []).map((item: any) => ({
      id: item.id,
      donorId: item.donorId,
      donorName: item.donorName || '匿名',
      donationType: '物品捐赠',
      itemType: item.itemType,
      itemName: item.itemName,
      quantity: item.quantity,
      donationDate: item.donationDate,
      remark: item.remark || '',
      status: item.status,
      type: 'item'
    }))
    
    const fundDonations: DonationRecord[] = (fundResponse.records || []).map((fund: any) => ({
      id: fund.id,
      donorId: fund.donorId,
      donorName: fund.donorName || '匿名',
      donationType: '资金捐赠',
      amount: fund.amount,
      donationDate: fund.donationDate,
      remark: fund.remark || '',
      status: fund.status,
      type: 'fund'
    }))
    
    let allDonations = [...itemDonations, ...fundDonations]
    
    if (selectedDonationType.value === 'item') {
      allDonations = itemDonations
    } else if (selectedDonationType.value === 'fund') {
      allDonations = fundDonations
    }
    
    allDonations.sort((a, b) => new Date(b.donationDate).getTime() - new Date(a.donationDate).getTime())
    
    total.value = allDonations.length
    
    const startIndex = (currentPage.value - 1) * pageSize.value
    const endIndex = startIndex + pageSize.value
    donations.value = allDonations.slice(startIndex, endIndex)
  } catch (err) {
    error.value = '加载捐赠记录失败'
    console.error('加载捐赠记录失败:', err)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadDonations()
}

const handleReset = () => {
  searchKeyword.value = ''
  selectedDonationType.value = ''
  currentPage.value = 1
  loadDonations()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadDonations()
}

const goToPage = (page: number) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  currentPage.value = page
  loadDonations()
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

const openApprovalModal = (donation: DonationRecord) => {
  currentDonation.value = donation
  approvalComment.value = ''
  showApprovalModal.value = true
}

const closeApprovalModal = () => {
  showApprovalModal.value = false
  currentDonation.value = null
  approvalComment.value = ''
}

const handleApprove = async () => {
  if (!currentDonation.value) return
  
  try {
    if (currentDonation.value.type === 'item') {
      await donationApi.approveItemDonation(currentDonation.value.id)
    } else {
      await donationApi.payFundDonation(currentDonation.value.id)
    }
    alert('审批成功！')
    closeApprovalModal()
    loadDonations()
  } catch (err) {
    error.value = '审批失败'
    console.error('审批失败:', err)
  }
}

const handleReject = async () => {
  if (!currentDonation.value) return
  
  if (!confirm('确定要拒绝该捐赠吗？')) return
  
  try {
    if (currentDonation.value.type === 'item') {
      await donationApi.rejectItemDonation(currentDonation.value.id)
    }
    alert('已拒绝该捐赠')
    closeApprovalModal()
    loadDonations()
  } catch (err) {
    error.value = '拒绝失败'
    console.error('拒绝失败:', err)
  }
}

const getStatusText = (donation: DonationRecord) => {
  if (donation.type === 'item') {
    const statusMap = ['待审批', '已通过', '已拒绝', '已寄出']
    return statusMap[donation.status] || '未知'
  } else {
    const statusMap = ['待支付', '已支付', '支付失败']
    return statusMap[donation.status] || '未知'
  }
}

const getStatusClass = (donation: DonationRecord) => {
  if (donation.type === 'item') {
    const classMap = ['status-pending', 'status-approved', 'status-rejected', 'status-shipped']
    return classMap[donation.status] || ''
  } else {
    const classMap = ['status-pending', 'status-paid', 'status-failed']
    return classMap[donation.status] || ''
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.split('T')[0]
}

onMounted(() => {
  loadDonations()
})
</script>

<template>
  <div class="donation-application-management">
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        type="text" 
        placeholder="请输入捐赠者名称进行搜索" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="selectedDonationType" class="search-select">
        <option value="">全部类型</option>
        <option value="item">物品捐赠</option>
        <option value="fund">资金捐赠</option>
      </select>
      <button @click="handleSearch" class="search-btn">搜索</button>
      <button @click="handleReset" class="reset-btn">重置</button>
    </div>
    
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    
    <div v-if="loading" class="loading">
      加载中...
    </div>
    
    <div v-else class="data-section">
      <table class="data-table">
        <thead>
          <tr>
            <th>捐赠者名称</th>
            <th>捐赠类型</th>
            <th>捐赠物品</th>
            <th>捐赠资金</th>
            <th>物品类型</th>
            <th>捐赠日期</th>
            <th>备注</th>
            <th>捐赠状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="donation in donations" :key="`${donation.type}-${donation.id}`">
            <td>{{ donation.donorName }}</td>
            <td>{{ donation.donationType }}</td>
            <td>{{ donation.type === 'item' ? donation.itemName : '-' }}</td>
            <td>{{ donation.type === 'fund' ? `¥${donation.amount}` : '-' }}</td>
            <td>{{ donation.type === 'item' ? donation.itemType : '-' }}</td>
            <td>{{ formatDate(donation.donationDate) }}</td>
            <td>{{ donation.remark || '-' }}</td>
            <td>
              <span :class="['status-tag', getStatusClass(donation)]">
                {{ getStatusText(donation) }}
              </span>
            </td>
            <td>
              <button 
                v-if="(donation.type === 'item' && donation.status === 0) || (donation.type === 'fund' && donation.status === 0)" 
                @click="openApprovalModal(donation)" 
                class="btn-edit"
              >
                去审批
              </button>
            </td>
          </tr>
          <tr v-if="donations.length === 0">
            <td colspan="9" class="no-data">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <div v-if="totalPages > 1" class="pagination-section">
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
    
    <div v-if="showApprovalModal" class="modal-overlay" @click="closeApprovalModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>审批确认</h3>
          <button @click="closeApprovalModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body" v-if="currentDonation">
          <div class="info-row">
            <label>捐赠者名称：</label>
            <span>{{ currentDonation.donorName }}</span>
          </div>
          <div class="info-row">
            <label>捐赠类型：</label>
            <span>{{ currentDonation.donationType }}</span>
          </div>
          <div v-if="currentDonation.type === 'item'" class="info-row">
            <label>捐赠物品：</label>
            <span>{{ currentDonation.itemName }}</span>
          </div>
          <div v-if="currentDonation.type === 'item'" class="info-row">
            <label>物品类型：</label>
            <span>{{ currentDonation.itemType }}</span>
          </div>
          <div v-if="currentDonation.type === 'item'" class="info-row">
            <label>数量：</label>
            <span>{{ currentDonation.quantity }}</span>
          </div>
          <div v-if="currentDonation.type === 'fund'" class="info-row">
            <label>捐赠金额：</label>
            <span>¥{{ currentDonation.amount }}</span>
          </div>
          <div class="info-row">
            <label>捐赠日期：</label>
            <span>{{ formatDate(currentDonation.donationDate) }}</span>
          </div>
          <div class="info-row">
            <label>备注：</label>
            <span>{{ currentDonation.remark || '无' }}</span>
          </div>
          <div class="info-row">
            <label>审批意见：</label>
            <textarea 
              v-model="approvalComment" 
              placeholder="请输入审批意见（可选）" 
              class="comment-input"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="handleReject" class="btn-reject">拒绝</button>
          <button @click="handleApprove" class="btn-approve">通过</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.donation-application-management {
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

.search-select {
  width: 150px;
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

.no-data {
  text-align: center;
  color: #999;
  padding: 40px;
}

.status-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-pending {
  background-color: #fff7e6;
  color: #fa8c16;
}

.status-approved {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-rejected {
  background-color: #fff1f0;
  color: #f5222d;
}

.status-shipped {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-paid {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-failed {
  background-color: #fff1f0;
  color: #f5222d;
}

.btn-edit {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  background-color: #409eff;
  color: white;
}

.btn-edit:hover {
  background-color: #66b1ff;
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

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 0;
}

.info-row {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-row label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #606266;
}

.info-row span {
  color: #606266;
  font-size: 14px;
}

.comment-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
  color: #606266;
  font-size: 14px;
  resize: vertical;
  min-height: 80px;
  font-family: inherit;
}

.comment-input:focus {
  border-color: #409eff;
  outline: none;
}

.modal-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.modal-footer button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-footer .btn-approve {
  background-color: #409eff;
  color: white;
}

.modal-footer .btn-reject {
  background-color: #909399;
  color: white;
}
</style>
