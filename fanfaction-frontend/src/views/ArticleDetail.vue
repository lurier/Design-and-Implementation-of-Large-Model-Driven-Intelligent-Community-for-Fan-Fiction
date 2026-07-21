<template>
  <Layout>
    <div class="article-detail-page" v-loading="loading">
      <template v-if="article">
        <!-- 阅读工具栏 -->
        <div class="reading-toolbar">
          <el-button @click="toggleImmersiveMode" :type="isImmersive ? 'primary' : 'default'">
            <el-icon><FullScreen /></el-icon> {{ isImmersive ? '退出沉浸' : '沉浸模式' }}
          </el-button>
          <el-button @click="showSettings = true">
            <el-icon><Setting /></el-icon> 阅读设置
          </el-button>
          <el-button @click="addBookmark">
            <el-icon><Star /></el-icon> 添加书签
          </el-button>
          <el-button @click="showBookmarks = true">
            <el-icon><Collection /></el-icon> 书签列表 ({{ bookmarks.length }})
          </el-button>
        </div>

        <div class="article-header">
          <h1 class="article-title">{{ article.title }}</h1>
          <div class="article-meta">
            <span class="author">{{ article.authorNickname || article.authorName }}</span>
            <span class="divider">·</span>
            <span class="time">{{ formatTime(article.createTime) }}</span>
            <span class="divider">·</span>
            <span class="stats">
              <el-icon><View /></el-icon> {{ article.viewCount }} 浏览
            </span>
            <span v-if="article.readTime" class="read-time">
              <el-icon><Clock /></el-icon> 预计阅读 {{ article.readTime }} 分钟
            </span>
            <TagList v-if="article.tags" :tags="article.tags.split(',')" />
          </div>
        </div>

        <div class="article-body" ref="articleBodyRef" @scroll="handleScroll">
          <div class="content" v-html="renderedContent"></div>
        </div>

        <div class="article-actions">
          <el-button 
            :type="isLiked ? 'primary' : 'default'" 
            :class="{ active: isLiked }"
            @click="handleLike"
          >
            <el-icon><Star /></el-icon> 点赞 ({{ article.likeCount }})
          </el-button>
          <el-button 
            :type="isFavorited ? 'warning' : 'default'" 
            :class="{ active: isFavorited }"
            @click="handleFavorite"
          >
            <el-icon><Collection /></el-icon> 收藏 ({{ article.favoriteCount }})
          </el-button>
        </div>

        <div class="comment-section">
          <h3 class="section-title">评论 ({{ article.commentCount }})</h3>
          
          <div class="comment-input" v-if="userStore.token">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
              maxlength="1000"
              show-word-limit
            />
            <el-button 
              type="primary" 
              @click="handlePublishComment" 
              :loading="commentSubmitting || emotionLoading" 
              :disabled="!commentContent.trim() || commentSubmitting || emotionLoading"
            >
              {{ emotionLoading ? 'AI 正在分析情绪...' : '发表评论' }}
            </el-button>
          </div>
          <div v-else class="login-tip">
            <el-button type="primary" @click="$router.push('/login')">登录后参与评论</el-button>
          </div>

          <div class="comment-list">
            <CommentItem
              v-for="comment in comments"
              :key="comment.id"
              :comment="comment"
              :article-id="article.id"
              @reply="fetchComments"
            />
            <el-empty v-if="comments.length === 0" description="暂无评论" />
          </div>

          <div class="comment-pagination">
            <el-pagination
              v-model:current-page="commentPage"
              :total="commentTotal"
              :page-size="10"
              layout="prev, pager, next"
              @current-change="fetchComments"
            />
          </div>
        </div>
      </template>
    </div>

    <!-- 阅读设置对话框 -->
    <el-dialog v-model="showSettings" title="阅读设置" width="400px">
      <el-form label-width="80px">
        <el-form-item label="背景主题">
          <el-radio-group v-model="theme">
            <el-radio-button label="light">日间</el-radio-button>
            <el-radio-button label="sepia">护眼</el-radio-button>
            <el-radio-button label="dark">夜间</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="字体大小">
          <el-slider v-model="fontSize" :min="14" :max="24" :step="2" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSettings = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 书签列表抽屉 -->
    <el-drawer v-model="showBookmarks" title="我的书签" size="350px">
      <div class="bookmarks-list">
        <el-empty v-if="bookmarks.length === 0" description="暂无书签" />
        <div v-for="bookmark in bookmarks" :key="bookmark.id" class="bookmark-item">
          <div class="bookmark-info">
            <div class="bookmark-position">位置: {{ bookmark.position }}px</div>
            <div class="bookmark-note" v-if="bookmark.note">{{ bookmark.note }}</div>
            <div class="bookmark-time">{{ formatTime(bookmark.createTime) }}</div>
          </div>
          <div class="bookmark-actions">
            <el-button size="small" @click="jumpToBookmark(bookmark.position)">跳转</el-button>
            <el-button size="small" type="danger" @click="removeBookmark(bookmark.id)">删除</el-button>
          </div>
        </div>
      </div>
    </el-drawer>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { View, Star, Collection, FullScreen, Setting, Clock } from '@element-plus/icons-vue'
