<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ref, onMounted, computed, nextTick } from 'vue'
import * as statisticsApi from '../api/statistics'
import type { 
  DashboardSummary, 
  MonthlyFundData, 
  ItemDonationByType, 
  DonationDistribution, 
  ActivityParticipation 
} from '../api/statistics'
// @ts-ignore
import echarts from '../assets/echarts-wrapper.js'
import ActivityTypeManagement from './components/ActivityTypeManagement.vue'
import ActivityManagement from './components/ActivityManagement.vue'
import ApplicationManagement from './components/ApplicationManagement.vue'
import DonationTypeManagement from './components/DonationTypeManagement.vue'
import DonationApplicationManagement from './components/DonationApplicationManagement.vue'
import DonationDistributionManagement from './components/DonationDistributionManagement.vue'
import AdminManagement from './components/AdminManagement.vue'
import StudentManagement from './components/StudentManagement.vue'
import FeedbackManagement from './components/FeedbackManagement.vue'
import AnnouncementManagement from './components/AnnouncementManagement.vue'

const router = useRouter()
const userStore = useUserStore()
const activeNav = ref('捐赠物资统计')
const expandedMenu = ref<string | null>(null)

const currentUserRole = computed(() => {
  return userStore.userInfo?.roleType || 0
})

const isAdmin = computed(() => {
  return currentUserRole.value === 5
})

const loading = ref(false)
const error = ref<string | null>(null)

const dashboardSummary = ref<DashboardSummary | null>(null)
const monthlyFundData = ref<MonthlyFundData[]>([])
const itemDonationByType = ref<ItemDonationByType[]>([])
const donationDistribution = ref<DonationDistribution | null>(null)
const activityParticipation = ref<ActivityParticipation[]>([])

const lineChartRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
const activityBarChartRef = ref<HTMLElement | null>(null)

let lineChartInstance: any = null
let barChartInstance: any = null
let pieChartInstance: any = null
let activityBarChartInstance: any = null

const lastYearMonthlyData = computed(() => {
  const currentYear = new Date().getFullYear()
  return monthlyFundData.value.filter(d => d.year === currentYear - 1)
})

const currentYearMonthlyData = computed(() => {
  const currentYear = new Date().getFullYear()
  return monthlyFundData.value.filter(d => d.year === currentYear)
})

onMounted(async () => {
  userStore.loadUserInfo()
  if (!userStore.isLoggedIn || !isAdmin.value) {
    router.push('/user-home')
  }
  await loadStatisticsData()
})

const handleLogout = () => {
  userStore.clearUserInfo()
  router.push('/login')
}

const setActiveNav = async (nav: string) => {
  activeNav.value = nav
  if (nav === '反馈信息' || nav === '公告信息') {
    expandedMenu.value = '其他'
  } else if (nav === '管理员信息' || nav === '学生信息') {
    expandedMenu.value = '用户信息'
  } else if (nav === '捐赠物资统计') {
    expandedMenu.value = null
    await nextTick()
    setTimeout(() => {
      initLineChart()
      initBarChart()
      initPieChart()
      initActivityBarChart()
    }, 100)
  } else if (nav.includes('公益活动')) {
    expandedMenu.value = '公益活动管理'
  } else if (nav.includes('捐赠')) {
    expandedMenu.value = '爱心捐赠管理'
  }
}

const toggleMenu = (menu: string) => {
  if (expandedMenu.value === menu) {
    expandedMenu.value = null
  } else {
    expandedMenu.value = menu
  }
}

