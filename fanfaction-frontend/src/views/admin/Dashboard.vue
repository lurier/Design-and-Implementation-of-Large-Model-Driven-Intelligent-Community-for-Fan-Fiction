<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h1 class="page-title">数据运营大屏</h1>
      <div class="header-info">
        <span class="date">{{ currentDate }}</span>
      </div>
    </div>

    <!-- 核心指标卡 -->
    <div class="stats-row">
      <el-card class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%)">
          <el-icon :size="24" color="#fff"><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            <span>{{ animatedValues.totalUsers }}</span>
            <span class="stat-unit">人</span>
          </div>
          <div class="stat-title">用户总数</div>
          <div class="stat-sub">今日新增 {{ animatedValues.todayNewUsers }} 人</div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, var(--color-secondary) 0%, var(--color-secondary-light) 100%)">
          <el-icon :size="24" color="#fff"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            <span>{{ animatedValues.totalArticles }}</span>
            <span class="stat-unit">篇</span>
          </div>
          <div class="stat-title">文章总数</div>
          <div class="stat-sub">今日新增 {{ animatedValues.todayNewArticles }} 篇</div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-light) 100%)">
          <el-icon :size="24" color="#fff"><List /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            <span>{{ animatedValues.totalComments }}</span>
            <span class="stat-unit">条</span>
          </div>
          <div class="stat-title">评论总数</div>
          <div class="stat-sub">今日新增 {{ animatedValues.todayNewComments }} 条</div>
        </div>
      </el-card>

      <el-card class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, rgba(128, 144, 118, 0.6) 0%, rgba(184, 104, 48, 0.6) 100%)">
          <el-icon :size="24" color="#fff"><Grid /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">
            <span>{{ animatedValues.totalViews }}</span>
            <span class="stat-unit">次</span>
          </div>
          <div class="stat-title">累计阅读量</div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <el-card class="chart-card">
        <template #header>
          <span class="chart-title">近7天新增趋势</span>
        </template>
        <div ref="trendChartRef" class="chart-container"></div>
      </el-card>

      <el-card class="chart-card">
        <template #header>
          <span class="chart-title">创作者分布</span>
        </template>
        <div ref="pieChartRef" class="chart-container"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { Grid, Document, User, List } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import dayjs from 'dayjs'
import { getDashboardStats } from '@/api/admin'

const currentDate = ref(dayjs().format('YYYY年MM月DD日 HH:mm'))

const animatedValues = reactive({
  totalUsers: 0,
  todayNewUsers: 0,
  totalArticles: 0,
  todayNewArticles: 0,
  totalComments: 0,
  todayNewComments: 0,
  totalViews: 0
})

const trendChartRef = ref(null)
const pieChartRef = ref(null)
let trendChart = null
let pieChart = null

const animateValue = (key, targetValue) => {
  const duration = 1500
  const startTime = performance.now()
  const startVal = animatedValues[key]
  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOut = 1 - Math.pow(1 - progress, 3)
    animatedValues[key] = Math.floor(startVal + (targetValue - startVal) * easeOut).toLocaleString()
    if (progress < 1) {
      requestAnimationFrame(animate)
    }
  }
  requestAnimationFrame(animate)
}

