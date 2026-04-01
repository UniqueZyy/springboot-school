<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import * as feedbackApi from '../../api/feedback'

interface Feedback {
  id: number
  type: number
  content: string
  status: number
  submitTime: string
  replyContent?: string
  replyTime?: string
}

const userStore = useUserStore()
const feedbacks = ref<Feedback[]>([])
const showAddModal = ref(false)
const newFeedback = ref({
  type: 1,
  content: ''
})

const loadFeedbacks = async () => {
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      console.error('用户未登录')
      return
    }
    
    // 从API获取当前用户的反馈列表
    const result = await feedbackApi.getFeedbacksBySubmitterId(userId)
    console.log('反馈数据:', result)
    if (result) {
      // 使用Number()转换类型进行比较
      feedbacks.value = result.filter((item: any) => Number(item.submitterId) === Number(userId))
    } else {
      feedbacks.value = []
    }
  } catch (error) {
    console.error('加载反馈信息失败:', error)
    feedbacks.value = []
  }
}

const handleAddFeedback = async () => {
  try {
    // 验证表单
    if (!newFeedback.value.type || newFeedback.value.type < 1) {
      alert('请选择反馈类型')
      return
    }
    if (!newFeedback.value.content.trim()) {
      alert('请输入反馈内容')
      return
    }
    if (newFeedback.value.content.trim().length < 5) {
      alert('反馈内容至少需要5个字符')
      return
    }
    
    // 调用API提交反馈
    const result = await feedbackApi.createFeedback({
      submitterId: userStore.userInfo?.id || 1,
      type: Number(newFeedback.value.type),
      content: newFeedback.value.content.trim()
    })
    
    if (result) {
      alert('反馈提交成功！')
      setTimeout(async () => {
        showAddModal.value = false
        resetForm()
        // 重新加载反馈列表
        await loadFeedbacks()
      }, 100)
    } else {
      alert('提交失败，请重试')
    }
  } catch (error) {
    console.error('提交反馈失败:', error)
    alert('提交失败，请重试')
  }
}

const resetForm = () => {
  newFeedback.value = {
    type: 1,
    content: ''
  }
}

const getTypeText = (type: number) => {
  const typeMap = ['', '建议', '问题', '投诉', '其他']
  return typeMap[type] || '其他'
}

const getStatusText = (status: number) => {
  return status === 1 ? '已处理' : '未处理'
}

const getStatusClass = (status: number) => {
  return status === 1 ? 'status-processed' : 'status-unprocessed'
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadFeedbacks()
})
</script>

<template>
  <div class="feedback-submit">
    <div class="section-header">
      <h2>反馈信息</h2>
      <button @click="showAddModal = true" class="btn-primary">提交反馈</button>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>反馈类型</th>
            <th>反馈内容</th>
            <th>提交时间</th>
            <th>状态</th>
            <th>回复内容</th>
            <th>回复时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="feedback in feedbacks" :key="feedback.id">
            <td>{{ getTypeText(feedback.type) }}</td>
            <td>{{ feedback.content }}</td>
            <td>{{ formatDate(feedback.submitTime) }}</td>
            <td>
              <span :class="['status-tag', getStatusClass(feedback.status)]">{{ getStatusText(feedback.status) }}</span>
            </td>
            <td>{{ feedback.replyContent || '-' }}</td>
            <td>{{ feedback.replyTime ? formatDate(feedback.replyTime) : '-' }}</td>
          </tr>
        </tbody>
      </table>
      
      <div v-if="feedbacks.length === 0" class="empty-state">
        <p>暂无反馈记录</p>
      </div>
    </div>
    
    <!-- 提交反馈模态框 -->
    <div v-if="showAddModal" class="modal-overlay" @click="showAddModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>提交反馈</h3>
          <button class="close-button" @click="showAddModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>反馈类型：</label>
            <select v-model="newFeedback.type" class="form-select">
              <option value="1">建议</option>
              <option value="2">问题</option>
              <option value="3">投诉</option>
              <option value="4">其他</option>
            </select>
          </div>
          <div class="form-group">
            <label>反馈内容：</label>
            <textarea v-model="newFeedback.content" class="form-textarea" rows="5" placeholder="请详细描述您的反馈内容..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showAddModal = false" class="btn-secondary">取消</button>
          <button @click="handleAddFeedback" class="btn-primary">提交反馈</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.feedback-submit {
  padding: 20px;
}

.table-container {
  overflow-x: auto;
  margin-top: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  color: #333;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.data-table tbody tr:hover {
  background-color: #f8f9fa;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-processed {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-unprocessed {
  background-color: #fff7e6;
  color: #fa8c16;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  background: #f9f9f9;
  border-radius: 8px;
  margin-top: 20px;
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

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.form-select, .form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-select:focus, .form-textarea:focus {
  outline: none;
  border-color: #ff6b81;
  box-shadow: 0 0 0 2px rgba(255, 107, 129, 0.25);
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
