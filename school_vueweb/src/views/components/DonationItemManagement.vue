<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../../stores/user'
import * as donationRecordApi from '../../api/donationRecord'
import type { ItemDonation } from '../../api/donationRecord'

const userStore = useUserStore()
const allItemDonations = ref<ItemDonation[]>([])
const showAddModal = ref(false)
const showEditModal = ref(false)
const editingItem = ref<ItemDonation | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const newItem = ref({
  itemType: '',
  itemName: '',
  quantity:1,
  remark: ''
})

const itemTypeOptions = [
  { value: '书籍', label: '书籍' },
  { value: '衣物', label: '衣物' },
  { value: '学习用品', label: '学习用品' },
  { value: '生活用品', label: '生活用品' },
  { value: '其他', label: '其他' }
]

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const itemDonations = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return allItemDonations.value.slice(start, end)
})

const loadItemDonations = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      console.error('用户未登录')
      return
    }
    
    const result = await donationRecordApi.getItemDonationsByDonorId(userId)
    console.log('物品捐赠数据:', result)
    if (result) {
      allItemDonations.value = result.filter((item: ItemDonation) => Number(item.donorId) === Number(userId))
      total.value = allItemDonations.value.length
    } else {
      allItemDonations.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载物品捐赠记录失败:', error)
    allItemDonations.value = []
    total.value = 0
  }
}

const handleAddItem = async () => {
  if (!newItem.value.itemType) {
    alert('请选择物品类型')
    return
  }
  if (!newItem.value.itemName.trim()) {
    alert('请输入物品名称')
    return
  }
  if (newItem.value.quantity < 1) {
    alert('捐赠数量至少为1')
    return
  }

  try {
    const result = await donationRecordApi.createItemDonation({
      donorId: userStore.userInfo?.id || 1,
      itemType: newItem.value.itemType,
      itemName: newItem.value.itemName.trim(),
      quantity: Number(newItem.value.quantity),
      remark: newItem.value.remark.trim()
    })
    
    if (result) {
      alert('捐赠提交成功！')
      setTimeout(() => {
        showAddModal.value = false
        resetForm()
        loadItemDonations()
      }, 100)
    } else {
      alert('提交失败，请重试')
    }
  } catch (error) {
    console.error('提交捐赠失败:', error)
    alert('提交失败，请重试')
  }
}

const handleEditItem = async () => {
  if (!editingItem.value) return
  
  if (!editingItem.value.itemType) {
    alert('请选择物品类型')
    return
  }
  if (!editingItem.value.itemName.trim()) {
    alert('请输入物品名称')
    return
  }
  if (editingItem.value.quantity < 1) {
    alert('捐赠数量至少为1')
    return
  }

  try {
    const result = await donationRecordApi.updateItemDonation(editingItem.value.id, {
      itemType: editingItem.value.itemType,
      itemName: editingItem.value.itemName.trim(),
      quantity: Number(editingItem.value.quantity),
      remark: editingItem.value.remark
    })
    
    if (result) {
      alert('修改成功！')
      setTimeout(() => {
        showEditModal.value = false
        editingItem.value = null
        loadItemDonations()
      }, 100)
    } else {
      alert('修改失败，请重试')
    }
  } catch (error) {
    console.error('修改捐赠失败:', error)
    alert('修改失败，请重试')
  }
}

const openEditModal = (item: ItemDonation) => {
  editingItem.value = { ...item }
  showEditModal.value = true
}

const handleCancelDonation = async (item: ItemDonation) => {
  if (!confirm('确定要取消此捐赠吗？')) {
    return
  }
  
  try {
    const result = await donationRecordApi.deleteItemDonation(item.id)
    if (result) {
      alert('取消捐赠成功！')
      loadItemDonations()
    } else {
      alert('取消失败，请重试')
    }
  } catch (error) {
    console.error('取消捐赠失败:', error)
    alert('取消失败，请重试')
  }
}

const resetForm = () => {
  newItem.value = {
    itemType: '',
    itemName: '',
    quantity: 1,
    remark: ''
  }
}

const getItemTypeClass = (type: string) => {
  const classMap: Record<string, string> = {
    '书籍': 'type-book',
    '衣物': 'type-clothes',
    '学习用品': 'type-study',
    '生活用品': 'type-daily',
    '其他': 'type-other'
  }
  return classMap[type] || 'type-other'
}

