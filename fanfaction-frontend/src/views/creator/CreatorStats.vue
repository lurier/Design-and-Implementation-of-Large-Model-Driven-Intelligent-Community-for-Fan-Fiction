<template>
  <div class="creator-stats-page">
    <div class="page-header">
      <h1 class="page-title">数据中心</h1>
      <p class="page-subtitle">查看您的创作成果与数据统计</p>
    </div>

    <div class="stats-container" v-loading="loading">
      <!-- 概览卡片 -->
      <el-row :gutter="20" class="overview-cards">
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #284139 0%, #3a5c52 100%)">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.workCount || 0 }}</div>
              <div class="stat-label">总作品数</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #B86830 0%, #d47a3e 100%)">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(stats.totalWords || 0) }}</div>
              <div class="stat-label">总字数</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #809076 0%, #a3b29a 100%)">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(stats.totalReads || 0) }}</div>
              <div class="stat-label">累计阅读量</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 数据指标卡片 -->
      <el-row :gutter="20" class="metrics-cards">
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-item">
              <el-icon class="metric-icon" style="color: #284139"><Star /></el-icon>
              <div class="metric-info">
                <span class="metric-value">{{ formatNumber(stats.totalLikes || 0) }}</span>
                <span class="metric-label">总点赞数</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-item">
              <el-icon class="metric-icon" style="color: #B86830"><Star /></el-icon>
              <div class="metric-info">
                <span class="metric-value">{{ formatNumber(stats.totalFavorites || 0) }}</span>
                <span class="metric-label">总收藏数</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-item">
              <el-icon class="metric-icon" style="color: #809076"><User /></el-icon>
              <div class="metric-info">
                <span class="metric-value">{{ formatNumber(stats.totalComments || 0) }}</span>
                <span class="metric-label">总评论数</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-item">
              <el-icon class="metric-icon" style="color: #8B3A3A"><User /></el-icon>
              <div class="metric-info">
                <span class="metric-value">{{ stats.fansCount || 0 }}</span>
                <span class="metric-label">粉丝数量</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 阅读量趋势图表 -->
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">阅读量趋势</span>
            <el-radio-group v-model="dateRange" size="small" @change="loadStats">
              <el-radio-button label="WEEK">近 7 天</el-radio-button>
              <el-radio-button label="MONTH">近 30 天</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div ref="readTrendChartRef" class="chart"></div>
      </el-card>

      <!-- 作品表现排行 -->
      <el-card class="chart-card">
        <template #header>
          <span class="card-title">作品数据明细</span>
        </template>
        <el-table :data="topWorks" style="width: 100%" :empty-text="topWorks.length === 0 ? '暂无作品数据' : ''">
          <el-table-column type="index" label="排名" width="60" />
          <el-table-column prop="title" label="作品名称" min-width="200" />
          <el-table-column prop="readCount" label="阅读量" width="100" sortable />
          <el-table-column prop="favoriteCount" label="收藏量" width="100" sortable />
          <el-table-column prop="commentCount" label="评论数" width="100" sortable />
          <el-table-column prop="likeCount" label="点赞数" width="100" sortable />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, View, Star, User } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getCreatorStats } from '@/api/creator'

const loading = ref(false)
const dateRange = ref('WEEK')
const readTrendChartRef = ref(null)
let readTrendChart = null

// 统计数据
const stats = reactive({
  workCount: 0,
  totalWords: 0,
  totalReads: 0,
  totalLikes: 0,
  totalFavorites: 0,
  totalComments: 0,
  fansCount: 0
})

// 顶部作品
const topWorks = ref([])