const loadStatisticsData = async () => {
  loading.value = true
  error.value = null
  
  try {
    const [
      summary,
      monthly,
      itemByType,
      distribution,
      participation
    ] = await Promise.all([
      statisticsApi.getDashboardSummary(),
      statisticsApi.getMonthlyFundDonation(),
      statisticsApi.getItemDonationByType(),
      statisticsApi.getDonationDistribution(),
      statisticsApi.getActivityParticipationByType()
    ])
    
    // 处理数据类型，确保前端能够正确渲染
    if (summary) {
      dashboardSummary.value = {
        totalFundAmount: Number(summary.totalFundAmount) || 0,
        totalItemCount: Number(summary.totalItemCount) || 0,
        totalActivityCount: Number(summary.totalActivityCount) || 0,
        totalApplicationCount: Number(summary.totalApplicationCount) || 0,
        totalStudentCount: Number(summary.totalStudentCount) || 0
      }
    }
    
    if (monthly) {
      monthlyFundData.value = monthly.map((item: any) => ({
        year: Number(item.year) || 0,
        month: Number(item.month) || 0,
        label: String(item.label || ''),
        amount: Number(item.amount) || 0
      }))
    }
    
    if (itemByType) {
      itemDonationByType.value = itemByType.map((item: any) => ({
        itemType: String(item.itemType || '其他'),
        quantity: Number(item.quantity) || 0
      }))
    }
    
    if (distribution) {
      donationDistribution.value = {
        distribution: distribution.distribution || [],
        totalFundAmount: Number(distribution.totalFundAmount) || 0,
        totalItemCount: Number(distribution.totalItemCount) || 0
      }
    }
    
    if (participation) {
      activityParticipation.value = participation.map((item: any) => ({
        typeId: Number(item.typeId) || 0,
        typeName: String(item.typeName || '未分类'),
        participantCount: Number(item.participantCount) || 0
      }))
    }
    
    validateDataConsistency()
    
    await initAllCharts()
    
  } catch (err) {
    console.error('加载统计数据失败:', err)
    error.value = '加载数据失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const validateDataConsistency = () => {
  if (!dashboardSummary.value || !donationDistribution.value) return
  
  const summaryFund = dashboardSummary.value.totalFundAmount
  const distributionFund = donationDistribution.value.totalFundAmount
  
  if (Math.abs(summaryFund - distributionFund) > 0.01) {
    console.warn('数据一致性警告: 捐赠资金总额不一致', {
      summary: summaryFund,
      distribution: distributionFund
    })
  }
  
  const summaryItems = dashboardSummary.value.totalItemCount
  const distributionItems = donationDistribution.value.totalItemCount
  
  if (summaryItems !== distributionItems) {
    console.warn('数据一致性警告: 捐赠物品总数不一致', {
      summary: summaryItems,
      distribution: distributionItems
    })
  }
}

const initLineChart = () => {
  if (!lineChartRef.value) return

  console.log('=== 线图数据调试 ===')
  console.log('monthlyFundData:', monthlyFundData.value)
  console.log('lastYearMonthlyData:', lastYearMonthlyData.value)

  if (!lastYearMonthlyData.value.length) {
    console.warn('没有上一年度的捐赠数据')
    return
  }

  if (lineChartInstance) {
    lineChartInstance.dispose()
  }

  lineChartInstance = echarts.init(lineChartRef.value)

  const months = lastYearMonthlyData.value.map(d => d.month + '月')
  const amounts = lastYearMonthlyData.value.map(d => d.amount)

  console.log('月份数据:', months)
  console.log('金额数据:', amounts)

  const option = {
    title: {
      text: '上一年度每月捐赠资金变化',
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 14,
        color: '#666'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: function(params: any) {
        return params[0].name + '<br/>捐赠金额: ¥' + params[0].value.toFixed(2)
      }
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: {
        fontSize: 10,
        interval: 0
      },
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '金额(元)',
      nameTextStyle: {
        fontSize: 10
      },
      axisLabel: {
        fontSize: 10,
        formatter: function(value: number) {
          if (value >= 1000) {
            return (value / 1000).toFixed(1) + 'k'
          }
          return value
        }
      }
    },
    series: [{
      data: amounts,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: {
        color: '#1890ff'
      },
      lineStyle: {
        width: 3
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0, color: 'rgba(24, 144, 255, 0.4)'
          }, {
            offset: 1, color: 'rgba(24, 144, 255, 0.05)'
          }]
        }
      },
      label: {
        show: true,
        position: 'top',
        formatter: function(params: any) {
          if (params.value > 0) {
            return '¥' + params.value.toFixed(0)
          }
          return ''
        },
        fontSize: 9,
        color: '#1890ff'
      }
    }],
    grid: {
      left: '15%',
      right: '10%',
      bottom: '15%',
      top: '20%'
    }
  }

  lineChartInstance.setOption(option)
}