import { getArticleDetail } from '@/api/article'
import { toggleLike, toggleFavorite, publishComment, getCommentList, refreshIdempotentToken } from '@/api/interaction'
import { detectEmotion } from '@/api/ai'
import { saveReadingProgress, getReadingProgress, addBookmark as addBookmarkApi, deleteBookmark as deleteBookmarkApi, getBookmarksByArticle } from '@/api/reading'
import { saveReadingHistory } from '@/api/history'
import { useUserStore } from '@/stores/user'
import Layout from '@/components/Layout.vue'
import TagList from '@/components/TagList.vue'
import CommentItem from '@/components/CommentItem.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { marked } from 'marked'

const route = useRoute()
const userStore = useUserStore()

const article = ref(null)
const loading = ref(false)
const isLiked = ref(false)
const isFavorited = ref(false)
const commentContent = ref('')
const commentSubmitting = ref(false)
const emotionLoading = ref(false)
const comments = ref([])
const commentPage = ref(1)
const commentTotal = ref(0)

// 阅读增强功能状态
const articleBodyRef = ref(null)
const isImmersive = ref(false)
const showSettings = ref(false)
const showBookmarks = ref(false)
const theme = ref('light')
const fontSize = ref(16)
const bookmarks = ref([])
const savedScrollPosition = ref(0)
let scrollTimer = null

// 阅读时长统计
let readStartTime = null
let totalReadDuration = 0
let readTimer = null
let isPageVisible = true

const renderedContent = computed(() => {
  if (!article.value?.content) return ''
  return marked.parse(article.value.content)
})

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')

const fetchArticle = async () => {
  loading.value = true
  try {
    const res = await getArticleDetail(route.params.id)
    article.value = res.data
    // 从后端获取用户的点赞/收藏状态
    isLiked.value = res.data.isLiked || false
    isFavorited.value = res.data.isFavorited || false
  } catch (error) {
    console.error('获取文章详情失败:', error.response?.data || error.message)
    if (error.response?.status === 404) {
      ElMessage.error('文章不存在')
    } else if (error.response?.status !== 401) {
      ElMessage.error('获取文章详情失败')
    }
  } finally {
    loading.value = false
  }
}

const fetchComments = async () => {
  try {
    const res = await getCommentList(route.params.id)
    // 打印原始数据结构，方便调试
    console.log('评论原始数据:', res.data)
    
    // 将平铺的评论列表转换为树形结构
    const flatComments = res.data || []
    const treeComments = buildCommentTree(flatComments)
    
    console.log('评论树形结构:', treeComments)
    
    comments.value = treeComments
    commentTotal.value = flatComments.length
  } catch (error) {
    console.error('获取评论失败:', error.response?.data || error.message)
    if (error.response?.status !== 401) {
      ElMessage.error('获取评论失败')
    }
  }
}

