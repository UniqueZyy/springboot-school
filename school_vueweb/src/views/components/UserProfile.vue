<template>
  <div class="user-profile">
    <div class="profile-header">
      <h2>个人信息</h2>
    </div>
    
    <div class="profile-content">
      <!-- 用户基本信息 -->
      <div class="user-info-card">
        <h3>基本信息</h3>
        <div class="info-item">
          <span class="label">姓名：</span>
          <span class="value">{{ userInfo.name || '未知' }}</span>
        </div>
        <div class="info-item">
          <span class="label">用户名：</span>
          <span class="value">{{ userInfo.username || '未知' }}</span>
        </div>
        <div class="info-item">
          <span class="label">角色：</span>
          <span class="value">{{ getUserRoleName(userInfo.roleType) }}</span>
        </div>

      </div>
      
      <!-- 数据统计概览 -->
      <div class="stats-overview">
        <h3>捐赠统计</h3>
        <div class="stats-cards">
          <div class="stat-card">
            <div class="card-value">{{ userStats.totalFundAmount || 0 }}元</div>
            <div class="card-label">总捐赠金额</div>
          </div>
          <div class="stat-card">
            <div class="card-value">{{ userStats.totalItemCount || 0 }}件</div>
            <div class="card-label">总捐赠物品</div>
          </div>
          <div class="stat-card">
            <div class="card-value">{{ userStats.totalActivityCount || 0 }}次</div>
            <div class="card-label">参与活动</div>
          </div>
          <div class="stat-card">
            <div class="card-value">{{ userStats.totalDonationCount || 0 }}次</div>
            <div class="card-label">总捐赠次数</div>
          </div>
        </div>
      </div>
      
      <!-- 数据可视化图表 -->
      <div class="charts-container">
        <h3>详细数据图</h3>
        <div class="charts-grid">
          <!-- 当月捐赠资金折线图 -->
          <div class="chart-section">
            <div class="section-header">
              <h4>当月捐赠资金变化</h4>
            </div>
            <div class="chart-wrapper">
              <div ref="fundChart" class="chart"></div>
            </div>
          </div>
          
          <!-- 不同种类捐赠物品数量柱状图 -->
          <div class="chart-section">
            <div class="section-header">
              <h4>捐赠物品类型分布</h4>
            </div>
            <div class="chart-wrapper">
              <div ref="itemChart" class="chart"></div>
            </div>
          </div>
          
          <!-- 参与不同种类支援活动数量柱形图 -->
          <div class="chart-section">
            <div class="section-header">
              <h4>参与活动类型分布</h4>
            </div>
            <div class="chart-wrapper">
              <div ref="activityChart" class="chart"></div>
            </div>
          </div>
          
          <!-- 捐赠资金与捐赠物品分配饼图 -->
          <div class="chart-section">
            <div class="section-header">
              <h4>捐赠类型占比</h4>
            </div>
            <div class="chart-wrapper">
              <div ref="distributionChart" class="chart"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '../../stores/user'
import * as statisticsApi from '../../api/statistics'
import type { UserVO } from '../../api/auth'

// 声明全局echarts对象
declare const echarts: any

// 扩展UserVO类型，添加createTime和lastLoginTime属性
interface ExtendedUserVO extends UserVO {
  createTime?: string
  lastLoginTime?: string
}

const userStore = useUserStore()
const userInfo = computed<ExtendedUserVO>(() => userStore.userInfo as ExtendedUserVO || {} as ExtendedUserVO)

// 图表引用
const fundChart = ref<HTMLElement>()
const itemChart = ref<HTMLElement>()
const activityChart = ref<HTMLElement>()
const distributionChart = ref<HTMLElement>()

// 用户统计数据
const userStats = ref({
  totalFundAmount: 0,
  totalItemCount: 0,
  totalActivityCount: 0,
  totalDonationCount: 0
})

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 获取用户角色名称
const getUserRoleName = (roleType: number) => {
  const roleMap: Record<number, string> = {
    1: '学生',
    2: '教师',
    3: '校友',
    4: '校外爱心人士',
    5: '管理员'
  }
  return roleMap[roleType] || '未知'
}