const initBarChart = () => {
  if (!barChartRef.value) return

  console.log('=== 物品捐赠柱状图数据调试 ===')
  console.log('itemDonationByType:', itemDonationByType.value)

  if (!itemDonationByType.value.length) {
    console.warn('没有物品捐赠数据')
    return
  }

  if (barChartInstance) {
    barChartInstance.dispose()
  }

  barChartInstance = echarts.init(barChartRef.value)

  const types = itemDonationByType.value.map(d => d.itemType)
  const quantities = itemDonationByType.value.map(d => d.quantity)

  console.log('物品种类:', types)
  console.log('数量:', quantities)

  const option = {
    title: {
      text: '不同种类捐赠物品数量',
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 14,
        color: '#666'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params: any) {
        return params[0].name + '<br/>数量: ' + params[0].value + '件'
      }
    },
    xAxis: {
      type: 'category',
      data: types,
      axisLabel: {
        fontSize: 11,
        interval: 0,
        rotate: 0
      },
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: '数量(件)',
      nameTextStyle: {
        fontSize: 10
      },
      axisLabel: {
        fontSize: 10
      }
    },
    series: [{
      data: quantities,
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: function(params: any) {
          const colors = ['#ff6b81', '#ffa502', '#2ed573', '#1e90ff', '#a55eea']
          return colors[params.dataIndex % colors.length]
        },
        borderRadius: [4, 4, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}件',
        fontSize: 10,
        color: '#666'
      }
    }],
    grid: {
      left: '15%',
      right: '10%',
      bottom: '15%',
      top: '20%'
    }
  }

  barChartInstance.setOption(option)
}