// 将平铺的评论列表转换为树形结构
const buildCommentTree = (flatList) => {
  if (!flatList || flatList.length === 0) return []
  
  console.log('开始构建评论树，输入数据:', flatList)
  
  // 创建 Map 用于快速查找节点，key 使用字符串类型确保匹配
  const nodeMap = new Map()
  const rootList = []
  
  // 第一遍：遍历所有评论，初始化节点并加入 Map
  flatList.forEach(comment => {
    // 为每个节点创建带 children 数组的新对象
    const node = {
      ...comment,
      children: []
    }
    // 同时用数字和字符串两种 key 存储，确保查找时能匹配
    nodeMap.set(comment.id, node)
    nodeMap.set(String(comment.id), node)
  })
  
  console.log('节点 Map 大小:', nodeMap.size)
  
  // 第二遍：遍历所有评论，构建树形结构
  flatList.forEach(comment => {
    // 获取当前节点的引用
    const node = nodeMap.get(comment.id)
    if (!node) return
    
    // 获取 parentId，并转换为字符串进行判断
    const parentId = comment.parentId
    const parentIdStr = String(parentId)
    
    console.log(`处理评论 ID=${comment.id}, parentId=${parentId} (${typeof parentId}), content=${comment.content?.substring(0, 20)}`)
    
    // 判断是否为根节点：parentId 为 0、'0'、null、undefined、空字符串
    const isRoot = (
      parentId === null || 
      parentId === undefined || 
      parentId === 0 || 
      parentId === '0' || 
      parentId === '' ||
      parentId === 'null' ||
      parentId === 'undefined'
    )
    
    if (isRoot) {
      // 是根节点，加入根列表
      rootList.push(node)
      console.log(`  -> 加入根节点列表`)
    } else {
      // 是子节点，查找父节点
      const parent = nodeMap.get(parentId) || nodeMap.get(parentIdStr)
      if (parent) {
        parent.children.push(node)
        console.log(`  -> 加入父节点 ID=${parentId} 的 children，当前父节点有 ${parent.children.length} 个子评论`)
      } else {
        // 如果找不到父节点（可能父评论不在当前分页内），也加入根列表
        console.log(`  -> 警告：找不到父节点 ID=${parentId}，当作根节点处理`)
        rootList.push(node)
      }
    }
  })
  
  console.log('构建完成，根节点数量:', rootList.length)
  
  // 打印树形结构的详细信息
  const printTree = (nodes, level = 0) => {
    nodes.forEach(node => {
      const indent = '  '.repeat(level)
      console.log(`${indent}- ${node.id}: ${node.content?.substring(0, 30)}... (children: ${node.children.length})`)
      if (node.children.length > 0) {
        printTree(node.children, level + 1)
      }
    })
  }
  console.log('树形结构:')
  printTree(rootList)
  
  return rootList
}

const handleLike = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await toggleLike(article.value.id)
    // 重新获取文章详情以更新状态和数量
    await fetchArticle()
    ElMessage.success(isLiked.value ? '点赞成功' : '已取消点赞')
  } catch (error) {
    console.error('点赞失败:', error.response?.data || error.message)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('点赞失败')
    }
  }
}

const handleFavorite = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await toggleFavorite(article.value.id)
    // 重新获取文章详情以更新状态和数量
    await fetchArticle()
    ElMessage.success(isFavorited.value ? '收藏成功' : '已取消收藏')
  } catch (error) {
    console.error('收藏失败:', error.response?.data || error.message)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('收藏失败')
    }
  }
}

