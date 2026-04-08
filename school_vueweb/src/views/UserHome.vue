<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ref, onMounted } from 'vue'


// 导入新的组件
import AnnouncementView from './components/AnnouncementView.vue'
import LoveRanking from './components/LoveRanking.vue'
import LoveStories from './components/LoveStories.vue'
import ActivityBrowse from './components/ActivityBrowse.vue'
import ActivityApplicationManagement from './components/ActivityApplicationManagement.vue'
import DonationItemManagement from './components/DonationItemManagement.vue'
import DonationFundManagement from './components/DonationFundManagement.vue'
import FeedbackSubmit from './components/FeedbackSubmit.vue'
import UserProfile from './components/UserProfile.vue'

const router = useRouter()
const userStore = useUserStore()
const activeNav = ref('公告信息')
const expandedMenu = ref<string | null>(null)


// 初始化用户信息
onMounted(() => {
  userStore.loadUserInfo()
  if (!userStore.isLoggedIn) {
    router.push('/login')
  }
})

const handleLogout = () => {
  userStore.clearUserInfo()
  router.push('/login')
}

const setActiveNav = (nav: string) => {
  activeNav.value = nav
  if (nav === '反馈信息' || nav === '公告信息') {
    expandedMenu.value = '其他'
  } else if (nav.includes('公益活动')) {
    expandedMenu.value = '公益活动'
  } else if (nav.includes('捐赠物品管理') || nav.includes('捐赠资金管理')) {
    expandedMenu.value = '捐赠申请信息'
  } else {
    expandedMenu.value = null
  }
}

const toggleMenu = (menu: string) => {
  if (expandedMenu.value === menu) {
    expandedMenu.value = null
  } else {
    expandedMenu.value = menu
  }
}
</script>