const getStatusText = (status: number) => {
  const statusMap = ['待审批', '已通过', '已拒绝', '已寄出']
  return statusMap[status] || '未知'
}

const getStatusClass = (status: number) => {
  const classMap = ['status-pending', 'status-approved', 'status-rejected', 'status-shipped']
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
  loadItemDonations()
})
</script>

<template>
  <div class="donation-item-management">
    <div class="section-header">
      <h2>捐赠物品信息</h2>
      <button @click="showAddModal = true" class="btn-primary">去捐赠物品</button>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>捐赠者名称</th>
            <th>物品类型</th>
            <th>物品名称</th>
            <th>捐赠数量</th>
            <th>捐赠日期</th>
            <th>备注</th>
            <th>捐赠状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in itemDonations" :key="item.id">
            <td>{{ item.donorName || userStore.userInfo?.name || '-' }}</td>
            <td>
              <span :class="['type-tag', getItemTypeClass(item.itemType)]">{{ item.itemType }}</span>
            </td>
            <td>{{ item.itemName }}</td>
            <td>{{ item.quantity }}</td>
            <td>{{ formatDate(item.donationDate) }}</td>
            <td>{{ item.remark || '-' }}</td>
            <td>
              <span :class="['status-tag', getStatusClass(item.status)]">{{ getStatusText(item.status) }}</span>
            </td>
            <td>
              <button v-if="item.status === 0" @click="openEditModal(item)" class="btn-edit">修改信息</button>
              <button v-if="item.status === 0" @click="handleCancelDonation(item)" class="btn-delete">取消捐赠</button>
              <span v-else>-</span>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="itemDonations.length === 0" class="empty-state">
        <p>暂无物品捐赠记录</p>
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
    
    <!-- 新增物品捐赠模态框 -->
    <div v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>捐赠物品</h3>
          <button class="close-button" @click="showAddModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>物品类型：</label>
            <select v-model="newItem.itemType" class="form-select">
              <option value="">请选择物品类型</option>
              <option v-for="option in itemTypeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>物品名称：</label>
            <input v-model="newItem.itemName" type="text" placeholder="请输入物品名称" class="form-input" />
          </div>
          <div class="form-group">
            <label>捐赠数量：</label>
            <input v-model.number="newItem.quantity" type="number" min="1" class="form-input" />
          </div>
          <div class="form-group">
            <label>备注：</label>
            <textarea v-model="newItem.remark" class="form-textarea" rows="3" placeholder="请输入备注信息（可选）"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showAddModal = false" class="btn-secondary">取消</button>
          <button @click="handleAddItem" class="btn-primary">提交捐赠</button>
        </div>
      </div>
    </div>

    <!-- 编辑物品捐赠模态框 -->
    <div v-if="showEditModal && editingItem" class="modal-overlay" @click="showEditModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>修改捐赠信息</h3>
          <button class="close-button" @click="showEditModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>物品类型：</label>
            <select v-model="editingItem.itemType" class="form-select">
              <option v-for="option in itemTypeOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>物品名称：</label>
            <input v-model="editingItem.itemName" type="text" class="form-input" />
          </div>
          <div class="form-group">
            <label>捐赠数量：</label>
            <input v-model.number="editingItem.quantity" type="number" min="1" class="form-input" />
          </div>
          <div class="form-group">
            <label>备注：</label>
            <textarea v-model="editingItem.remark" class="form-textarea" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showEditModal = false" class="btn-secondary">取消</button>
          <button @click="handleEditItem" class="btn-primary">确认修改</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.donation-item-management {
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

.type-tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.type-book {
  background: #e3f2fd;
  color: #1976d2;
}

.type-clothes {
  background: #fce4ec;
  color: #c2185b;
}

.type-study {
  background: #f3e5f5;
  color: #7b1fa2;
}

.type-daily {
  background: #e8f5e9;
  color: #388e3c;
}

.type-other {
  background: #fff3e0;
  color: #f57c00;
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

.status-approved {
  background: #e8f5e9;
  color: #388e3c;
}

.status-rejected {
  background: #ffebee;
  color: #d32f2f;
}

.status-shipped {
  background: #e3f2fd;
  color: #1976d2;
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
.form-select,
.form-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-input:focus,
.form-select:focus,
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