const handlePublishComment = async () => {
  if (!commentContent.value.trim() || commentSubmitting.value || emotionLoading.value) return

  // 第一步：AI 情绪检测
  emotionLoading.value = true
  try {
    const emotionRes = await detectEmotion(commentContent.value)
    const emotionData = emotionRes.data
    emotionLoading.value = false

    // 如果判定为负面情绪，拦截并提示
    if (emotionData.is_negative) {
      ElMessageBox.alert(
        `检测到您的评论包含${emotionData.emotion_type || '负面'}情绪，请注意友善交流哦~`,
        '温馨提示',
        {
          confirmButtonText: '知道了',
          type: 'warning',
          showClose: false,
          closeOnClickModal: false,
          closeOnPressEscape: false
        }
      )
      commentContent.value = ''
      return
    }
  } catch (error) {
    emotionLoading.value = false
    console.error('情绪检测失败:', error)
    // 情绪检测服务异常时放行，不阻塞评论
  }

  // 第二步：发表评论
  commentSubmitting.value = true
  try {
    await publishComment({
      articleId: article.value.id,
      content: commentContent.value
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    article.value.commentCount += 1
    commentPage.value = 1
    fetchComments()
    refreshIdempotentToken()
  } catch (error) {
    console.error('评论失败:', error.response?.data || error.message)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '评论失败')
    }
  } finally {
    commentSubmitting.value = false
  }
}

// ========== 阅读增强功能 ==========

// 切换沉浸式模式
const toggleImmersiveMode = () => {
  isImmersive.value = !isImmersive.value
  if (isImmersive.value) {
    document.documentElement.requestFullscreen?.()
  } else {
    document.exitFullscreen?.()
  }
}

// 滚动事件处理（防抖）
const handleScroll = () => {
  if (scrollTimer) clearTimeout(scrollTimer)
  scrollTimer = setTimeout(() => {
    const scrollTop = articleBodyRef.value?.scrollTop || 0
    const scrollHeight = articleBodyRef.value?.scrollHeight || 0
    const clientHeight = articleBodyRef.value?.clientHeight || 0
    const readPercentage = scrollHeight > 0 ? (scrollTop / (scrollHeight - clientHeight)) * 100 : 0
    
    // 保存到本地存储
    localStorage.setItem(`reading_progress_${article.value.id}`, JSON.stringify({
      scrollPosition: scrollTop,
      readPercentage: readPercentage.toFixed(2),
      timestamp: Date.now()
    }))
    
    // 如果用户已登录，同步到后端
    if (userStore.token) {
      saveReadingProgress({
        articleId: article.value.id,
        scrollPosition: Math.round(scrollTop),
        readPercentage: parseFloat(readPercentage.toFixed(2))
      }).catch(err => console.error('保存阅读进度失败:', err.response?.data || err.message))
    }
  }, 500)
}

// 添加书签
const addBookmark = async () => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    return
  }
  
  const position = articleBodyRef.value?.scrollTop || 0
  try {
    await addBookmarkApi({
      articleId: article.value.id,
      position: Math.round(position),
      note: ''
    })
    ElMessage.success('书签添加成功')
    // 立即刷新书签列表
    await loadBookmarks()
  } catch (error) {
    console.error('添加书签失败:', error.response?.data || error.message)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('添加书签失败')
    }
  }
}

// 加载书签列表
const loadBookmarks = async () => {
  if (!userStore.token) return
  try {
    const res = await getBookmarksByArticle(article.value.id)
    bookmarks.value = res.data || []
  } catch (error) {
    // 401 错误不显示，避免打扰用户
    if (error.response?.status !== 401) {
      console.error('加载书签失败:', error.response?.data || error.message)
    }
  }
}

// 跳转到书签位置
const jumpToBookmark = (position) => {
  if (articleBodyRef.value) {
    articleBodyRef.value.scrollTo({
      top: position,
      behavior: 'smooth'
    })
  }
  showBookmarks.value = false
}

// 删除书签
const removeBookmark = async (id) => {
  try {
    await deleteBookmarkApi(id)
    ElMessage.success('书签已删除')
    await loadBookmarks()
  } catch (error) {
    console.error('删除书签失败:', error.response?.data || error.message)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error('删除书签失败')
    }
  }
}

