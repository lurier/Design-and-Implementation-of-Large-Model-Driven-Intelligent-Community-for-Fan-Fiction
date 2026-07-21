<template>
  <Layout>
    <div class="reading-history-page">
      <div class="page-header">
        <h2 class="page-title">
          <el-icon><Clock /></el-icon>
          我的阅读历史
        </h2>
      </div>

      <div class="history-list" v-loading="loading">
        <div 
          v-for="article in historyList" 
          :key="article.id" 
          class="history-card"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="article-content">
            <h3 class="article-title">{{ article.title }}</h3>
            <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
            
            <div class="reading-progress-bar" v-if="article.readPercentage > 0">
              <el-progress 
                :percentage="parseFloat(article.readPercentage)" 
                :stroke-width="6"
                :show-text="false"
              />
              <span class="progress-text">已读 {{ parseFloat(article.readPercentage).toFixed(1) }}%</span>
            </div>
            
            <div class="article-meta">
              <span class="author">{{ article.authorNickname || article.authorName }}</span>
              <span class="divider">·</span>
              <span class="time">最后阅读: {{ formatTime(article.lastReadTime) }}</span>
              <span class="divider">·</span>
              <span class="stats">
                <el-icon><View /></el-icon> {{ article.viewCount }}
                <el-icon><Star /></el-icon> {{ article.likeCount }}
                <el-icon><ChatDotRound /></el-icon> {{ article.commentCount }}
              </span>
              <TagList v-if="article.tags" :tags="article.tags.split(',')" />
            </div>
          </div>
        </div>

        <el-empty v-if="historyList.length === 0 && !loading" description="暂无阅读历史" />
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchHistory"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Clock, View, Star, ChatDotRound } from '@element-plus/icons-vue'
import { getReadingHistory } from '@/api/history'
import Layout from '@/components/Layout.vue'
import TagList from '@/components/TagList.vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'

const historyList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatTime = (time) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await getReadingHistory({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    historyList.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error(error)
    ElMessage.error('获取阅读历史失败')
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  currentPage.value = 1
  fetchHistory()
}

onMounted(() => {
  fetchHistory()
})
</script>

<style lang="scss" scoped>
.reading-history-page {
  .page-header {
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #fff;
    border-radius: var(--radius-md);
    box-shadow: var(--shadow-card);

    .page-title {
      margin: 0;
      font-family: var(--font-sans);
      font-size: 20px;
      font-weight: 600;
      color: var(--color-text-main);
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        font-size: 24px;
        color: var(--color-primary);
      }
    }
  }

  .history-list {
    .history-card {
      background: #fff;
      border-radius: var(--radius-md);
      padding: 20px;
      margin-bottom: 12px;
      cursor: pointer;
      transition: all var(--transition-normal);
      box-shadow: var(--shadow-card);

      &:hover {
        box-shadow: var(--shadow-elevated);
        transform: translateY(-2px);

        .article-title {
          color: var(--color-primary);
        }
      }

      .article-content {
        .article-title {
          margin: 0 0 8px 0;
          font-family: var(--font-sans);
          font-size: 18px;
          font-weight: 600;
          color: var(--color-text-main);
          transition: color var(--transition-fast);
        }

        .article-summary {
          margin: 0 0 12px 0;
          color: var(--color-text-secondary);
          font-family: var(--font-serif);
          font-size: 14px;
          line-height: 1.6;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }

        .reading-progress-bar {
          margin-bottom: 12px;
          display: flex;
          align-items: center;
          gap: 12px;

          :deep(.el-progress) {
            flex: 1;
          }
          
          :deep(.el-progress-bar__outer) {
            background: #F0EBE1;
            border-radius: 8px;
            overflow: hidden;
          }
          
          :deep(.el-progress-bar__inner) {
            background: var(--color-accent);
            border-radius: 8px;
          }

          .progress-text {
            font-size: 12px;
            color: var(--color-accent);
            font-weight: 500;
            min-width: 80px;
            font-family: var(--font-mono);
          }
        }

        .article-meta {
          display: flex;
          align-items: center;
          gap: 8px;
          font-family: var(--font-sans);
          font-size: 13px;
          color: var(--color-text-muted);

          .author {
            color: var(--color-text-secondary);
          }

          .time {
            color: var(--color-accent);
            font-weight: 500;
          }

          .divider {
            color: var(--border-color);
          }

          .stats {
            display: flex;
            align-items: center;
            gap: 4px;
            
            .el-icon {
              margin-right: 2px;
            }
          }

          .tags {
            margin-left: auto;
            display: flex;
            gap: 6px;
          }
        }
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    padding: 24px 0;
  }
}
</style>
