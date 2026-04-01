<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as donationApi from '../../api/donation'

interface DistributionStory {
  id: number
  title: string
  description: string
  time: string
  details: string
}

const stories = ref<DistributionStory[]>([])
const selectedStory = ref<DistributionStory | null>(null)
const showDetailModal = ref(false)
const searchKeyword = ref('')

const loadStories = async () => {
  try {
    const response = await donationApi.getLoveStories(searchKeyword.value)
    stories.value = response
  } catch (error) {
    console.error('加载爱心事迹失败:', error)
  }
}

const viewDetail = (story: DistributionStory) => {
  selectedStory.value = story
  showDetailModal.value = true
}

const closeModal = () => {
  showDetailModal.value = false
  selectedStory.value = null
}

const handleSearch = () => {
  loadStories()
}

const resetSearch = () => {
  searchKeyword.value = ''
  loadStories()
}

onMounted(() => {
  loadStories()
})
</script>

<template>
  <div class="love-stories">
    <div class="section-header">
      <h2>爱心事迹</h2>
    </div>
    
    <div class="query-section">
      <input 
        v-model="searchKeyword" 
        placeholder="请输入活动名称" 
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <button @click="handleSearch" class="search-btn">查询</button>
      <button @click="resetSearch" class="reset-btn">重置</button>
    </div>
    
    <div class="stories-grid">
      <div 
        v-for="story in stories" 
        :key="story.id"
        class="story-card"
      >
        <h3 class="story-title">{{ story.title }}</h3>
        <p class="story-description">{{ story.description }}</p>
        <div class="story-meta">
          <span class="story-time">{{ story.time }}</span>
          <button @click="viewDetail(story)" class="view-detail-btn">查看详情</button>
        </div>
      </div>
      
      <div v-if="stories.length === 0" class="empty-state">
        <p>暂无爱心事迹</p>
      </div>
    </div>
    
    <!-- 详情模态框 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ selectedStory?.title }}</h3>
          <button class="close-button" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="story-detail-time">{{ selectedStory?.time }}</div>
          <div class="story-detail-content">
            {{ selectedStory?.details }}
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-primary" @click="closeModal">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.love-stories {
  padding: 20px;
}

.query-section {
  display: flex;
  gap: 10px;
  margin: 20px 0;
  flex-wrap: wrap;
}

.search-input {
  width: 300px;
  padding: 8px 12px;
  border: 1px solid #ced4da;
  border-radius: 4px;
  font-size: 14px;
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

.stories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.story-card {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.story-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.story-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.story-description {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.story-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.view-detail-btn {
  background-color: #ff6b81;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.view-detail-btn:hover {
  background-color: #ff4757;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  color: #999;
  background: #f9f9f9;
  border-radius: 8px;
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
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e9ecef;
  text-align: right;
}

@media (max-width: 768px) {
  .query-section {
    flex-direction: column;
  }
  
  .search-input {
    min-width: auto;
  }
  
  .stories-grid {
    grid-template-columns: 1fr;
  }
}
</style>
