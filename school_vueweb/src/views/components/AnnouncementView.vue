<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as announcementApi from '../../api/announcement'
import type { Announcement } from '../../api/announcement'

const announcements = ref<Announcement[]>([])
const selectedAnnouncement = ref<Announcement | null>(null)
const showDetailModal = ref(false)

const loadAnnouncements = async () => {
  try {
    const result = await announcementApi.getAnnouncementsPage(1, 100)
    announcements.value = result.records || []
  } catch (error) {
    console.error('加载公告失败:', error)
  }
}

const viewDetail = (announcement: Announcement) => {
  selectedAnnouncement.value = announcement
  showDetailModal.value = true
}

const closeModal = () => {
  showDetailModal.value = false
  selectedAnnouncement.value = null
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<template>
  <div class="announcement-view">
    <div class="section-header">
      <h2>公告信息</h2>
    </div>
    
    <div class="announcement-list">
      <div 
        v-for="announcement in announcements" 
        :key="announcement.id"
        class="announcement-item"
        @click="viewDetail(announcement)"
      >
        <div class="announcement-header">
          <h3 class="announcement-title">{{ announcement.title }}</h3>
          <span class="announcement-time">{{ formatDate(announcement.publishTime) }}</span>
        </div>
        <div class="announcement-content">
          {{ announcement.content.substring(0, 100) }}...
        </div>
      </div>
      
      <div v-if="announcements.length === 0" class="empty-state">
        <p>暂无公告信息</p>
      </div>
    </div>
    
    <!-- 公告详情模态框 -->
    <div v-if="showDetailModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ selectedAnnouncement?.title }}</h3>
          <button class="close-button" @click="closeModal">×</button>
        </div>
        <div class="modal-body">
          <div class="announcement-meta">
            <span>发布时间：{{ selectedAnnouncement ? formatDate(selectedAnnouncement.publishTime) : '' }}</span>
            <span v-if="selectedAnnouncement?.expireTime">过期时间：{{ formatDate(selectedAnnouncement.expireTime) }}</span>
          </div>
          <div class="announcement-detail-content">
            {{ selectedAnnouncement?.content }}
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
.announcement-view {
  padding: 20px;
}

.announcement-list {
  margin-top: 20px;
}

.announcement-item {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.announcement-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.announcement-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  flex: 1;
  margin-right: 20px;
}

.announcement-time {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.announcement-content {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.empty-state {
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

.announcement-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #999;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.announcement-detail-content {
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
</style>
