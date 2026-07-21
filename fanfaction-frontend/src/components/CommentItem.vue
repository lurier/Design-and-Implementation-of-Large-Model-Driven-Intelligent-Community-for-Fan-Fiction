<template>
  <div class="comment-item-wrapper">
    <!-- 一级评论 -->
    <div class="comment-item parent-comment">
      <div class="comment-avatar">
        <el-avatar :size="40">
          <img v-if="comment.userAvatar" :src="comment.userAvatar" alt="头像" />
          <span v-else class="avatar-text">
            {{ (comment.userNickname || comment.userName || 'U').charAt(0).toUpperCase() }}
          </span>
        </el-avatar>
      </div>
      <div class="comment-body">
        <div class="comment-header">
          <span class="comment-author">{{ comment.userNickname || comment.userName }}</span>
          <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          <el-button 
            v-if="userStore.token" 
            link 
            size="small"
            @click="handleReply"
            class="reply-btn"
          >
            回复
          </el-button>
        </div>
        
        <!-- 一级评论内容 -->
        <div class="comment-content">
          <span>{{ comment.content }}</span>
        </div>
        
        <!-- 回复输入框 -->
        <div v-if="showReplyInput" class="reply-input-wrapper">
          <el-input
            ref="replyInputRef"
            v-model="replyContent"
            type="textarea"
            :rows="2"
            placeholder="写下你的回复..."
            maxlength="500"
            show-word-limit
          />
          <div class="reply-actions">
            <el-button size="small" @click="cancelReply">取消</el-button>
            <el-button size="small" type="primary" @click="handleSubmitReply" :loading="replySubmitting || emotionLoading" :disabled="!replyContent.trim() || replySubmitting || emotionLoading">
              {{ emotionLoading ? 'AI 正在分析情绪...' : '发送' }}
            </el-button>
          </div>
        </div>
        
        <!-- 子评论区域 -->
        <div v-if="comment.children && comment.children.length > 0" class="children-comments-wrapper">
          <div class="children-comments">
            <!-- 只显示前2条或全部（根据展开状态） -->
            <div 
              v-for="(child, index) in displayedChildren" 
              :key="child.id" 
              class="child-comment"
            >
              <div class="child-avatar">
                <el-avatar :size="32">
                  <img v-if="child.userAvatar" :src="child.userAvatar" alt="头像" />
                  <span v-else class="avatar-text">
                    {{ (child.userNickname || child.userName || 'U').charAt(0).toUpperCase() }}
                  </span>
                </el-avatar>
              </div>
              <div class="child-body">
                <div class="child-header">
                  <span class="child-author">{{ child.userNickname || child.userName }}</span>
                  <span class="child-time">{{ formatTime(child.createTime) }}</span>
                </div>
                <div class="child-content">
                  <!-- @提及高亮 -->
                  <template v-if="child.replyUserName">
                    <span class="mention">@{{ child.replyUserName }}</span>
                    <span> {{ child.content }}</span>
                  </template>
                  <template v-else>
                    <span>{{ child.content }}</span>
                  </template>
                </div>
                <el-button 
                  v-if="userStore.token" 
                  link 
                  size="small"
                  @click="() => handleChildReply(child)"
                  class="child-reply-btn"
                >
                  回复
                </el-button>
              </div>
            </div>
            
            <!-- 展开/收起按钮 -->
            <div v-if="comment.children.length > 2" class="expand-btn-wrapper">
              <el-button 
                v-if="!showAllChildren"
                link 
                size="small"
                @click="showAllChildren = true"
                class="expand-btn"
              >
                展开 {{ comment.children.length - 2 }} 条回复
              </el-button>
              <el-button 
                v-else
                link 
                size="small"
                @click="showAllChildren = false"
                class="expand-btn"
              >
                收起
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { publishComment, refreshIdempotentToken } from '@/api/interaction'
import { detectEmotion } from '@/api/ai'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  },
  articleId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['reply'])

const userStore = useUserStore()
const showReplyInput = ref(false)
const replyContent = ref('')
const replySubmitting = ref(false)
const emotionLoading = ref(false)
const replyInputRef = ref(null)
const showAllChildren = ref(false)

// 根据展开状态决定显示的子评论数量
const displayedChildren = computed(() => {
  if (!props.comment.children || props.comment.children.length === 0) {
    return []
  }
  if (showAllChildren.value) {
    return props.comment.children
  }
  // 默认只显示前2条
  return props.comment.children.slice(0, 2)
})

const formatTime = (time) => dayjs(time).format('MM-DD HH:mm')

// 显示回复输入框（回复一级评论）
const handleReply = () => {
  showReplyInput.value = true
  replyContent.value = `@${props.comment.userNickname || props.comment.userName} `
  setTimeout(() => {
    replyInputRef.value?.focus()
    const textarea = replyInputRef.value?.textareaRef?.$el || replyInputRef.value?.$el
    if (textarea) {
      textarea.selectionStart = textarea.selectionEnd = replyContent.value.length
    }
  }, 100)
}

