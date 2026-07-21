<template>
  <Layout>
    <div class="home-page">
      <!-- 个性化推荐区域 -->
      <div class="recommend-section" v-if="token">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><StarFilled /></el-icon> 为你推荐
          </h3>
          <el-tag type="warning" effect="plain" size="small">基于你的阅读偏好</el-tag>
        </div>
        <div class="recommend-carousel-wrapper" v-loading="recommendLoading" @mouseenter="handleMouseEnter" @mouseleave="handleMouseLeave">
          <button 
            v-if="showLeftArrow" 
            class="carousel-arrow carousel-arrow-left" 
            @click="scrollCarouselLeft"
            aria-label="向左滑动"
          >
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <div class="recommend-carousel" ref="carouselRef" @scroll="handleCarouselScroll">
            <template v-if="recommendedArticles.length > 0">
              <div 
                v-for="article in recommendedArticles" 
                :key="article.id" 
                class="recommend-card"
                @click="$router.push(`/article/${article.id}`)"
              >
                <div class="card-cover">
                  <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
                  <div v-else class="cover-placeholder">
                    <el-icon><Document /></el-icon>
                  </div>
                </div>
                <div class="card-body">
                  <h4 class="recommend-title">{{ article.title }}</h4>
                  <div class="recommend-meta">
                    <span class="stats">
                      <el-icon><View /></el-icon> {{ article.viewCount }}
                      <el-icon><Star /></el-icon> {{ article.likeCount }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无推荐，先去发现页逛逛吧~" :image-size="80" />
          </div>
          <button 
            v-if="showRightArrow" 
            class="carousel-arrow carousel-arrow-right" 
            @click="scrollCarouselRight"
            aria-label="向右滑动"
          >
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
      </div>

      <div class="page-header">
        <div class="header-left">
          <h2 class="page-title">发现</h2>
        </div>
        <div class="header-right">
          <el-input
            v-model="keyword"
            placeholder="搜索文章..."
            :prefix-icon="Search"
            class="search-input"
            clearable
            @input="handleSearchInput"
          />
          <el-select v-model="sortBy" class="sort-select" @change="fetchArticles">
            <el-option label="最新发布" value="time" />
            <el-option label="最热" value="hot" />
          </el-select>
        </div>
      </div>

      <!-- 分类筛选器 -->
      <div class="category-filter">
        <el-tabs v-model="selectedTag" @tab-change="handleTagChange">
          <el-tab-pane label="全部" name="" />
          <el-tab-pane 
            v-for="tag in displayTags" 
            :key="tag" 
            :label="tag" 
            :name="tag" 
          />
        </el-tabs>
      </div>

      <div class="article-list">
        <div 
          v-for="article in articles" 
          :key="article.id" 
          class="article-card"
          @click="$router.push(`/article/${article.id}`)"
        >
          <div class="article-content">
            <h3 class="article-title" v-html="highlightText(article.title)"></h3>
            <p class="article-summary" v-html="highlightText(article.summary || '暂无摘要')"></p>
            <div class="article-meta">
              <span class="author">{{ article.authorNickname || article.authorName }}</span>
              <span class="divider">·</span>
              <span class="time">{{ formatTime(article.createTime) }}</span>
              <span class="divider">·</span>
              <span class="stats">
                <el-icon><View /></el-icon> {{ article.viewCount }}
                <el-icon><Star /></el-icon> {{ article.likeCount }}
                <el-icon><ChatDotRound /></el-icon> {{ article.commentCount }}
              </span>
              <TagList 
                v-if="article.tags" 
                :tags="article.tags.split(',')" 
                mode="emit"
                @tag-click="handleArticleTagClick"
              />
            </div>
          </div>
        </div>

        <el-empty v-if="articles.length === 0 && !loading" description="暂无文章" />
      </div>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchArticles"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, View, Star, ChatDotRound, StarFilled, Document, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getArticleList } from '@/api/article'
import { getRecommendations } from '@/api/recommend'
import Layout from '@/components/Layout.vue'
import TagList from '@/components/TagList.vue'
import dayjs from 'dayjs'
import { highlightKeyword } from '@/utils/highlight'

const route = useRoute()
const router = useRouter()

const articles = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const sortBy = ref('time')
const selectedTag = ref('')
const popularTags = ref([])
const temporaryTag = ref('') // 临时存储URL中不存在于热门标签的tag

// 推荐相关
const recommendedArticles = ref([])
const showRecommend = ref(false)
const currentUserId = ref(null)
const token = ref(localStorage.getItem('token'))
const recommendLoading = ref(false)

// 轮播箭头控制
const carouselRef = ref(null)
const showLeftArrow = ref(false)
const showRightArrow = ref(false)
const isHoverCarousel = ref(false)

const handleCarouselScroll = () => {
  const el = carouselRef.value
  if (!el) return
  const { scrollLeft: sl, scrollWidth, clientWidth } = el
  showLeftArrow.value = sl > 5
  showRightArrow.value = sl < scrollWidth - clientWidth - 5
}

const scrollCarouselLeft = () => {
  const el = carouselRef.value
  if (!el) return
  el.scrollBy({ left: -216, behavior: 'smooth' })
}

const scrollCarouselRight = () => {
  const el = carouselRef.value
  if (!el) return
  el.scrollBy({ left: 216, behavior: 'smooth' })
}

const handleMouseEnter = () => {
  isHoverCarousel.value = true
  handleCarouselScroll()
}

const handleMouseLeave = () => {
  isHoverCarousel.value = false
  showLeftArrow.value = false
  showRightArrow.value = false
}

// 显示的标签列表（热门标签 + 临时标签）
const displayTags = computed(() => {
  const tags = [...popularTags.value]
  if (temporaryTag.value && !tags.includes(temporaryTag.value)) {
    tags.push(temporaryTag.value)
  }
  return tags
})

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticleList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
      keyword: keyword.value || undefined,
      tag: selectedTag.value || undefined
    })
    articles.value = res.data.records
    total.value = res.data.total
    
    // 如果是第一次加载且没有选择标签，提取热门标签
    if (!selectedTag.value && popularTags.value.length === 0) {
      extractPopularTags(res.data.records)
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 从文章中提取热门标签
const extractPopularTags = (articles) => {
  const tagCount = {}
  articles.forEach(article => {
    if (article.tags) {
      const tags = article.tags.split(',')
      tags.forEach(tag => {
        tagCount[tag] = (tagCount[tag] || 0) + 1
      })
    }
  })
  
  // 按出现次数排序，取前10个
  popularTags.value = Object.entries(tagCount)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 10)
    .map(([tag]) => tag)
}

