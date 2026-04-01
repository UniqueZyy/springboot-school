<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../../stores/user'
import * as donationRecordApi from '../../api/donationRecord'
import type { FundDonation } from '../../api/donationRecord'

const userStore = useUserStore()
const allFundDonations = ref<FundDonation[]>([])
const showAddModal = ref(false)
const showEditModal = ref(false)
const editingFund = ref<FundDonation | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const newFund = ref({
  amount: 100,
  remark: ''
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const fundDonations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return allFundDonations.value.slice(start, end)
})

const loadFundDonations = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      console.error('用户未登录')
      return
    }
    
    const result = await donationRecordApi.getFundDonationsByDonorId(userId)
    console.log('资金捐赠数据:', result)
    if (result) {
      allFundDonations.value = result.filter((item: FundDonation) => Number(item.donorId) === Number(userId))
      total.value = allFundDonations.value.length
    } else {
      allFundDonations.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载资金捐赠记录失败:', error)
    allFundDonations.value = []
    total.value = 0
  }
}

const handleAddFund = async () => {
  if (newFund.value.amount < 1) {
    alert('捐赠金额至少为1元')
    return
  }

  try {
    const result = await donationRecordApi.createFundDonation({
      donorId: userStore.userInfo?.id || 1,
      amount: Number(newFund.value.amount),
      remark: newFund.value.remark.trim()
    })
    
    if (result) {
      alert('捐赠提交成功！')
      setTimeout(() => {
        showAddModal.value = false
        resetForm()
        loadFundDonations()
      }, 100)
    } else {
      alert('提交失败，请重试')
    }
  } catch (error) {
    console.error('提交捐赠失败:', error)
    alert('提交失败，请重试')
  }
}

const handleEditFund = async () => {
  if (!editingFund.value) return
  
  if (editingFund.value.amount < 1) {
    alert('捐赠金额至少为1元')
    return
  }

  try {
    const result = await donationRecordApi.updateFundDonation(editingFund.value.id, {
      amount: Number(editingFund.value.amount),
      remark: editingFund.value.remark
    })
    
    if (result) {
      alert('修改成功！')
      setTimeout(() => {
        showEditModal.value = false
        editingFund.value = null
        loadFundDonations()
      }, 100)
    } else {
      alert('修改失败，请重试')
    }
  } catch (error) {
    console.error('修改捐赠失败:', error)
    alert('修改失败，请重试')
  }
}

const openEditModal = (fund: FundDonation) => {
  editingFund.value = { ...fund }
  showEditModal.value = true
}

const handleCancelDonation = async (fund: FundDonation) => {
  if (!confirm('确定要取消此捐赠吗？')) {
    return
  }
  
  try {
    const result = await donationRecordApi.deleteFundDonation(fund.id)
    if (result) {
      alert('取消捐赠成功！')
      loadFundDonations()
    } else {
      alert('取消失败，请重试')
    }
  } catch (error) {
    console.error('取消捐赠失败:', error)
    alert('取消失败，请重试')
  }
}

const resetForm = () => {
  newFund.value = {
    amount: 100,
    remark: ''
  }
}

const getStatusText = (status: number) => {
  const statusMap = ['待支付', '已支付', '支付失败']
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  const classMap = ['status-pending', 'status-paid', 'status-failed']
  return classMap[status] || ''
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const goToPage = (page: number) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  currentPage.value = page
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

onMounted(() => {
  loadFundDonations()
})
</script>

<template>
  <div class="donation-fund-management">
    <div class="section-header">
      <h2>捐赠资金信息</h2>
      <button @click="showAddModal = true" class="btn-primary">去捐赠资金</button>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>捐赠者名称</th>
            <th>捐赠金额</th>
            <th>捐赠日期</th>
            <th>备注</th>
            <th>捐赠状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="fund in fundDonations" :key="fund.id">
            <td>{{ fund.donorName || userStore.userInfo?.name || '-' }}</td>
            <td>¥{{ fund.amount.toLocaleString() }}</td>
            <td>{{ formatDate(fund.donationDate) }}</td>
            <td>{{ fund.remark || '-' }}</td>
            <td>
              <span :class="['status-tag', getStatusClass(fund.status)]">{{ getStatusText(fund.status) }}</span>
            </td>
            <td>
              <button v-if="fund.status === 0" @click="openEditModal(fund)" class="btn-edit">修改信息</button>
              <button v-if="fund.status === 0" @click="handleCancelDonation(fund)" class="btn-delete">取消捐赠</button>
              <span v-else>-</span>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="fundDonations.length === 0" class="empty-state">
        <p>暂无资金捐赠记录</p>
      </div>
    </div>
    
    <div v-if="total > 0" class="pagination-section">
      <div class="pagination-info"> 共 {{ total }} 条记录，第 {{ currentPage }} / {{ totalPages }} 页 </div>
      <div class="pagination-controls">
        <button @click="goToPage(1)" :disabled="currentPage === 1" class="page-btn">首页</button>
        <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1" class="page-btn">上一页</button>
        <span class="page-numbers">
          <button v-for="page in getPageNumbers()" :key="page" @click="goToPage(page)" :class="{ active: page === currentPage }" class="page-number">{{ page }}</button>
        </span>
        <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages" class="page-btn">下一页</button>
        <button @click="goToPage(totalPages)" :disabled="currentPage === totalPages" class="page-btn">末页</button>
      </div>
    </div>
    
    <!-- 新增资金捐赠模态框 -->
    <div v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>捐赠资金</h3>
          <button class="close-button" @click="showAddModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>捐赠金额（元）：</label>
            <input v-model.number="newFund.amount" type="number" min="1" step="1" class="form-input" />
          </div>
          <div class="form-group">
            <label>备注：</label>
            <textarea v-model="newFund.remark" class="form-textarea" rows="3" placeholder="请输入备注信息（可选）"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showAddModal = false" class="btn-secondary">取消</button>
          <button @click="handleAddFund" class="btn-primary">提交捐赠</button>
        </div>
      </div>
    </div>

    <!-- 编辑资金捐赠模态框 -->
    <div v-if="showEditModal && editingFund" class="modal-overlay" @click="showEditModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>修改捐赠信息</h3>
          <button class="close-button" @click="showEditModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>捐赠金额（元）：</label>
            <input v-model.number="editingFund.amount" type="number" min="1" step="1" class="form-input" />
          </div>
          <div class="form-group">
            <label>备注：</label>
            <textarea v-model="editingFund.remark" class="form-textarea" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showEditModal = false" class="btn-secondary">取消</button>
          <button @click="handleEditFund" class="btn-primary">确认修改</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.donation-fund-management {
  padding: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #333;
}

.btn-primary {
  background: #ff6b9d;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary:hover {
  background: #ff5a8a;
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

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #666;
}

.data-table tr:hover {
  background: #f8f9fa;
}

.status-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-pending {
  background: #fff3e0;
  color: #f57c00;
}

.status-paid {
  background: #e8f5e9;
  color: #388e3c;
}

.status-failed {
  background: #ffebee;
  color: #d32f2f;
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: #999;
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

.form-input,
.form-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #ff6b9d;
}

.form-textarea {
  resize: vertical;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  text-align: right;
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