// 恢复阅读进度
const restoreReadingProgress = async () => {
  // 先尝试从后端获取（仅当用户已登录）
  if (userStore.token) {
    try {
      const res = await getReadingProgress(article.value.id)
      if (res.data && res.data.scrollPosition > 0) {
        savedScrollPosition.value = res.data.scrollPosition
        setTimeout(() => {
          if (articleBodyRef.value) {
            articleBodyRef.value.scrollTop = res.data.scrollPosition
          }
        }, 100)
        return
      }
    } catch (error) {
      // 401 错误不显示，避免打扰用户
      if (error.response?.status !== 401) {
        console.error('获取后端阅读进度失败:', error.response?.data || error.message)
      }
    }
  }
  
  // 如果后端没有，从本地存储获取
  const localProgress = localStorage.getItem(`reading_progress_${article.value.id}`)
  if (localProgress) {
    try {
      const progress = JSON.parse(localProgress)
      // 只恢复7天内的进度
      if (Date.now() - progress.timestamp < 7 * 24 * 60 * 60 * 1000) {
        savedScrollPosition.value = progress.scrollPosition
        setTimeout(() => {
          if (articleBodyRef.value) {
            articleBodyRef.value.scrollTop = progress.scrollPosition
          }
        }, 100)
      }
    } catch (error) {
      console.error('解析本地进度失败:', error)
    }
  }
}

// 监听主题和字体大小变化
watch([theme, fontSize], () => {
  const body = document.querySelector('.article-body .content')
  if (body) {
    body.className = `content theme-${theme.value}`
    body.style.fontSize = `${fontSize.value}px`
    localStorage.setItem('reader_theme', theme.value)
    localStorage.setItem('reader_fontSize', fontSize.value)
  }
})

// 页面离开前保存进度
const handleBeforeUnload = () => {
  if (article.value && articleBodyRef.value) {
    const scrollTop = articleBodyRef.value.scrollTop
    localStorage.setItem(`reading_progress_${article.value.id}`, JSON.stringify({
      scrollPosition: scrollTop,
      timestamp: Date.now()
    }))
  }
}

// 保存阅读历史（含阅读时长）
const saveHistoryOnLeave = () => {
  if (!article.value || !userStore.token) return
  
  const scrollTop = articleBodyRef.value?.scrollTop || 0
  const scrollHeight = articleBodyRef.value?.scrollHeight || 0
  const clientHeight = articleBodyRef.value?.clientHeight || 0
  const readPercentage = scrollHeight > clientHeight 
    ? (scrollTop / (scrollHeight - clientHeight)) * 100 
    : 100
  
  // 计算累计阅读时长
  if (readStartTime && isPageVisible) {
    totalReadDuration += Math.floor((Date.now() - readStartTime) / 1000)
    readStartTime = null
  }
  
  // 至少阅读了3秒才记录
  if (totalReadDuration < 3) return
  
  saveReadingHistory({
    articleId: article.value.id,
    readDuration: totalReadDuration,
    scrollPosition: Math.round(scrollTop),
    readPercentage: parseFloat(readPercentage.toFixed(2))
  }).catch(err => console.error('保存阅读历史失败:', err.response?.data || err.message))
}

// 监听页面可见性变化
const handleVisibilityChange = () => {
  if (document.hidden) {
    // 页面隐藏，累加当前阅读时长
    if (readStartTime) {
      totalReadDuration += Math.floor((Date.now() - readStartTime) / 1000)
      readStartTime = null
    }
    isPageVisible = false
  } else {
    // 页面重新可见，重新开始计时
    readStartTime = Date.now()
    isPageVisible = true
  }
}