const initPieChart = () => {
  if (!pieChartRef.value || !donationDistribution.value) return

  if (pieChartInstance) {
    pieChartInstance.dispose()
  }

  pieChartInstance = echarts.init(pieChartRef.value)

  const pieData = donationDistribution.value.distribution.map((item: any) => ({
    name: item.name,
    value: item.value
  }))

  const option = {
    title: {
      text: '捐赠分布',
      left: 'center',
      top: 5,
      textStyle: {
        fontSize: 14,
        color: '#666'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: function(params: any) {
        if (params.name === '捐赠资金') {
          return params.name + ': ¥' + params.value.toFixed(2) + ' (' + params.percent + '%)'
        }
        return params.name + ': ' + params.value + '件 (' + params.percent + '%)'
      }
    },
    legend: {
      orient: 'horizontal',
      bottom: 5,
      left: 'center',
      itemGap: 30,
      itemWidth: 15,
      itemHeight: 10,
      textStyle: {
        fontSize: 11,
        padding: [0, 0, 0, 5]
      }
    },
    series: [{
      type: 'pie',
      radius: ['30%', '50%'],
      center: ['50%', '50%'],
      data: pieData,
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2,
        color: (params: any) => {
          const colors = ['#1890ff', '#52c41a', '#ff6b81', '#ffa502']
          return colors[params.dataIndex % colors.length]
        }
      },
      label: {
        show: true,
        position: 'outside',
        formatter: function(params: any) {
          if (params.name === '捐赠资金') {
            return '{name|' + params.name + '}\n{value|¥' + params.value.toFixed(0) + '}'
          }
          return '{name|' + params.name + '}\n{value|' + params.value + '件}'
        },
        rich: {
          name: {
            fontSize: 11,
            color: '#666',
            lineHeight: 16
          },
          value: {
            fontSize: 10,
            color: '#999',
            lineHeight: 14
          }
        }
      },
      labelLine: {
        show: true,
        length: 10,
        length2: 8,
        smooth: true
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 13,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }],
    grid: {
      left: '5%',
      right: '5%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    }
  }

  pieChartInstance.setOption(option)
}

const initActivityBarChart = () => {
  if (!activityBarChartRef.value) return

  console.log('=== 活动参与人数柱状图数据调试 ===')
  console.log('activityParticipation:', activityParticipation.value)

  if (!activityParticipation.value.length) {
    console.warn('没有活动参与数据')
    return
  }

  if (activityBarChartInstance) {
    activityBarChartInstance.dispose()
  }

  activityBarChartInstance = echarts.init(activityBarChartRef.value)

  const types = activityParticipation.value.map(d => d.typeName)
  const counts = activityParticipation.value.map(d => d.participantCount)

  console.log('活动类型:', types)
  console.log('参与人数:', counts)

  const option = {
    title: {
      text: '不同种类志愿活动参加人数',
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 14,
        color: '#666'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params: any) {
        return params[0].name + '<br/>参与人数: ' + params[0].value + '人'
      }
    },
    xAxis: {
      type: 'category',
      data: types,
      axisLabel: {
        fontSize: 11,
        interval: 0,
        rotate: 0
      },
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: '人数(人)',
      nameTextStyle: {
        fontSize: 10
      },
      axisLabel: {
        fontSize: 10
      },
      minInterval: 1
    },
    series: [{
      data: counts,
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: function(params: any) {
          const colors = ['#ff6b81', '#ffa502', '#2ed573', '#1e90ff', '#a55eea', '#ff4757']
          return colors[params.dataIndex % colors.length]
        },
        borderRadius: [4, 4, 0, 0]
      },
      label: {
        show: true,
        position: 'top',
        formatter: '{c}人',
        fontSize: 10,
        color: '#666'
      }
    }],
    grid: {
      left: '15%',
      right: '10%',
      bottom: '15%',
      top: '20%'
    }
  }
  
  activityBarChartInstance.setOption(option)
}

const initAllCharts = async () => {
  await nextTick()
  
  setTimeout(() => {
    initLineChart()
    initBarChart()
    initPieChart()
    initActivityBarChart()
    
    window.addEventListener('resize', () => {
      lineChartInstance?.resize()
      barChartInstance?.resize()
      pieChartInstance?.resize()
      activityBarChartInstance?.resize()
    })
  }, 100)
}

