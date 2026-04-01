<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as donationApi from '../../api/donation'
import * as publicActivityApi from '../../api/publicActivity'

interface RankingItem {
  id: number
  name: string
  amount?: number
  quantity?: number
  times?: number
  rank: number
}

const donationRanking = ref<RankingItem[]>([])
const itemRanking = ref<RankingItem[]>([])
const activityRanking = ref<RankingItem[]>([])

const loadDonationRanking = async () => {
  try {
    const response = await donationApi.getDonationRanking()
    donationRanking.value = response
  } catch (error) {
    console.error('加载捐款榜单失败:', error)
  }
}

const loadItemRanking = async () => {
  try {
    const response = await donationApi.getItemRanking()
    itemRanking.value = response
  } catch (error) {
    console.error('加载捐物榜单失败:', error)
  }
}

const loadActivityRanking = async () => {
  try {
    const response = await donationApi.getActivityRanking()
    activityRanking.value = response
  } catch (error) {
    console.error('加载活动参与榜单失败:', error)
  }
}

onMounted(() => {
  loadDonationRanking()
  loadItemRanking()
  loadActivityRanking()
})
</script>

<template>
  <div class="love-ranking">
    <div class="section-header">
      <h2>爱心榜单</h2>
    </div>
    
    <div class="ranking-container">
      <!-- 爱心捐款榜单 -->
      <div class="ranking-card">
        <div class="ranking-header">
          <h3><span class="ranking-icon">🏆</span> 爱心捐款榜单（前十名）</h3>
        </div>
        <div class="ranking-list">
          <div 
            v-for="item in donationRanking" 
            :key="item.id"
            class="ranking-item"
            :class="{ 'top-3': item.rank <= 3 }"
          >
            <div class="rank-number">{{ item.rank }}</div>
            <div class="rank-info">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-value">捐款金额：¥{{ item.amount?.toLocaleString() }}元</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 爱心捐物榜单 -->
      <div class="ranking-card">
        <div class="ranking-header">
          <h3><span class="ranking-icon">👑</span> 爱心捐物榜单（前十名）</h3>
        </div>
        <div class="ranking-list">
          <div 
            v-for="item in itemRanking" 
            :key="item.id"
            class="ranking-item"
            :class="{ 'top-3': item.rank <= 3 }"
          >
            <div class="rank-number">{{ item.rank }}</div>
            <div class="rank-info">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-value">捐赠件数：{{ item.quantity }}件</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 参加公益活动榜单 -->
      <div class="ranking-card">
        <div class="ranking-header">
          <h3><span class="ranking-icon">⭐</span> 参加公益活动榜单（前十名）</h3>
        </div>
        <div class="ranking-list">
          <div 
            v-for="item in activityRanking" 
            :key="item.id"
            class="ranking-item"
            :class="{ 'top-3': item.rank <= 3 }"
          >
            <div class="rank-number">{{ item.rank }}</div>
            <div class="rank-info">
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-value">参加次数：{{ item.times }}次</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.love-ranking {
  padding: 20px;
}

.ranking-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.ranking-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.ranking-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #ff6b81;
}

.ranking-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.ranking-icon {
  font-size: 20px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 6px;
  transition: all 0.3s;
}

.ranking-item:hover {
  background: #f0f0f0;
  transform: translateX(5px);
}

.ranking-item.top-3 {
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
  border: 1px solid #ffd700;
}

.rank-number {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #ff6b81;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  margin-right: 15px;
  flex-shrink: 0;
}

.ranking-item.top-3 .rank-number {
  background: #ffd700;
}

.rank-info {
  flex: 1;
}

.rank-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.rank-value {
  font-size: 12px;
  color: #666;
}

@media (max-width: 768px) {
  .ranking-container {
    grid-template-columns: 1fr;
  }
}
</style>