onMounted(() => {
  fetchArticle()
  fetchComments()
  
  // 预获取幂等性Token（仅登录用户需要）
  if (userStore.token) {
    refreshIdempotentToken()
  }
  
  // 加载阅读设置
  const savedTheme = localStorage.getItem('reader_theme')
  const savedFontSize = localStorage.getItem('reader_fontSize')
  if (savedTheme) theme.value = savedTheme
  if (savedFontSize) fontSize.value = parseInt(savedFontSize)
  
  // 应用主题和字体
  watch([theme, fontSize], () => {
    const body = document.querySelector('.article-body .content')
    if (body) {
      body.className = `content theme-${theme.value}`
      body.style.fontSize = `${fontSize.value}px`
    }
  }, { immediate: true })
  
  // 监听页面离开
  window.addEventListener('beforeunload', handleBeforeUnload)
  window.addEventListener('beforeunload', saveHistoryOnLeave)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  
  // 开始阅读计时
  readStartTime = Date.now()
})

onUnmounted(() => {
  // 清理监听器
  window.removeEventListener('beforeunload', handleBeforeUnload)
  window.removeEventListener('beforeunload', saveHistoryOnLeave)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  if (scrollTimer) clearTimeout(scrollTimer)
  
  // 保存阅读历史
  saveHistoryOnLeave()
})

// 监听文章加载完成后恢复进度
watch(() => article.value, (newArticle) => {
  if (newArticle) {
    restoreReadingProgress()
    loadBookmarks()
    // 切换文章时重置阅读时长
    totalReadDuration = 0
    readStartTime = Date.now()
  }
}, { immediate: false })
</script>

<style lang="scss" scoped>
.article-detail-page {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 40px 32px;
  box-shadow: var(--shadow-card);
  max-width: var(--content-max-width);
  margin: 0 auto;

  // 阅读工具栏 — 毛玻璃效果
  .reading-toolbar {
    display: flex;
    gap: 10px;
    margin-bottom: 28px;
    padding: 10px 14px;
    background: rgba(40, 65, 57, 0.06);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    border-radius: var(--radius-md);
    border: 1px solid var(--border-color-light);

    .el-button {
      flex: 1;
      font-family: var(--font-sans);
      font-size: 13px;
      color: var(--color-text-secondary);
      
      &:hover {
        color: var(--color-primary);
      }
    }
  }

  .article-header {
    margin-bottom: 36px;
    padding-bottom: 24px;
    border-bottom: 1px solid var(--border-color);

    .article-title {
      margin: 0 0 16px 0;
      font-family: var(--font-serif);
      font-size: 30px;
      font-weight: 600;
      color: var(--color-text-main);
      line-height: 1.45;
      letter-spacing: 0.01em;
    }

    .article-meta {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      font-family: var(--font-sans);
      font-size: 13px;
      color: var(--color-text-muted);

      .author {
        color: var(--color-primary);
        font-weight: 500;
      }

      .read-time {
        color: var(--color-accent);
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 4px;
        
        .el-icon {
          font-size: 13px;
        }
      }

      .divider {
        color: var(--color-secondary-light);
      }

      .tags {
        margin-left: auto;
        display: flex;
        gap: 6px;
      }
    }
  }

  .article-body {
    .content {
      font-family: var(--font-serif);
      font-size: var(--font-size-body);
      line-height: var(--line-height-reading);
      color: var(--color-text-main);
      min-height: 200px;
      max-width: 100%;
      word-break: break-word;

      :deep(p) {
        margin: 0 0 var(--paragraph-spacing) 0;
        text-align: justify;
        text-justify: inter-word;
      }

      :deep(img) {
        max-width: 100%;
        border-radius: var(--radius-xs);
      }

      :deep(code) {
        background: rgba(128, 144, 118, 0.1);
        padding: 2px 6px;
        border-radius: var(--radius-xs);
        font-family: var(--font-mono);
        font-size: 14px;
        color: var(--color-primary);
      }

      :deep(pre) {
        background: rgba(40, 65, 57, 0.04);
        padding: 16px;
        border-radius: var(--radius-sm);
        overflow-x: auto;
        border: 1px solid var(--border-color-light);
      }

      :deep(blockquote) {
        border-left: 3px solid var(--color-primary);
        padding: 14px 20px;
        margin: 20px 0;
        background: rgba(128, 144, 118, 0.06);
        color: var(--color-text-secondary);
        font-style: italic;
        border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
      }

      :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
        font-family: var(--font-sans);
        color: var(--color-text-main);
        margin: 1.8em 0 0.8em 0;
      }
    }
  }

  .article-actions {
    display: flex;
    gap: 12px;
    margin: 36px 0;
    padding: 20px 0;
    border-top: 1px solid var(--border-color);
    border-bottom: 1px solid var(--border-color);

    .el-button {
      font-family: var(--font-sans);
      
      .el-icon {
        margin-right: 4px;
      }

      &.active {
        border-color: var(--color-accent);
        color: var(--color-accent);
        background: rgba(184, 104, 48, 0.06);
      }
      
      &:hover {
        border-color: var(--color-accent);
        color: var(--color-accent);
      }
    }
  }

  .comment-section {
    .section-title {
      margin: 0 0 20px 0;
      font-family: var(--font-sans);
      font-size: 18px;
      font-weight: 600;
      color: var(--color-text-main);
    }
  }
  
  // 移动端适配
  @media (max-width: 768px) {
    padding: 24px 16px;
    border-radius: 0;
    
    .article-header .article-title {
      font-size: 24px;
    }
    
    .article-body .content {
      font-size: var(--font-size-body-mobile);
    }
  }
}