const initTrendChart = (articleTrend, userTrend) => {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)

  const days = articleTrend.map(d => d.date)
  const articles = articleTrend.map(d => d.count)
  const users = userTrend.map(d => d.count)

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ebeef5',
      borderWidth: 1,
      textStyle: { color: '#303133' },
      axisPointer: { type: 'cross' }
    },
    legend: { data: ['新增文章', '新增用户'], bottom: 0 },
    grid: { left: '3%', right: '4%', bottom: '15%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: { lineStyle: { color: '#ebeef5' } },
      axisLabel: { color: '#909399' }
    },
    yAxis: [
      {
        type: 'value', name: '文章数',
        axisLine: { show: false }, axisTick: { show: false },
        splitLine: { lineStyle: { color: '#f5f7fa' } },
        axisLabel: { color: '#909399' }
      },
      {
        type: 'value', name: '用户数',
        axisLine: { show: false }, axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#909399' }
      }
    ],
    series: [
      {
        name: '新增文章', type: 'line', smooth: true,
        symbol: 'circle', symbolSize: 8,
        lineStyle: { width: 3, color: '#B86830' },
        itemStyle: { color: '#B86830', borderWidth: 2, borderColor: '#fff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(184, 104, 48, 0.25)' },
            { offset: 1, color: 'rgba(184, 104, 48, 0.02)' }
          ])
        },
        data: articles
      },
      {
        name: '新增用户', type: 'line', yAxisIndex: 1, smooth: true,
        symbol: 'circle', symbolSize: 8,
        lineStyle: { width: 3, color: '#809076' },
        itemStyle: { color: '#809076', borderWidth: 2, borderColor: '#fff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(128, 144, 118, 0.25)' },
            { offset: 1, color: 'rgba(128, 144, 118, 0.02)' }
          ])
        },
        data: users
      }
    ]
  }
  trendChart.setOption(option)
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(pieChartRef.value)

  const option = {
    title: {
      text: '数据总览',
      subtext: '核心指标占比',
      left: 'center',
      top: 10,
      textStyle: { fontSize: 16, color: '#303133' }
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#ebeef5',
      textStyle: { color: '#303133' },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { color: '#606266' } },
    series: [
      {
        name: '数据分布',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['40%', '55%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' },
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.2)' }
        },
        labelLine: { show: false },
        data: [
          { value: 0, name: '用户', itemStyle: { color: '#284139' } },
          { value: 0, name: '文章', itemStyle: { color: '#809076' } },
          { value: 0, name: '评论', itemStyle: { color: '#B86830' } },
          { value: 0, name: '阅读', itemStyle: { color: '#3a5c52' } }
        ]
      }
    ]
  }
  pieChart.setOption(option)
}

const handleResize = () => {
  trendChart?.resize()
  pieChart?.resize()
}

const loadStats = async () => {
  try {
    const res = await getDashboardStats()
    const stats = res.data

    // 动画展示数字
    const keys = [
      ['totalUsers', stats.totalUsers],
      ['todayNewUsers', stats.todayNewUsers],
      ['totalArticles', stats.totalArticles],
      ['todayNewArticles', stats.todayNewArticles],
      ['totalComments', stats.totalComments],
      ['todayNewComments', stats.todayNewComments],
      ['totalViews', stats.totalViews]
    ]
    keys.forEach(([key, val]) => animateValue(key, val || 0))

    // 初始化图表
    nextTick(() => {
      initTrendChart(stats.articleTrend || [], stats.userTrend || [])

      // 更新饼图数据
      if (!pieChart) {
        initPieChart()
      }
      if (pieChart) {
        pieChart.setOption({
          series: [{
            data: [
              { value: stats.totalUsers || 0, name: '用户', itemStyle: { color: '#284139' } },
              { value: stats.totalArticles || 0, name: '文章', itemStyle: { color: '#809076' } },
              { value: stats.totalComments || 0, name: '评论', itemStyle: { color: '#B86830' } },
              { value: Math.floor((stats.totalViews || 0) / 100), name: '阅读(百次)', itemStyle: { color: '#F8D794' } }
            ]
          }]
        })
      }
    })
  } catch (error) {
    console.error('加载工作台数据失败:', error)
  }
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-family: var(--font-sans);
  font-size: 26px;
  font-weight: 600;
  margin: 0;
  color: var(--color-text-main);
}

.header-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.date {
  font-family: var(--font-mono);
  font-size: 13px;
  color: var(--color-text-muted);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  border: 1px solid var(--border-color-light);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-family: var(--font-serif);
  font-size: 30px;
  font-weight: 700;
  color: var(--color-text-main);
  margin-bottom: 2px;
  line-height: 1.1;
}

.stat-unit {
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 400;
  color: var(--color-text-muted);
  margin-left: 4px;
  text-transform: lowercase;
}

.stat-title {
  font-family: var(--font-sans);
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 2px;
}

.stat-sub {
  font-family: var(--font-sans);
  font-size: 12px;
  color: var(--color-accent);
}

.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.chart-card {
  height: 400px;
}

.chart-title {
  font-family: var(--font-sans);
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-main);
}

.chart-container {
  width: 100%;
  height: calc(100% - 48px);
}

@media (max-width: 768px) {
  .stats-row {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-row {
    grid-template-columns: 1fr;
  }
}
</style>