const handleTagChange = (tag) => {
  currentPage.value = 1
  // 如果是临时标签，点击后清除临时标签状态
  if (tag === temporaryTag.value) {
    temporaryTag.value = ''
  }
  // 清除URL中的activeTag参数
  router.push({ query: {} })
  fetchArticles()
}

// 场景三：首页文章卡片中点击Tag
const handleArticleTagClick = (tag) => {
  currentPage.value = 1
  selectedTag.value = tag
  
  // 检查标签是否在热门标签列表中
  if (!popularTags.value.includes(tag)) {
    // 如果不在，设置临时标签用于显示
    temporaryTag.value = tag
  }
  
  fetchArticles()
}

const handleSearchInput = () => {
  // 由 watch 处理防抖逻辑
}

const handleSearch = () => {
  // 立即搜索（用于回车触发）
  if (searchTimer) clearTimeout(searchTimer)
  currentPage.value = 1
  fetchArticles()
}

// 搜索防抖功能
let searchTimer = null
watch(keyword, (newVal) => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1
    fetchArticles()
  }, 500) // 500ms 防抖
})

const handleSizeChange = () => {
  currentPage.value = 1
  fetchArticles()
}

// 高亮文本
const highlightText = (text) => {
  if (!keyword.value) return text
  return highlightKeyword(text, keyword.value)
}