// 阅读主题样式（非 scoped，需要全局应用）
:deep(.article-body) {
  overflow-y: auto;
  max-height: calc(100vh - 300px);
  
  .content {
    font-family: var(--font-serif);
    font-size: var(--font-size-body);
    line-height: var(--line-height-reading);
    color: var(--color-text-main);
    min-height: 200px;
    transition: all 0.3s ease;
    padding: 20px;
    border-radius: var(--radius-md);

    // 日间模式（默认）
    &.theme-light {
      background: #fff;
      color: var(--color-text-main);
    }

    // 护眼模式
    &.theme-sepia {
      background: var(--color-bg-paper-light);
      color: #5b4636;
    }

    // 夜间模式
    &.theme-dark {
      background: #1a1a1a;
      color: #d0d0d0;
    }

    :deep(p) {
      margin: 0 0 var(--paragraph-spacing) 0;
    }

    :deep(img) {
      max-width: 100%;
      border-radius: var(--radius-xs);
    }

    :deep(code) {
      background: rgba(128, 144, 118, 0.1);
      padding: 2px 6px;
      border-radius: var(--radius-xs);
      font-family: var(--font-mono);
      font-size: 14px;
    }

    :deep(pre) {
      background: rgba(40, 65, 57, 0.04);
      padding: 16px;
      border-radius: var(--radius-sm);
      overflow-x: auto;
      border: 1px solid var(--border-color-light);
    }

    :deep(blockquote) {
      border-left: 3px solid var(--color-primary);
      padding: 14px 20px;
      margin: 20px 0;
      background: rgba(128, 144, 118, 0.06);
      color: var(--color-text-secondary);
      font-style: italic;
      border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
    }
  }
}

// 书签列表样式
.bookmarks-list {
  padding: 10px;

  .bookmark-item {
    padding: 12px;
    margin-bottom: 10px;
    background: #f5f5f5;
    border-radius: 6px;
    transition: all 0.2s;

    &:hover {
      background: #e8e8e8;
    }

    .bookmark-info {
      margin-bottom: 8px;

      .bookmark-position {
        font-size: 13px;
        color: var(--color-accent);
        font-weight: 500;
        margin-bottom: 4px;
      }

      .bookmark-note {
        font-size: 14px;
        color: #333;
        margin-bottom: 4px;
      }

      .bookmark-time {
        font-size: 12px;
        color: #999;
      }
    }

    .bookmark-actions {
      display: flex;
      gap: 8px;
    }
  }
}
</style>