const formatAmount = (amount: number) => {
  if (amount >= 10000) {
    return (amount / 10000).toFixed(1) + '万'
  }
  return amount.toFixed(0)
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
          <span>{{ userStore.userInfo?.name || '管理员' }}</span>
          <button @click="handleLogout" class="logout-button">退出登录</button>
        </div>
      </div>
    </header>
    
    <div class="admin-content">
      <aside class="sidebar">
        <ul class="nav-menu">
          <li class="nav-item" :class="{ active: activeNav === '捐赠物资统计' }" @click="setActiveNav('捐赠物资统计')">
            <div class="nav-item-content">
              <img src="../assets/柱形图.svg" alt="捐赠物资统计" class="nav-icon" />
              <span>捐赠物资统计</span>
            </div>
          </li>
          
          <li class="nav-item nav-parent">
            <div class="nav-item-content" @click="toggleMenu('公益活动管理')">
              <img src="../assets/旗帜1-01.svg" alt="公益活动管理" class="nav-icon" />
              <span>公益活动管理</span>
              <span class="nav-arrow" :class="{ active: expandedMenu === '公益活动管理' }">▼</span>
            </div>
            <ul class="nav-dropdown" v-if="expandedMenu === '公益活动管理'">
              <li class="nav-dropdown-item" :class="{ active: activeNav === '公益活动类型' }" @click="setActiveNav('公益活动类型')">
                <span>公益活动类型</span>
              </li>
              <li class="nav-dropdown-item" :class="{ active: activeNav === '公益活动信息' }" @click="setActiveNav('公益活动信息')">
                <span>公益活动信息</span>
              </li>
              <li class="nav-dropdown-item" :class="{ active: activeNav === '公益活动加入申请' }" @click="setActiveNav('公益活动加入申请')">
                <span>公益活动加入申请</span>
              </li>
            </ul>
          </li>
          
          <li class="nav-item nav-parent">
            <div class="nav-item-content" @click="toggleMenu('爱心捐赠管理')">
              <img src="../assets/爱心箱.svg" alt="爱心捐赠管理" class="nav-icon" />
              <span>爱心捐赠管理</span>
              <span class="nav-arrow" :class="{ active: expandedMenu === '爱心捐赠管理' }">▼</span>
            </div>
            <ul class="nav-dropdown" v-if="expandedMenu === '爱心捐赠管理'">
              <li class="nav-dropdown-item" :class="{ active: activeNav === '捐赠物品类型信息' }" @click="setActiveNav('捐赠物品类型信息')">
                <span>捐赠物品类型信息</span>
              </li>
              <li class="nav-dropdown-item" :class="{ active: activeNav === '捐赠申请信息' }" @click="setActiveNav('捐赠申请信息')">
                <span>捐赠申请信息</span>
              </li>
              <li class="nav-dropdown-item" :class="{ active: activeNav === '捐赠物资分配' }" @click="setActiveNav('捐赠物资分配')">
                <span>捐赠物资分配</span>
              </li>
            </ul>
          </li>
          
          <li class="nav-item nav-parent">
            <div class="nav-item-content" @click="toggleMenu('用户信息')">
              <img src="../assets/用户.svg" alt="用户信息" class="nav-icon" />
              <span>用户信息</span>
              <span class="nav-arrow" :class="{ active: expandedMenu === '用户信息' }">▼</span>
            </div>
            <ul class="nav-dropdown" v-if="expandedMenu === '用户信息'">
              <li class="nav-dropdown-item" :class="{ active: activeNav === '管理员信息' }" @click="setActiveNav('管理员信息')">
                <span>管理员信息</span>
              </li>
              <li class="nav-dropdown-item" :class="{ active: activeNav === '学生信息' }" @click="setActiveNav('学生信息')">
                <span>学生信息</span>
              </li>
            </ul>
          </li>
          
          <li class="nav-item nav-parent">
            <div class="nav-item-content" @click="toggleMenu('其他')">
              <img src="../assets/其他.svg" alt="其他" class="nav-icon" />
              <span>其他</span>
              <span class="nav-arrow" :class="{ active: expandedMenu === '其他' }">▼</span>
            </div>
            <ul class="nav-dropdown" v-if="expandedMenu === '其他'">
              <li class="nav-dropdown-item" :class="{ active: activeNav === '反馈信息' }" @click="setActiveNav('反馈信息')">
                <span>反馈信息</span>
              </li>
              <li class="nav-dropdown-item" :class="{ active: activeNav === '公告信息' }" @click="setActiveNav('公告信息')">
                <span>公告信息</span>
              </li>
            </ul>
          </li>
        </ul>
      </aside>
      
      <main class="main-content">
        <div v-if="activeNav === '捐赠物资统计'">
          <div v-if="loading" class="loading-container">
            <div class="loading-spinner"></div>
            <p>正在加载数据...</p>
          </div>
          
          <div v-else-if="error" class="error-container">
            <p class="error-message">{{ error }}</p>
            <button @click="loadStatisticsData" class="retry-button">重新加载</button>
          </div>
          
          <template v-else>
            <div class="data-cards">
              <div class="data-card">
                <div class="card-icon">
                  <img src="../assets/profitable_unclear.svg" alt="捐赠金额" />
                </div>
                <div class="card-content">
                  <div class="card-value">¥{{ formatAmount(dashboardSummary?.totalFundAmount || 0) }}</div>
                  <div class="card-label">捐赠金额</div>
                </div>
              </div>
              <div class="data-card">
                <div class="card-icon">
                  <img src="../assets/021-hand.svg" alt="捐赠物资" />
                </div>
                <div class="card-content">
                  <div class="card-value">{{ dashboardSummary?.totalItemCount || 0 }}</div>
                  <div class="card-label">捐赠物资</div>
                </div>
              </div>
              <div class="data-card">
                <div class="card-icon">
                  <img src="../assets/flag.svg" alt="公益活动" />
                </div>
                <div class="card-content">
                  <div class="card-value">{{ dashboardSummary?.totalActivityCount || 0 }}</div>
                  <div class="card-label">公益活动</div>
                </div>
              </div>
              <div class="data-card">
                <div class="card-icon">
                  <img src="../assets/donate.svg" alt="活动申请" />
                </div>
                <div class="card-content">
                  <div class="card-value">{{ dashboardSummary?.totalApplicationCount || 0 }}</div>
                  <div class="card-label">活动申请</div>
                </div>
              </div>
              <div class="data-card">
                <div class="card-icon">
                  <img src="../assets/022boss.svg" alt="学生总人数" />
                </div>
                <div class="card-content">
                  <div class="card-value">{{ dashboardSummary?.totalStudentCount || 0 }}</div>
                  <div class="card-label">学生总人数</div>
                </div>
              </div>
            </div>
            
            <div class="charts-grid">
              <div class="chart-section">
                <div class="section-header">
                  <h2>上一年度每月捐赠资金变化线图</h2>
                </div>
                <div class="chart-wrapper">
                  <div ref="lineChartRef" class="echarts-container"></div>
                </div>
              </div>
              
              <div class="chart-section">
                <div class="section-header">
                  <h2>不同种类捐赠物品数量柱状图</h2>
                </div>
                <div class="chart-wrapper">
                  <div ref="barChartRef" class="echarts-container"></div>
                </div>
              </div>
              
              <div class="chart-section">
                <div class="section-header">
                  <h2>捐赠资金与捐赠物品分配饼状图</h2>
                </div>
                <div class="chart-wrapper">
                  <div ref="pieChartRef" class="echarts-container"></div>
                </div>
              </div>
              
              <div class="chart-section">
                <div class="section-header">
                  <h2>不同种类志愿活动参加人数柱状图</h2>
                </div>
                <div class="chart-wrapper">
                  <div ref="activityBarChartRef" class="echarts-container"></div>
                </div>
              </div>
            </div>
          </template>
        </div>
        
        <div class="content-section" v-if="activeNav === '公益活动类型'">
          <ActivityTypeManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '公益活动信息'">
          <ActivityManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '公益活动加入申请'">
          <ApplicationManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '捐赠物品类型信息'">
          <DonationTypeManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '捐赠申请信息'">
          <DonationApplicationManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '捐赠物资分配'">
          <DonationDistributionManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '管理员信息'">
          <AdminManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '学生信息'">
          <StudentManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '反馈信息'">
          <FeedbackManagement />
        </div>
        
        <div class="content-section" v-if="activeNav === '公告信息'">
          <AnnouncementManagement />
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

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #ff6b81;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.error-message {
  color: #f56c6c;
  font-size: 16px;
  margin-bottom: 20px;
}

.retry-button {
  background-color: #ff6b81;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.retry-button:hover {
  background-color: #ff4757;
}

.data-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.data-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.card-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.card-label {
  font-size: 14px;
  color: #666;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

.chart-section {
  background: white;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.section-header h2 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.chart-subtitle {
  font-size: 12px;
  color: #999;
}

.chart-wrapper {
  min-height: 200px;
}

.echarts-container {
  width: 100%;
  height: 250px;
}

.content-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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