// 回复子评论
const handleChildReply = (child) => {
  showReplyInput.value = true
  replyContent.value = `@${child.userNickname || child.userName} `
  setTimeout(() => {
    replyInputRef.value?.focus()
    const textarea = replyInputRef.value?.textareaRef?.$el || replyInputRef.value?.$el
    if (textarea) {
      textarea.selectionStart = textarea.selectionEnd = replyContent.value.length
    }
  }, 100)
}

// 取消回复
const cancelReply = () => {
  showReplyInput.value = false
  replyContent.value = ''
}

// 提交回复
const handleSubmitReply = async () => {
  if (!replyContent.value.trim() || replySubmitting.value || emotionLoading.value) return
  
  // 第一步：AI 情绪检测
  emotionLoading.value = true
  try {
    const emotionRes = await detectEmotion(replyContent.value)
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
      replyContent.value = ''
      return
    }
  } catch (error) {
    emotionLoading.value = false
    console.error('情绪检测失败:', error)
    // 情绪检测服务异常时放行，不阻塞回复
  }

  // 第二步：发表回复
  replySubmitting.value = true
  try {
    await publishComment({
      articleId: props.articleId,
      content: replyContent.value,
      parentId: props.comment.id
    })
    ElMessage.success('回复成功')
    emit('reply')
    cancelReply()
    refreshIdempotentToken()
  } catch (error) {
    console.error('回复失败:', error.response?.data || error.message)
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '回复失败')
    }
  } finally {
    replySubmitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.comment-item-wrapper {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
  }
}

/* 一级评论 */
.parent-comment {
  display: flex;
  padding: 8px 0;
  
  .comment-avatar {
    flex-shrink: 0;
    margin-right: 12px;
    
    .el-avatar {
      background: linear-gradient(135deg, #284139 0%, #809076 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 50%;
      }
      
      .avatar-text {
        color: #fff;
        font-size: 16px;
        font-weight: 600;
      }
    }
  }
  
  .comment-body {
    flex: 1;
    min-width: 0;
  }
  
  .comment-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
    
    .comment-author {
      font-weight: 600;
      color: #1a1a1a;
      font-size: 14px;
    }
    
    .comment-time {
      font-size: 12px;
      color: #999;
      margin-left: auto;
      margin-right: 8px;
    }
    
    .reply-btn {
      padding: 0 8px;
      font-size: 12px;
      color: var(--color-accent);
      
      &:hover {
        color: var(--color-accent);
      }
    }
  }
  
  .comment-content {
    font-size: 14px;
    color: #333;
    line-height: 1.6;
    word-break: break-word;
  }
  
  .reply-input-wrapper {
    margin-top: 12px;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 8px;
    
    .el-textarea {
      margin-bottom: 8px;
      
      textarea {
        font-size: 14px;
      }
    }
    
    .reply-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
  }
}

/* 子评论区域 */
.children-comments-wrapper {
  margin-top: 12px;
}

.children-comments {
  background: #f5f5f5;
  border-radius: 12px;
  padding: 12px;
}

/* 子评论 */
.child-comment {
  display: flex;
  padding: 10px 0;
  
  &:first-child {
    padding-top: 0;
  }
  
  &:not(:last-child) {
    border-bottom: 1px solid #e8e8e8;
  }
  
  .child-avatar {
    flex-shrink: 0;
    margin-right: 10px;
    
    .el-avatar {
      background: linear-gradient(135deg, #284139 0%, #809076 100%);
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 50%;
      }

      .avatar-text {
        color: #fff;
        font-size: 12px;
        font-weight: 600;
      }
    }
  }
  
  .child-body {
    flex: 1;
    min-width: 0;
  }
  
  .child-header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 4px;
    
    .child-author {
      font-weight: 500;
      color: #1a1a1a;
      font-size: 13px;
    }
    
    .child-time {
      font-size: 11px;
      color: #999;
    }
  }
  
  .child-content {
    font-size: 13px;
    color: #333;
    line-height: 1.5;
    word-break: break-word;
    
    .mention {
      color: var(--color-primary);
      font-weight: 500;
    }
  }

  .child-reply-btn {
    padding: 0 6px;
    font-size: 11px;
    color: var(--color-primary);
    margin-top: 6px;

    &:hover {
      color: var(--color-accent);
    }
  }
}

/* 展开/收起按钮 */
.expand-btn-wrapper {
  padding-top: 8px;
  text-align: center;
  
  .expand-btn {
    font-size: 12px;
    color: #999;
    
    &:hover {
      color: var(--color-accent);
    }
  }
}
</style>
