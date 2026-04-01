<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useUserStore } from '../../stores/user'
import * as publicActivityApi from '../../api/publicActivity'
import type { PublicActivity } from '../../api/publicActivity'

const userStore = useUserStore()
const allActivities = ref<PublicActivity[]>([])
const searchKeyword = ref('')
const activityStatus = ref('')
const currentPage = ref(1)
const total = ref(0)

const getItemsPerPage = () => {
  const screenWidth = window.innerWidth
  const containerPadding = 40
  const gap = 20
  const cardMinWidth = 350
  const availableWidth = screenWidth - containerPadding
  
  let itemsPerRow = Math.floor((availableWidth + gap) / (cardMinWidth + gap))
  itemsPerRow = Math.max(1, Math.min(itemsPerRow, 6))
  
  return itemsPerRow * 2
}

const pageSize = ref(getItemsPerPage())

const totalPages = computed(() => {
  return Math.ceil(total.value / pageSize.value)
})

const activities = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return allActivities.value.slice(start, end)
})

const handleResize = () => {
  const newPageSize = getItemsPerPage()
  if (newPageSize !== pageSize.value) {
    pageSize.value = newPageSize
    if (currentPage.value > totalPages.value && totalPages.value > 0) {
      currentPage.value = totalPages.value
    }
  }
}

const loadActivities = async () => {
  try {
    const result = await publicActivityApi.getActivities()
    let activityList = result || []
    
    activityList.sort((a: PublicActivity, b: PublicActivity) => {
      const statusOrder: Record<number, number> = { 1: 0, 0: 1, 2: 2, 3: 3 }
      const statusA = Number(a.status)
      const statusB = Number(b.status)
      return (statusOrder[statusA] ?? 999) - (statusOrder[statusB] ?? 999)
    })
    
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      activityList = activityList.filter((activity: PublicActivity) => {
        return (activity.activityName || '').toLowerCase().includes(keyword)
      })
    }
    
    if (activityStatus.value !== '') {
      const status = Number(activityStatus.value)
      activityList = activityList.filter((activity: PublicActivity) => {
        return Number(activity.status) === status
      })
    }
    
    allActivities.value = activityList
    total.value = activityList.length
  } catch (error) {
    console.error('加载活动失败:', error)
  }
}

const handleJoinActivity = async (activityId: number) => {
  try {
    await publicActivityApi.applyActivity({
      userId: userStore.userInfo?.id || 0,
      activityId,
      applicationReason: '我想参加这个公益活动',
      status: 0,
      applyTime: new Date().toISOString(),
      approveTime: null,
      approverId: null,
      approveRemark: null
    })
    alert('报名成功！')
    loadActivities()
  } catch (error) {
    console.error('报名失败:', error)
    alert('报名失败，请重试')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadActivities()
}

const resetSearch = () => {
  searchKeyword.value = ''
  activityStatus.value = ''
  currentPage.value = 1
  loadActivities()
}

const goToPage = (page: number) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  currentPage.value = page
  loadActivities()
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
  const statusMap = ['未开始', '进行中', '已结束', '已取消']
  if (status === undefined || status === null) return '未开始'
  return statusMap[status] ?? '未开始'
}

const getStatusClass = (status: number) => {
  const classMap = ['status-not-started', 'status-ongoing', 'status-ended', 'status-cancelled']
  return classMap[status] || ''
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadActivities()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="activity-browse">
    <div class="section-header">
      <h2>公益活动浏览</h2>
    </div>
    
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入活动名称" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <select v-model="activityStatus" class="status-select">
        <option value="">请选择活动状态</option>
        <option value="0">未开始</option>
        <option value="1">进行中</option>
        <option value="2">已结束</option>
        <option value="3">已取消</option>
      </select>
      <button @click="handleSearch" class="search-btn">查询</button>
      <button @click="resetSearch" class="reset-btn">重置</button>
    </div>
    
    <div class="activities-grid">
      <div 
        v-for="activity in activities" 
        :key="activity.id"
        class="activity-card"
      >
        <h3 class="activity-title">公益活动：{{ activity.activityName }}</h3>
        <div class="activity-info">
          <p><strong>活动类型：</strong>{{ activity.typeName }}</p>
          <p><strong>负责人：</strong>未知</p>
          <p><strong>负责人联系方式：</strong>未提供</p>
          <p><strong>详细信息：</strong>{{ activity.description }}</p>
          <p><strong>剩余可预约人数：</strong>{{ activity.maxParticipants ? activity.maxParticipants - (activity.currentParticipants || 0) : '无限制' }}</p>
          <p><strong>开始时间：</strong>{{ formatDate(activity.startTime) }}</p>
          <p><strong>结束时间：</strong>{{ formatDate(activity.endTime) }}</p>
          <p><strong>状态：</strong><span :class="['status-tag', getStatusClass(activity.status)]">{{ getStatusText(activity.status) }}</span></p>
        </div>
        <button 
          v-if="activity.status === 1" 
          @click="handleJoinActivity(activity.id)" 
          class="join-btn"
        >
          加入活动
        </button>
        <button 
          v-else 
          class="join-btn disabled"
          disabled
        >
          {{ activity.status === 0 ? '活动未开始' : activity.status === 2 ? '活动已结束' : '活动已取消' }}
        </button>
      </div>
      
      <div v-if="activities.length === 0" class="empty-state">
        <p>暂无公益活动</p>
      </div>
    </div>
    
    <!-- 分页组件 -->
    <div v-if="total > 0" class="pagination-section">
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
.activity-browse {
  padding: 20px;
}

.query-section {
  display: flex;
  gap: 10px;
  margin: 20px 0;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  width: 300px;
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
}

.status-select {
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
  background: white;
  min-width: 150px;
}

.search-btn, .reset-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.search-btn {
  background-color: #ff6b81;
  color: white;
}

.search-btn:hover {
  background-color: #ff4757;
}

.reset-btn {
  background-color: #6c757d;
  color: white;
}

.reset-btn:hover {
  background-color: #5a6268;
}

.activities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.activity-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.activity-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.activity-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.activity-info {
  flex: 1;
  margin-bottom: 20px;
}

.activity-info p {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.activity-info p strong {
  color: #333;
}

.status-tag {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-not-started {
  background-color: #e6f7ff;
  color: #1890ff;
}

.status-ongoing {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-ended {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.status-cancelled {
  background-color: #f5f5f5;
  color: #666;
}

.join-btn {
  background-color: #ff6b81;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
  align-self: flex-start;
}

.join-btn:hover {
  background-color: #ff4757;
}

.join-btn.disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: #999;
  background: #f9f9f9;
  border-radius: 8px;
}

.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.pagination-info {
  font-size: 14px;
  color: #666;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background-color: #f8f9fa;
  border-color: #adb5bd;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-number {
  padding: 6px 10px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.page-number:hover {
  background-color: #f8f9fa;
  border-color: #adb5bd;
}

.page-number.active {
  background-color: #ff6b81;
  color: white;
  border-color: #ff6b81;
}

@media (max-width: 768px) {
  .query-section {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input, .status-select {
    min-width: auto;
  }
  
  .activities-grid {
    grid-template-columns: 1fr;
  }
  
  .pagination-section {
    flex-direction: column;
    gap: 10px;
    align-items: stretch;
  }
  
  .pagination-controls {
    justify-content: center;
  }
}
</style>