// 获取推荐文章
const fetchRecommendations = async () => {
  // 未登录用户不显示推荐区域
  if (!token.value) {
    return
  }
  
  recommendLoading.value = true
  try {
    const res = await getRecommendations(10)
    if (res.code === 200 && res.data && res.data.length > 0) {
      recommendedArticles.value = res.data
    } else {
      recommendedArticles.value = []
    }
  } catch (error) {
    console.error('获取推荐失败:', error)
    recommendedArticles.value = []
  } finally {
    recommendLoading.value = false
  }
}

// 处理URL中的activeTag参数
const handleActiveTagFromUrl = () => {
  const activeTag = route.query.activeTag
  if (activeTag) {
    try {
      const decodedTag = decodeURIComponent(activeTag)
      selectedTag.value = decodedTag
      
      // 检查标签是否在热门标签列表中
      if (!popularTags.value.includes(decodedTag)) {
        // 如果不在，设置临时标签用于显示
        temporaryTag.value = decodedTag
      }
      
      currentPage.value = 1
      fetchArticles()
    } catch (e) {
      console.error('解析URL标签失败:', e)
    }
  }
}

onMounted(() => {
  fetchArticles()
  fetchRecommendations()
  
  // 初始加载时处理URL中的activeTag参数
  handleActiveTagFromUrl()
  
  // 监听路由变化，处理URL中的activeTag参数
  watch(() => route.query.activeTag, (newTag) => {
    if (newTag) {
      handleActiveTagFromUrl()
    }
  })

  // 推荐数据加载后检查箭头状态
  watch(recommendedArticles, () => {
    nextTick(() => {
      handleCarouselScroll()
    })
  })
})

onUnmounted(() => {
  if (searchTimer) clearTimeout(searchTimer)
})
</script>