<template>
  <div class="home-container">
    <header class="admin-header">
      <div class="header-left">
        <h1>高校爱心捐赠管理系统</h1>
        <div class="breadcrumb">
          <span>首页&nbsp;&nbsp;/&nbsp;&nbsp;</span>
          <span class="breadcrumb-active">{{ activeNav }}</span>
        </div>
      </div>
      <div class="header-right">
        <div class="user-info">
          <span>{{ userStore.userInfo?.name || '用户' }}</span>
          <button @click="handleLogout" class="logout-button">退出登录</button>
        </div>
      </div>
    </header>
    
    <div class="admin-content">
      <aside class="sidebar">
        <ul class="nav-menu">

          
          <li class="nav-item" :class="{ active: activeNav === '公告信息' }" @click="setActiveNav('公告信息')">
            <div class="nav-item-content">
              <img src="../assets/其他.svg" alt="公告信息" class="nav-icon" />
              <span>公告信息</span>
            </div>
          </li>
          
          <li class="nav-item" :class="{ active: activeNav === '爱心榜单' }" @click="setActiveNav('爱心榜单')">
            <div class="nav-item-content">
              <img src="../assets/爱心箱.svg" alt="爱心榜单" class="nav-icon" />
              <span>爱心榜单</span>
            </div>
          </li>
          
          <li class="nav-item" :class="{ active: activeNav === '爱心事迹' }" @click="setActiveNav('爱心事迹')">
            <div class="nav-item-content">
              <img src="../assets/捐赠物品@2x.svg" alt="爱心事迹" class="nav-icon" />
              <span>爱心事迹</span>
            </div>
          </li>
          
          <li class="nav-item nav-parent">
            <div class="nav-item-content" @click="toggleMenu('公益活动')">
              <img src="../assets/旗帜1-01.svg" alt="公益活动" class="nav-icon" />
              <span>公益活动</span>
              <span class="nav-arrow" :class="{ active: expandedMenu === '公益活动' }">▼</span>
            </div>
            <ul class="nav-dropdown" v-if="expandedMenu === '公益活动'">
              <li class="nav-dropdown-item" :class="{ active: activeNav === '公益活动浏览' }" @click="setActiveNav('公益活动浏览')">
                <span>公益活动浏览</span>
              </li>
              <li class="nav-dropdown-item" :class="{ active: activeNav === '公益活动申请管理' }" @click="setActiveNav('公益活动申请管理')">
                <span>公益活动申请管理</span>
              </li>
            </ul>
          </li>
          
          <li class="nav-item nav-parent">
            <div class="nav-item-content" @click="toggleMenu('捐赠申请信息')">
              <img src="../assets/爱心箱.svg" alt="捐赠申请信息" class="nav-icon" />
              <span>捐赠申请信息</span>
              <span class="nav-arrow" :class="{ active: expandedMenu === '捐赠申请信息' }">▼</span>
            </div>
            <ul class="nav-dropdown" v-if="expandedMenu === '捐赠申请信息'">
              <li class="nav-dropdown-item" :class="{ active: activeNav === '捐赠物品管理' }" @click="setActiveNav('捐赠物品管理')">
                <span>捐赠物品管理</span>
              </li>
              <li class="nav-dropdown-item" :class="{ active: activeNav === '捐赠资金管理' }" @click="setActiveNav('捐赠资金管理')">
                <span>捐赠资金管理</span>
              </li>
            </ul>
          </li>
          
          <li class="nav-item" :class="{ active: activeNav === '反馈信息' }" @click="setActiveNav('反馈信息')">
            <div class="nav-item-content">
              <img src="../assets/其他.svg" alt="反馈信息" class="nav-icon" />
              <span>反馈信息</span>
            </div>
          </li>
          
          <li class="nav-item" :class="{ active: activeNav === '个人信息' }" @click="setActiveNav('个人信息')">
            <div class="nav-item-content">
              <img src="../assets/用户.svg" alt="个人信息" class="nav-icon" />
              <span>个人信息</span>
            </div>
          </li>
        </ul>
      </aside>
      
      <main class="main-content">

        
        <div class="content-section" v-if="activeNav === '公告信息'">
          <AnnouncementView />
        </div>
        
        <div class="content-section" v-if="activeNav === '爱心榜单'">
          <LoveRanking />
        </div>
        
        <div class="content-section" v-if="activeNav === '爱心事迹'">
          <LoveStories />
        </div>
        
        <div class="content-section" v-if="activeNav === '公益活动浏览'">
          <ActivityBrowse />
        </div>
        
        <div class="content-section" v-if="activeNav === '公益活动申请管理'">
          <ActivityApplicationManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '捐赠物品管理'">
          <DonationItemManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '捐赠资金管理'">
          <DonationFundManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '反馈信息'">
          <FeedbackSubmit />
        </div>
        
        <div class="content-section" v-if="activeNav === '个人信息'">
          <UserProfile />
        </div>
        
        <div class="content-section" v-if="activeNav !== '公告信息' && activeNav !== '爱心榜单' && activeNav !== '爱心事迹' && activeNav !== '公益活动浏览' && activeNav !== '公益活动申请管理' && activeNav !== '捐赠物品管理' && activeNav !== '捐赠资金管理' && activeNav !== '反馈信息' && activeNav !== '个人信息'">
          <div class="section-header">
            <h2>{{ activeNav }}</h2>
            <button class="btn-primary">添加新记录</button>
          </div>
          <div class="content-placeholder">
            <p>这里显示{{ activeNav }}的详细数据和操作</p>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.admin-header {
  background-color: #ff6b81;
  color: white;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-left h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.breadcrumb {
  font-size: 14px;
  opacity: 0.9;
  display: flex;
  align-items: center;
}

.breadcrumb-active {
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.admin-content {
  display: flex;
  min-height: calc(100vh - 70px);
}

.sidebar {
  width: 200px;
  background-color: #f8f9fa;
  border-right: 1px solid #e9ecef;
  padding: 20px 0;
}

.nav-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  border-left: 3px solid transparent;
  color: #333;
}

.nav-item-content {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  gap: 10px;
}

.nav-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.nav-arrow {
  margin-left: auto;
  font-size: 12px;
  transition: transform 0.3s;
}

.nav-arrow.active {
  transform: rotate(180deg);
}

.nav-item:hover {
  background-color: #e9ecef;
}

.nav-item.active {
  background-color: #ff6b81;
  color: white;
  border-left-color: #ff4757;
}

.nav-dropdown {
  list-style: none;
  padding: 0;
  margin: 0;
  background-color: #f8f9fa;
  border-left: 3px solid transparent;
}

.nav-dropdown-item {
  padding: 10px 20px 10px 40px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 13px;
  color: #333;
}

.nav-dropdown-item:hover {
  background-color: #e9ecef;
  padding-left: 45px;
}

.nav-dropdown-item.active {
  background-color: #ff6b81;
  color: white;
  border-left-color: #ff4757;
}

.main-content {
  flex: 1;
  padding: 30px;
  background-color: #f5f5f5;
  overflow-y: auto;
}

.content-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.btn-primary {
  background-color: #ff6b81;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn-primary:hover {
  background-color: #ff4757;
}

.content-placeholder {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.logout-button {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.logout-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
}
</style>
