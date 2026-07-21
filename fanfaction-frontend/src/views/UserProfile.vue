<template>
  <Layout>
    <div class="user-profile-page">
      <!-- 加载中 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 未登录提示 -->
      <div v-else-if="!userStore.token || !userStore.userInfo" class="not-logged-in">
        <el-empty description="请先登录">
          <el-button type="primary" @click="$router.push('/login')">去登录</el-button>
        </el-empty>
      </div>

      <!-- 用户信息卡片 -->
      <div v-else class="profile-header">
        <div class="profile-card">
          <div class="avatar-section">
            <el-avatar 
              :size="100" 
              :src="userStore.userInfo?.avatar || ''"
              class="user-avatar"
            >
              {{ userStore.userInfo?.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <div class="info-section">
            <div class="header-actions">
              <h2 class="nickname">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</h2>
              <el-button type="primary" size="small" @click="showSettings = true" class="settings-btn">
                <el-icon><Setting /></el-icon> 设置
              </el-button>
            </div>
            <p class="username">@{{ userStore.userInfo?.username }}</p>
            <div class="meta-info">
              <div class="info-item">
                <el-icon><Message /></el-icon>
                <span>{{ userStore.userInfo?.email || '未设置邮箱' }}</span>
              </div>
              <div class="info-item" v-if="userStore.userInfo?.phone">
                <el-icon><Phone /></el-icon>
                <span>{{ userStore.userInfo?.phone }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 设置对话框 -->
      <el-dialog v-model="showSettings" title="个人设置" width="500px">
        <el-form :model="profileForm" label-width="80px" label-position="left">
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="头像">
            <div class="avatar-upload">
              <el-avatar 
                :size="80" 
                :src="profileForm.avatar || ''"
                class="avatar-preview"
              >
                {{ profileForm.nickname?.charAt(0) || userStore.userInfo?.username?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="avatar-actions">
                <input 
                  ref="avatarInput" 
                  type="file" 
                  accept="image/*" 
                  style="display: none"
                  @change="handleAvatarChange"
                />
                <el-button size="small" @click="triggerAvatarUpload">
                  <el-icon><Upload /></el-icon> 选择头像
                </el-button>
                <el-button 
                  v-if="profileForm.avatar" 
                  size="small" 
                  type="danger" 
                  @click="profileForm.avatar = ''"
                >
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </div>
              <div class="avatar-tip">支持 JPG、PNG、GIF 格式，建议尺寸 200x200</div>
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="showSettings = false">取消</el-button>
            <el-button type="primary" @click="saveProfile" :loading="saving">保存</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- Tab 页签 -->
      <div v-if="userStore.token && userStore.userInfo" class="profile-content">
        <el-tabs v-model="activeTab" class="profile-tabs" @tab-change="handleTabChange">
          <el-tab-pane label="我发布的文章" name="published">
            <div class="article-list">
              <div 
                v-for="article in publishedArticles" 
                :key="article.id" 
                class="article-card"
                @click="$router.push(`/article/${article.id}`)"
              >
                <div class="article-content">
                  <h3 class="article-title">{{ article.title }}</h3>
                  <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
                  <div class="article-meta">
                    <span class="time">{{ formatTime(article.createTime) }}</span>
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
              <el-empty v-if="publishedArticles.length === 0" description="暂无发布的文章" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="我收藏的文章" name="favorites">
            <div class="article-list">
              <div 
                v-for="article in favoriteArticles" 
                :key="article.id" 
                class="article-card"
                @click="$router.push(`/article/${article.id}`)"
              >
                <div class="article-content">
                  <h3 class="article-title">{{ article.title }}</h3>
                  <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
                  <div class="article-meta">
                    <span class="author">作者：{{ article.authorNickname || article.authorName }}</span>
                    <span class="divider">·</span>
                    <span class="time">{{ formatTime(article.createTime) }}</span>
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
              <el-empty v-if="favoriteArticles.length === 0" description="暂无收藏的文章" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="我点赞的文章" name="likes">
            <div class="article-list">
              <div 
                v-for="article in likedArticles" 
                :key="article.id" 
                class="article-card"
                @click="$router.push(`/article/${article.id}`)"
              >
                <div class="article-content">
                  <h3 class="article-title">{{ article.title }}</h3>
                  <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
                  <div class="article-meta">
                    <span class="author">作者：{{ article.authorNickname || article.authorName }}</span>
                    <span class="divider">·</span>
                    <span class="time">{{ formatTime(article.createTime) }}</span>
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
              <el-empty v-if="likedArticles.length === 0" description="暂无点赞的文章" />
            </div>
          </el-tab-pane>

          <el-tab-pane label="创作者中心" name="creator">
            <div class="creator-section">
              <!-- 已申请状态 -->
              <div v-if="hasApplied" class="creator-status-card">
                <el-result
                  :icon="applicationStatus === 1 ? 'success' : applicationStatus === 2 ? 'error' : 'info'"
                  :title="statusText"
                  :sub-title="statusSubText"
                >
                  <template #extra>
                    <div class="status-details">
                      <div class="status-item">
                        <span class="label">笔名：</span>
                        <span>{{ creatorData?.penName }}</span>
                      </div>
                      <div class="status-item">
                        <span class="label">擅长领域：</span>
                        <span>{{ creatorData?.expertise }}</span>
                      </div>
                      <div class="status-item" v-if="creatorData?.reviewComment">
                        <span class="label">审核意见：</span>
                        <span>{{ creatorData?.reviewComment }}</span>
                      </div>
                      <div class="status-item">
                        <span class="label">申请时间：</span>
                        <span>{{ creatorData?.createTime }}</span>
                      </div>
                    </div>
                    <div class="status-actions">
                      <el-button 
                        v-if="applicationStatus === 1" 
                        type="primary" 
                        @click="$router.push('/creator/publish')"
                      >
                        发布文章
                      </el-button>
                      <el-button v-if="applicationStatus !== 1" @click="$router.push('/creator-apply')">
                        查看申请
                      </el-button>
                    </div>
                  </template>
                </el-result>
              </div>

              <!-- 未申请入口 -->
              <div v-else class="creator-apply-card">
                <div class="creator-header">
                  <el-icon class="creator-icon"><Edit /></el-icon>
                  <h3>成为创作者</h3>
                  <p>用文字创造无限可能，与同好共筑想象世界</p>
                </div>
                <div class="creator-benefits">
                  <div class="benefit-item">
                    <el-icon><Document /></el-icon>
                    <span>发布作品</span>
                  </div>
                  <div class="benefit-item">
                    <el-icon><User /></el-icon>
                    <span>建立影响力</span>
                  </div>
                  <div class="benefit-item">
                    <el-icon><ChatLineRound /></el-icon>
                    <span>互动交流</span>
                  </div>
                </div>
                <el-button type="primary" size="large" @click="$router.push('/creator-apply')">
                  立即申请
                </el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { Message, Phone, View, Star, ChatDotRound, Setting, Upload, Delete, Edit, Document, User, ChatLineRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import Layout from '@/components/Layout.vue'
import TagList from '@/components/TagList.vue'
import dayjs from 'dayjs'
import { getUserFavorites, getUserLikedArticles } from '@/api/interaction'
import { getUserPublishedArticles, updateUserProfile } from '@/api/auth'
import { getCreatorApplication } from '@/api/creator'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('published')
const showSettings = ref(false)
const saving = ref(false)
const loading = ref(true)
const avatarInput = ref(null)

// 真实数据
const publishedArticles = ref([])
const favoriteArticles = ref([])
const likedArticles = ref([])

// 创作者申请相关
const hasApplied = ref(false)
const creatorData = ref(null)
const applicationStatus = ref(0) // 0-审核中 1-通过 2-拒绝

// 状态文本
const statusText = computed(() => {
  if (applicationStatus.value === 1) return '申请已通过'
  if (applicationStatus.value === 2) return '申请未通过'
  return '审核中'
})

const statusSubText = computed(() => {
  if (applicationStatus.value === 1) return '您已成为创作者，可以开始发布文章啦！'
  if (applicationStatus.value === 2) return '抱歉，您的申请未通过审核，请查看审核意见'
  return '我们将在 3 个工作日内完成审核，请耐心等待'
})

// 个人资料表单
const profileForm = ref({
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

// 监听用户信息变化，填充表单
watch(() => userStore.userInfo, (newUserInfo) => {
  if (newUserInfo) {
    profileForm.value = {
      nickname: newUserInfo.nickname || '',
      email: newUserInfo.email || '',
      phone: newUserInfo.phone || '',
      avatar: newUserInfo.avatar || ''
    }
  }
}, { immediate: true })

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const handleTabChange = (tab) => {
  console.log('切换到 Tab:', tab)
}

const loadPublishedArticles = async () => {
  try {
    const res = await getUserPublishedArticles()
    publishedArticles.value = res.data?.records || []
  } catch (error) {
    console.error('加载发布的文章失败:', error)
    ElMessage.error('加载发布的文章失败')
  }
}

const loadFavoriteArticles = async () => {
  try {
    const res = await getUserFavorites()
    favoriteArticles.value = res.data || []
  } catch (error) {
    console.error('加载收藏的文章失败:', error)
    ElMessage.error('加载收藏的文章失败')
  }
}

const loadLikedArticles = async () => {
  try {
    const res = await getUserLikedArticles()
    likedArticles.value = res.data || []
  } catch (error) {
    console.error('加载点赞的文章失败:', error)
    ElMessage.error('加载点赞的文章失败')
  }
}

const loadAllArticles = async () => {
  await loadPublishedArticles()
  await loadFavoriteArticles()
  await loadLikedArticles()
  loading.value = false
}

// 加载创作者申请状态
const loadCreatorApplication = async () => {
  try {
    const res = await getCreatorApplication()
    if (res.data) {
      hasApplied.value = true
      creatorData.value = res.data
      applicationStatus.value = res.data.status
    }
  } catch (error) {
    // 404 表示未申请，不显示错误
    if (error.response?.status !== 404) {
      console.error('加载创作者申请状态失败:', error)
    }
    hasApplied.value = false
  }
}

const saveProfile = async () => {
  saving.value = true
  try {
    await updateUserProfile(profileForm.value)
    ElMessage.success('保存成功')
    showSettings.value = false
    // 重新获取用户信息
    await userStore.fetchUserInfo()
    // 同步更新全局状态（确保立即生效）
    userStore.updateUserInfo({
      nickname: profileForm.value.nickname,
      email: profileForm.value.email,
      phone: profileForm.value.phone,
      avatar: profileForm.value.avatar
    })
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 触发文件选择
const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

// 处理头像文件选择
const handleAvatarChange = (event) => {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('请选择 JPG、PNG 或 GIF 格式的图片')
    return
  }

  // 验证文件大小（不超过 5MB）
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }

  // 读取文件并转换为 Base64
  const reader = new FileReader()
  reader.onload = (e) => {
    profileForm.value.avatar = e.target.result
    ElMessage.success('头像已选择')
  }
  reader.onerror = () => {
    ElMessage.error('读取图片失败')
  }
  reader.readAsDataURL(file)
  
  // 清空 input，允许重复选择同一文件
  event.target.value = ''
}

onMounted(async () => {
  // 确保用户已登录
  if (!userStore.token) {
    loading.value = false
    return
  }
  
  // 等待用户信息加载完成
  if (!userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  
  // 加载用户文章数据
  await loadAllArticles()
  
  // 加载创作者申请状态
  await loadCreatorApplication()
})
</script>

<style lang="scss" scoped>
.user-profile-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;

  .loading-container {
    padding: 40px 20px;
  }

  .not-logged-in {
    padding: 100px 20px;
    text-align: center;
  }

  .profile-header {
    margin-bottom: 20px;

    .profile-card {
      background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
      border-radius: 12px;
      padding: 30px;
      display: flex;
      align-items: center;
      gap: 30px;
      color: white;
      box-shadow: 0 4px 12px rgba(40, 65, 57, 0.25);

      .avatar-section {
        flex-shrink: 0;

        .user-avatar {
          border: 4px solid rgba(255, 255, 255, 0.3);
          border-radius: 50%;
          object-fit: cover;
        }
      }

      .info-section {
        flex: 1;

        .header-actions {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 8px;

          .nickname {
            margin: 0;
            font-size: 28px;
            font-weight: 600;
            color: white;
          }

          .settings-btn {
            flex-shrink: 0;
            background: rgba(255, 255, 255, 0.2);
            border-color: rgba(255, 255, 255, 0.3);
            color: white;

            &:hover {
              background: rgba(255, 255, 255, 0.3);
            }
          }
        }

        .username {
          margin: 0 0 16px 0;
          font-size: 16px;
          color: rgba(255, 255, 255, 0.8);
        }

        .meta-info {
          display: flex;
          gap: 20px;
          flex-wrap: wrap;

          .info-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: rgba(255, 255, 255, 0.8);

            .el-icon {
              font-size: 16px;
            }
          }
        }
      }
    }
  }

  .profile-content {
    .profile-tabs {
      background: #fff;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

      :deep(.el-tabs__header) {
        margin-bottom: 24px;
      }

      :deep(.el-tabs__item) {
        font-size: 16px;
        padding: 12px 24px;
      }

      :deep(.el-tabs__content) {
        padding: 0;
      }
    }

    .article-list {
      .article-card {
        background: #fafafa;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 12px;
        cursor: pointer;
        transition: all 0.2s;
        border: 1px solid #eee;

        &:hover {
          background: #fff;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          transform: translateY(-2px);
          border-color: var(--color-accent);

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
            font-size: 14px;
            line-height: 1.6;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }

          .article-meta {
            display: flex;
            align-items: center;
            gap: 8px;
            font-family: var(--font-sans);
            font-size: 13px;
            color: var(--color-text-muted);

            .author {
              color: #666;
            }

            .divider {
              color: #ddd;
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
  }
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;

  .avatar-preview {
    border: 2px solid #e0e0e0;
    border-radius: 50%;
    object-fit: cover;
  }

  .avatar-actions {
    display: flex;
    gap: 8px;
  }

  .avatar-tip {
    font-size: 12px;
    color: #999;
    line-height: 1.5;
  }
}

.creator-section {
  padding: 20px;
  min-height: 400px;
}

.creator-status-card,
.creator-apply-card {
  max-width: 600px;
  margin: 0 auto;
}

.creator-status-card {
  :deep(.el-result__title) {
    font-size: 20px;
  }
  
  :deep(.el-result__subtitle) {
    font-size: 14px;
  }
  
  .status-details {
    margin: 24px 0;
    text-align: left;
    max-width: 400px;
    margin-left: auto;
    margin-right: auto;
    
    .status-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      
      .label {
        color: #666;
        font-size: 14px;
        font-weight: 500;
      }
      
      span {
        color: #333;
        font-size: 14px;
      }
    }
  }
  
  .status-actions {
    margin-top: 24px;
    display: flex;
    gap: 12px;
    justify-content: center;
  }
}

.creator-apply-card {
  text-align: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 16px;
  
  .creator-header {
    margin-bottom: 32px;
    
    .creator-icon {
      font-size: 64px;
      color: var(--color-accent);
      margin-bottom: 16px;
    }
    
    h3 {
      font-size: 24px;
      color: #333;
      margin-bottom: 12px;
    }
    
    p {
      font-size: 14px;
      color: #666;
      margin: 0;
    }
  }
  
  .creator-benefits {
    display: flex;
    justify-content: center;
    gap: 32px;
    margin-bottom: 32px;
    
    .benefit-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      
      .el-icon {
        font-size: 32px;
        color: var(--color-accent);
      }
      
      span {
        font-size: 14px;
        color: #666;
      }
    }
  }
}
</style>