<style lang="scss" scoped>
.home-page {
  // 推荐区域样式 — 暗调奢华风格，径向渐变避免死黑
  .recommend-section {
    margin-bottom: 24px;
    padding: 24px;
    background: radial-gradient(ellipse at 30% 20%, #3a5c52 0%, #284139 50%, #1a2d26 100%);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-elevated);
    position: relative;
    overflow: visible;

    // 噪点纹理叠加层
    &::before {
      content: '';
      position: absolute;
      inset: 0;
      opacity: 0.04;
      background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)'/%3E%3C/svg%3E");
      background-repeat: repeat;
      background-size: 200px 200px;
      pointer-events: none;
    }

    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
      position: relative;
      z-index: 1;

      .section-title {
        margin: 0;
        font-family: var(--font-sans);
        font-size: 18px;
        font-weight: 600;
        color: var(--color-bg-paper);
        display: flex;
        align-items: center;
        gap: 8px;

        .el-icon {
          font-size: 20px;
        }
      }
    }

    .recommend-carousel-wrapper {
      position: relative;
    }

    .carousel-arrow {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      width: 36px;
      height: 36px;
      border-radius: 50%;
      border: none;
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(4px);
      color: #284139;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 10;
      transition: all var(--transition-fast);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

      &:hover {
        background: #fff;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
        transform: translateY(-50%) scale(1.1);
      }

      .el-icon {
        font-size: 18px;
      }

      &.carousel-arrow-left {
        left: -8px;
      }

      &.carousel-arrow-right {
        right: -8px;
      }
    }

    .recommend-carousel {
      display: flex;
      gap: 16px;
      overflow-x: auto;
      scroll-behavior: smooth;
      scroll-snap-type: x mandatory;
      -webkit-overflow-scrolling: touch;
      padding-bottom: 4px;
      position: relative;
      z-index: 1;

      // 隐藏滚动条（Chrome/Safari）
      &::-webkit-scrollbar {
        display: none;
      }
      // 隐藏滚动条（Firefox）
      scrollbar-width: none;
      // 隐藏滚动条（IE/Edge）
      -ms-overflow-style: none;

      .recommend-card {
        flex: 0 0 200px;
        flex-shrink: 0;
        min-width: 180px;
        background: rgba(255, 255, 255, 0.93);
        backdrop-filter: blur(4px);
        border-radius: var(--radius-md);
        cursor: pointer;
        transition: all var(--transition-normal);
        border: 1px solid rgba(248, 215, 148, 0.15);
        overflow: hidden;
        scroll-snap-align: start;

        &:hover {
          transform: translateY(-5px);
          box-shadow: var(--shadow-elevated);
          background: #fff;
          border-color: rgba(248, 215, 148, 0.35);
        }

        .card-cover {
          width: 100%;
          height: 120px;
          overflow: hidden;
          background: #f0f0f0;

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
          }

          .cover-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #e8e0d4 0%, #d4c8b8 100%);
            color: #a09080;

            .el-icon {
              font-size: 32px;
            }
          }
        }

        .card-body {
          padding: 12px;

          .recommend-title {
            margin: 0 0 8px 0;
            font-family: var(--font-sans);
            font-size: 14px;
            font-weight: 600;
            color: var(--color-text-main);
            line-height: 1.4;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }

          .recommend-meta {
            display: flex;
            align-items: center;
            font-family: var(--font-sans);
            font-size: 11px;
            color: var(--color-text-muted);

            .stats {
              display: flex;
              gap: 8px;
              align-items: center;

              .el-icon {
                font-size: 13px;
              }
            }
          }
        }
      }
    }
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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
    }

    .header-right {
      display: flex;
      gap: 12px;
      align-items: center;

      .search-input {
        width: 240px;
      }

      .sort-select {
        width: 120px;
      }
    }
  }

  // 分类筛选器 — 胶囊标签栏
  .category-filter {
    margin-bottom: 20px;
    padding: 12px 20px;
    background: #fff;
    border-radius: var(--radius-md);
    box-shadow: var(--shadow-card);
    height: auto;

    :deep(.el-tabs__header) {
      margin: 0;
    }

    // 隐藏 Element Plus 默认的导航底部边框
    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }

    // 隐藏默认的激活指示条（is-active 下划线）
    :deep(.el-tabs__active-bar) {
      display: none;
    }

    :deep(.el-tabs__nav) {
      border: none;
      gap: 12px;
    }

    :deep(.el-tabs__item) {
      display: flex;
      align-items: center;
      height: 38px;
      border: 1px solid #E0E0E0;
      border-radius: 20px;
      margin-right: 0;
      font-family: var(--font-sans);
      font-size: 14px;
      background: transparent;
      color: var(--color-text-secondary);
      padding: 0 22px;
      line-height: 1;
      transition: all var(--transition-fast);

      &:hover {
        color: #284139;
        border-color: #284139;
      }

      &.is-active {
        background: #284139;
        color: #FDFBF7;
        border-color: #284139;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(40, 65, 57, 0.25);
      }
    }
  }

  .article-list {
    .article-card {
      background: #fff;
      border-radius: var(--radius-md);
      padding: 20px;
      margin-bottom: 12px;
      cursor: pointer;
      transition: all var(--transition-normal);
      box-shadow: 0 4px 20px rgba(40, 65, 57, 0.05);

      &:hover {
        box-shadow: 0 6px 28px rgba(40, 65, 57, 0.1);
        transform: translateY(-1px);

        .article-title {
          color: var(--color-primary);
        }
      }

      .article-content {
        .article-title {
          margin: 0 0 8px 0;
          font-family: var(--font-sans);
          font-size: 18px;
          font-weight: 700;
          color: var(--color-text-main);
          transition: color var(--transition-fast);
          line-height: 1.6;

          // 高亮样式
          :deep(.highlight) {
            color: var(--color-accent);
            background: rgba(184, 104, 48, 0.1);
            padding: 0 2px;
            border-radius: 2px;
            font-weight: 600;
          }
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

          // 高亮样式
          :deep(.highlight) {
            color: var(--color-accent);
            background: rgba(184, 104, 48, 0.1);
            padding: 0 2px;
            border-radius: 2px;
            font-weight: 600;
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