// 初始化图表
const initCharts = async () => {
  const userId = userInfo.value.id
  if (!userId) {
    console.error('用户ID不存在')
    return
  }
  
  try {
    // 获取数据
    const [monthlyFund, itemByType, activityParticipation, donationDistribution] = await Promise.all([
      statisticsApi.getUserMonthlyFundDonation(userId),
      statisticsApi.getUserItemDonationByType(userId),
      statisticsApi.getUserActivityParticipation(userId),
      statisticsApi.getUserDonationDistribution(userId)
    ])
    
    // 当月捐赠资金折线图
    if (fundChart.value) {
      const chart = echarts.init(fundChart.value)
      const fundLabels = monthlyFund.map(item => item.label)
      const fundData = monthlyFund.map(item => item.amount)
      
      chart.setOption({
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: fundLabels,
          axisLabel: {
            interval: 0,
            rotate: 45
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '捐赠金额',
          type: 'line',
          stack: 'Total',
          data: fundData,
          itemStyle: {
            color: '#5470c6'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(84, 112, 198, 0.8)' },
              { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
            ])
          }
        }]
      })
    }
    
    // 不同种类捐赠物品数量柱状图
    if (itemChart.value) {
      const chart = echarts.init(itemChart.value)
      const itemLabels = itemByType.map(item => item.itemType)
      const itemData = itemByType.map(item => item.quantity)
      
      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: itemLabels
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '捐赠数量',
          type: 'bar',
          data: itemData,
          itemStyle: {
            color: function(params) {
              const colors = ['#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272']
              return colors[params.dataIndex % colors.length]
            }
          }
        }]
      })
    }
    
    // 参与不同种类支援活动数量柱形图
    if (activityChart.value) {
      const chart = echarts.init(activityChart.value)
      const activityLabels = activityParticipation.map(item => item.typeName)
      const activityData = activityParticipation.map(item => item.participantCount)
      
      chart.setOption({
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: activityLabels
        },
        yAxis: {
          type: 'value'
        },
        series: [{
          name: '参与次数',
          type: 'bar',
          data: activityData,
          itemStyle: {
            color: function(params) {
              const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666']
              return colors[params.dataIndex % colors.length]
            }
          }
        }]
      })
    }
    
    // 捐赠资金与捐赠物品分配饼图
    if (distributionChart.value) {
      const chart = echarts.init(distributionChart.value)
      const distributionData = donationDistribution.distribution
      
      chart.setOption({
        tooltip: {
          trigger: 'item'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          name: '捐赠类型',
          type: 'pie',
          radius: '50%',
          data: distributionData,
          label: {
            show: true,
            formatter: '{b}: {d}%'
          },
          itemStyle: {
            color: function(params) {
              const colors = ['#5470c6', '#91cc75']
              return colors[params.dataIndex % colors.length]
            }
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      })
    }
  } catch (error) {
    console.error('加载图表数据失败:', error)
    // 加载失败时使用模拟数据
    loadMockCharts()
  }
}

