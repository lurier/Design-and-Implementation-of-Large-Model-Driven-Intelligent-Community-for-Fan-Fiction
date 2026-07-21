<template>
  <Layout>
    <div class="reader-center-page">
      <div class="page-header">
        <h2 class="page-title">
          <el-icon><DataAnalysis /></el-icon>
          读者数据中心
        </h2>
        <p class="page-desc">查看您的阅读偏好和数据分析</p>
      </div>

      <div class="charts-container">
        <!-- 偏好分类饼图 -->
        <div class="chart-card">
          <h3 class="chart-title">阅读偏好分布</h3>
          <div ref="categoryChartRef" class="chart-wrapper"></div>
        </div>

        <!-- 活跃时段柱状图 -->
        <div class="chart-card">
          <h3 class="chart-title">活跃时段分析</h3>
          <div ref="timeChartRef" class="chart-wrapper"></div>
        </div>

        <!-- 阅读趋势折线图 -->
        <div class="chart-card full-width">
          <h3 class="chart-title">阅读趋势（近30天）</h3>
          <div ref="trendChartRef" class="chart-wrapper"></div>
        </div>
      </div>

      <!-- 数据统计卡片 -->
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon" style="background: #284139;">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statsData?.totalRead || 0 }}</div>
            <div class="stat-label">总阅读文章</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #B86830;">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statsData?.totalMinutes || 0 }}分钟</div>
            <div class="stat-label">阅读时长</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #809076;">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statsData?.favoriteCount || 0 }}</div>
            <div class="stat-label">收藏文章</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon" style="background: #8B3A3A;">
            <el-icon><Comment /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statsData?.commentCount || 0 }}</div>
            <div class="stat-label">发表评论</div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Reading, Clock, Star, Comment } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import Layout from '@/components/Layout.vue'
import { getUserStats } from '@/api/stats'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const categoryChartRef = ref(null)
const timeChartRef = ref(null)
const trendChartRef = ref(null)

let categoryChart = null
let timeChart = null
let trendChart = null

// 真实统计数据
const statsData = ref(null)

// 初始化偏好分类饼图
const initCategoryChart = () => {
  if (!categoryChartRef.value) return
  
  categoryChart = echarts.init(categoryChartRef.value)
  
  // 使用真实数据，如果没有数据则显示提示
  const data = statsData.value?.categoryDistribution && statsData.value.categoryDistribution.length > 0
    ? statsData.value.categoryDistribution.map(item => ({
        value: item.value,
        name: item.name,
        itemStyle: {
          color: ['#284139', '#B86830', '#809076', '#8B3A3A', '#F8D794', '#3a5c52'][
            statsData.value.categoryDistribution.indexOf(item) % 6
          ]
        }
      }))
    : [{ value: 0, name: '暂无数据', itemStyle: { color: '#e0e0e0' } }]
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}篇 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        name: '阅读偏好',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data
      }
    ]
  }
  
  categoryChart.setOption(option)
}

// 初始化活跃时段柱状图
const initTimeChart = () => {
  if (!timeChartRef.value) return
  
  timeChart = echarts.init(timeChartRef.value)
  
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  
  // 使用真实数据
  const data = statsData.value?.hourlyActivity && statsData.value.hourlyActivity.length > 0
    ? statsData.value.hourlyActivity.map(item => item.value)
    : Array(24).fill(0)
  
  const hasData = data.some(val => val > 0)
  
  const option = {
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
      data: hours,
      axisLabel: {
        interval: 2,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '阅读次数'
    },
    series: hasData ? [{
      name: '阅读次数',
      type: 'bar',
      data: data,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#284139' },
          { offset: 1, color: '#3a5c52' }
        ])
      },
      emphasis: {
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#B86830' },
            { offset: 1, color: '#8B3A3A' }
          ])
        }
      }
    }] : [{
      name: '阅读次数',
      type: 'bar',
      data: [0],
      itemStyle: { color: '#e0e0e0' },
      label: {
        show: true,
        position: 'center',
        formatter: '暂无数据',
        fontSize: 14,
        color: '#999'
      }
    }]
  }
  
  timeChart.setOption(option)
}

// 初始化阅读趋势折线图
const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  trendChart = echarts.init(trendChartRef.value)
  
  // 使用真实数据
  const trendData = statsData.value?.readingTrend || []
  const dates = trendData.length > 0
    ? trendData.map(item => {
        const date = new Date(item.date)
        return `${date.getMonth() + 1}/${date.getDate()}`
      })
    : []
  const data = trendData.length > 0
    ? trendData.map(item => item.value)
    : []
  
  const hasData = data.some(val => val > 0)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: hasData ? {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        interval: 4,
        rotate: 45
      }
    } : {
      type: 'category',
      boundaryGap: false,
      data: [''],
      axisLabel: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '阅读文章数'
    },
    series: hasData ? [{
      name: '阅读量',
      type: 'line',
      smooth: true,
      data: data,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(184, 104, 48, 0.35)' },
          { offset: 1, color: 'rgba(184, 104, 48, 0.05)' }
        ])
      },
      itemStyle: {
        color: '#B86830'
      },
      lineStyle: {
        width: 3,
        color: '#B86830'
      }
    }] : [{
      name: '阅读量',
      type: 'line',
      data: [0],
      itemStyle: { color: '#e0e0e0' },
      label: {
        show: true,
        position: 'center',
        formatter: '暂无数据',
        fontSize: 14,
        color: '#999'
      }
    }]
  }
  
  trendChart.setOption(option)
}

// 响应窗口大小变化
const handleResize = () => {
  categoryChart?.resize()
  timeChart?.resize()
  trendChart?.resize()
}

// 加载用户统计数据
const loadUserStats = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  
  try {
    const res = await getUserStats()
    console.log('统计数据返回:', res)
    statsData.value = res.data || res
    console.log('statsData:', statsData.value)
    
    // 初始化图表
    initCategoryChart()
    initTimeChart()
    initTrendChart()
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

onMounted(() => {
  loadUserStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
  timeChart?.dispose()
  trendChart?.dispose()
})
</script>

<style lang="scss" scoped>
.reader-center-page {
  .page-header {
    margin-bottom: 24px;
    padding: 20px 24px;
    background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
    border-radius: 12px;
    color: #fff;

    .page-title {
      margin: 0 0 8px 0;
      font-size: 24px;
      display: flex;
      align-items: center;
      gap: 10px;

      .el-icon {
        font-size: 28px;
      }
    }

    .page-desc {
      margin: 0;
      font-size: 14px;
      opacity: 0.9;
    }
  }

  .charts-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 24px;

    .chart-card {
      background: #fff;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
      }

      &.full-width {
        grid-column: 1 / -1;
      }

      .chart-title {
        margin: 0 0 16px 0;
        font-size: 16px;
        color: #333;
        font-weight: 600;
      }

      .chart-wrapper {
        width: 100%;
        height: 350px;
      }
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    .stat-card {
      background: #fff;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 28px;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: #333;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 13px;
          color: #999;
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .reader-center-page {
    .charts-container {
      grid-template-columns: 1fr;
    }

    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }
}
</style>