// 数字格式化函数
const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 加载统计数据
const loadStats = async () => {
  loading.value = true
  try {
    const res = await getCreatorStats({
      dateRange: dateRange.value
    })
    
    const data = res.data
    stats.workCount = data.workCount || 0
    stats.totalWords = data.totalWords || 0
    stats.totalReads = data.totalReads || 0
    stats.totalLikes = data.totalLikes || 0
    stats.totalFavorites = data.totalFavorites || 0
    stats.totalComments = data.totalComments || 0
    stats.fansCount = data.fansCount || 0
    topWorks.value = data.topWorks || []
    
    // 更新图表
    if (data.readTrend) {
      updateReadTrendChart(data.readTrend)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 更新阅读量趋势图表
const updateReadTrendChart = (trendData) => {
  if (!readTrendChartRef.value) return
  
  if (!readTrendChart) {
    readTrendChart = echarts.init(readTrendChartRef.value)
  }
  
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
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: trendData.dates || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '阅读量',
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(184, 104, 48, 0.25)' },
            { offset: 1, color: 'rgba(184, 104, 48, 0.01)' }
          ])
        },
        itemStyle: {
          color: '#B86830'
        },
        lineStyle: {
          color: '#B86830',
          width: 3
        },
        data: trendData.readCounts || []
      }
    ]
  }
  
  readTrendChart.setOption(option)
}

onMounted(() => {
  loadStats()
  
  nextTick(() => {
    if (readTrendChartRef.value) {
      readTrendChart = echarts.init(readTrendChartRef.value)
      
      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        readTrendChart?.resize()
      })
    }
  })
})
</script>

<style lang="scss" scoped>
.creator-stats-page {
  .page-header {
    margin-bottom: 24px;

    .page-title {
      font-size: 28px;
      font-weight: 700;
      color: var(--color-text-main);
      margin: 0;
      margin-bottom: 8px;
    }

    .page-subtitle {
      font-size: 14px;
      color: #8c8c8c;
      margin: 0;
    }
  }

  .stats-container {
    .overview-cards {
      margin-bottom: 20px;

      .stat-card {
        display: flex;
        align-items: center;
        padding: 24px;
        border-radius: 16px;
        transition: all 0.3s ease;
        border: 1px solid #e8e8e8;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
        }

        .stat-icon {
          width: 70px;
          height: 70px;
          border-radius: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 20px;
          flex-shrink: 0;

          .el-icon {
            font-size: 32px;
            color: #fff;
          }
        }

        .stat-content {
          flex: 1;

          .stat-value {
            font-size: 36px;
            font-weight: 700;
            color: var(--color-text-main);
            margin-bottom: 4px;
            line-height: 1.2;
          }

          .stat-label {
            font-size: 14px;
            color: var(--color-text-muted);
            font-weight: 500;
          }
        }
      }
    }

    .metrics-cards {
      margin-bottom: 20px;

      .metric-card {
        padding: 16px;
        border-radius: 12px;
        border: 1px solid #f0f0f0;

        .metric-item {
          display: flex;
          align-items: center;

          .metric-icon {
            font-size: 24px;
            margin-right: 12px;
          }

          .metric-info {
            display: flex;
            flex-direction: column;

            .metric-value {
              font-size: 18px;
              font-weight: 600;
              color: var(--color-text-main);
            }

            .metric-label {
              font-size: 12px;
              color: #8c8c8c;
            }
          }
        }
      }
    }

    .chart-card {
      margin-bottom: 20px;
      border-radius: 16px;
      border: 1px solid #e8e8e8;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 20px;
        border-bottom: 1px solid #f0f0f0;

        .card-title {
          font-size: 16px;
          font-weight: 600;
          color: #1a1a2e;
        }
      }

      .chart {
        height: 320px;
        width: 100%;
        padding: 20px;
      }
    }
  }

  // 响应式布局
  @media (max-width: 768px) {
    .overview-cards .stat-card {
      padding: 20px;

      .stat-icon {
        width: 56px;
        height: 56px;

        .el-icon {
          font-size: 26px;
        }
      }

      .stat-content .stat-value {
        font-size: 28px;
      }
    }

    .chart-card .chart {
      height: 260px;
    }
  }
}
</style>