// 加载模拟图表数据
const loadMockCharts = () => {
  // 模拟数据
  const fundData = [100, 200, 150, 300, 250, 400, 350]
  const fundLabels = ['1日', '5日', '10日', '15日', '20日', '25日', '30日']
  
  const itemData = [10, 5, 8, 3, 2]
  const itemLabels = ['书籍', '衣物', '食品', '文具', '其他']
  
  const activityData = [5, 3, 2, 4]
  const activityLabels = ['环保活动', '助学活动', '敬老活动', '其他活动']
  
  const distributionData = [60, 40]
  const distributionLabels = ['资金捐赠', '物品捐赠']
  
  // 当月捐赠资金折线图
  if (fundChart.value) {
    const chart = echarts.init(fundChart.value)
    chart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: fundLabels,
        axisLabel: {
          interval: 0,
          rotate: 45
        }
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        name: '捐赠金额',
        type: 'line',
        stack: 'Total',
        data: fundData,
        itemStyle: {
          color: '#5470c6'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(84, 112, 198, 0.8)' },
            { offset: 1, color: 'rgba(84, 112, 198, 0.1)' }
          ])
        }
      }]
    })
  }
  
  // 不同种类捐赠物品数量柱状图
  if (itemChart.value) {
    const chart = echarts.init(itemChart.value)
    chart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: itemLabels
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        name: '捐赠数量',
        type: 'bar',
        data: itemData,
        itemStyle: {
          color: function(params) {
            const colors = ['#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272']
            return colors[params.dataIndex % colors.length]
          }
        }
      }]
    })
  }
  
  // 参与不同种类支援活动数量柱形图
  if (activityChart.value) {
    const chart = echarts.init(activityChart.value)
    chart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: activityLabels
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        name: '参与次数',
        type: 'bar',
        data: activityData,
        itemStyle: {
          color: function(params) {
            const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666']
            return colors[params.dataIndex % colors.length]
          }
        }
      }]
    })
  }
  
  // 捐赠资金与捐赠物品分配饼图
  if (distributionChart.value) {
    const chart = echarts.init(distributionChart.value)
    chart.setOption({
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [{
        name: '捐赠类型',
        type: 'pie',
        radius: '50%',
        data: [
          { value: distributionData[0], name: distributionLabels[0] },
          { value: distributionData[1], name: distributionLabels[1] }
        ],
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        itemStyle: {
          color: function(params) {
            const colors = ['#5470c6', '#91cc75']
            return colors[params.dataIndex % colors.length]
          }
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }
}

// 加载用户数据
const loadUserData = async () => {
  try {
    const userId = userInfo.value.id
    if (!userId) {
      console.error('用户ID不存在')
      return
    }
    
    const summary = await statisticsApi.getUserDonationSummary(userId)
    userStats.value = summary
  } catch (error) {
    console.error('加载用户数据失败:', error)
    // 加载失败时使用模拟数据
    userStats.value = {
      totalFundAmount: 1500,
      totalItemCount: 28,
      totalActivityCount: 12,
      totalDonationCount: 15
    }
  }
}

onMounted(async () => {
  await loadUserData()
  await initCharts()
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    if (fundChart.value) {
      echarts.getInstanceByDom(fundChart.value)?.resize()
    }
    if (itemChart.value) {
      echarts.getInstanceByDom(itemChart.value)?.resize()
    }
    if (activityChart.value) {
      echarts.getInstanceByDom(activityChart.value)?.resize()
    }
    if (distributionChart.value) {
      echarts.getInstanceByDom(distributionChart.value)?.resize()
    }
  })
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  overflow-y: auto;
  overflow-x: hidden;
}

.profile-header {
  margin-bottom: 20px;
}

.profile-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.profile-content {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.user-info-card {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

.user-info-card h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
}

.label {
  width: 120px;
  font-weight: 500;
  color: #606266;
}

.value {
  color: #303133;
}

.stats-overview {
  margin-bottom: 30px;
}

.stats-overview h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 15px;
}

.stats-cards {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 200px;
  padding: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  text-align: center;
  border: 1px solid #e4e7ed;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 5px;
}

.card-label {
  font-size: 14px;
  color: #606266;
}

.charts-container {
    margin-top: 30px;
}

.charts-container h3 {
    font-size: 18px;
    color: #333;
    margin-bottom: 15px;
}

.charts-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-top: 20px;
}

.chart-section {
    background-color: #f8f9fa;
    border-radius: 8px;
    padding: 15px;
    border: 1px solid #e4e7ed;
}

.section-header {
    margin-bottom: 15px;
}

.section-header h4 {
    font-size: 16px;
    color: #606266;
    margin: 0;
}

.chart-wrapper {
    position: relative;
    height: 250px;
}

.chart {
    width: 100%;
    height: 100%;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 10px;
    background-color: white;
}

@media (max-width: 768px) {
  .stats-cards {
    flex-direction: column;
  }
  
  .stat-card {
    min-width: 100%;
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-wrapper {
    height: 300px;
  }
}
</style>