<template>
  <div class="customer-service">
    <!-- 悬浮按钮 -->
    <transition name="fade">
      <div 
        v-if="!isExpanded" 
        class="float-button"
        @click="isExpanded = true"
      >
        <el-icon><ChatDotRound /></el-icon>
        <span class="badge" v-if="unreadCount > 0">{{ unreadCount }}</span>
      </div>
    </transition>

    <!-- 客服对话框 -->
    <transition name="slide">
      <div v-if="isExpanded" class="chat-window">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-info">
            <el-icon class="avatar"><Service /></el-icon>
            <div class="text">
              <div class="title">智能客服</div>
              <div class="status">
                <span class="dot"></span>
                在线
              </div>
            </div>
          </div>
          <el-icon class="close" @click="isExpanded = false"><Close /></el-icon>
        </div>

        <!-- 聊天内容 -->
        <div class="chat-content" ref="chatContentRef">
          <!-- 欢迎消息 -->
          <div class="message bot-message">
            <div class="message-bubble">
              您好！我是 FanFaction 智能客服，请问有什么可以帮助您的？
            </div>
          </div>

          <!-- 常见问题列表 -->
          <div class="faq-section">
            <div class="faq-title">常见问题</div>
            <div 
              v-for="(faq, index) in faqList" 
              :key="index"
              class="faq-item"
              @click="handleFaqClick(faq.question)"
            >
              <el-icon><QuestionFilled /></el-icon>
              <span>{{ faq.question }}</span>
            </div>
          </div>

          <!-- 对话历史 -->
          <div 
            v-for="(msg, index) in messages" 
            :key="index"
            :class="['message', msg.type === 'user' ? 'user-message' : 'bot-message']"
          >
            <div :class="['message-bubble', msg.type]">
              <div v-if="msg.type === 'user'">{{ msg.content }}</div>
              <div v-else v-html="msg.content"></div>
              <!-- 流式响应加载动画 -->
              <div v-if="msg.loading" class="loading-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            placeholder="输入您的问题..."
            @keyup.enter="sendMessage"
            :disabled="isStreaming"
          >
            <template #append>
              <el-button @click="sendMessage" :loading="isStreaming">
                <el-icon><Promotion /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ChatDotRound, Service, Close, QuestionFilled, Promotion } from '@element-plus/icons-vue'

const isExpanded = ref(false)
const unreadCount = ref(0)
const inputMessage = ref('')
const isStreaming = ref(false)
const chatContentRef = ref(null)
const messages = ref([])

// 预设常见问题
const faqList = ref([
  { question: '如何成为创作者？', answer: '点击导航栏的"成为创作者"，填写申请表单，提交后等待审核即可。审核通过后您就可以发布文章啦！' },
  { question: '如何发布文章？', answer: '成为创作者后，在个人中心点击"发布文章"，填写标题、内容和标签，然后点击发布即可。' },
  { question: '如何收藏/点赞文章？', answer: '在文章详情页，点击右上角的星星图标收藏文章，点击大拇指图标为文章点赞。' },
  { question: '如何评论和回复？', answer: '在文章底部的评论区域输入评论内容并发表。点击评论的"回复"按钮可以回复其他用户。' },
  { question: '阅读历史在哪里查看？', answer: '在导航栏点击"阅读历史"，即可查看您最近阅读过的文章列表。' },
  { question: '如何删除自己的评论？', answer: '目前暂不支持删除评论，后续会添加此功能。' }
])

// 处理常见问题点击
const handleFaqClick = async (question) => {
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: question
  })
  
  await nextTick()
  scrollToBottom()
  
  // 模拟机器人回复（预留大模型接口）
  await simulateBotResponse(question)
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isStreaming.value) return
  
  const userMsg = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: userMsg
  })
  
  await nextTick()
  scrollToBottom()
  
  // 调用大模型接口（预留）
  await callLLM(userMsg)
}

// 模拟机器人回复（后续替换为真实大模型接口）
const simulateBotResponse = async (question) => {
  isStreaming.value = true
  
  // 添加加载中的消息
  const loadingMsg = {
    type: 'bot',
    content: '',
    loading: true
  }
  messages.value.push(loadingMsg)
  
  await nextTick()
  scrollToBottom()
  
  // 模拟延迟
  await new Promise(resolve => setTimeout(resolve, 1000))
  
  // 查找匹配的 FAQ 答案
  const faq = faqList.value.find(f => f.question === question)
  const answer = faq ? faq.answer : '抱歉，我不太理解您的问题。您可以尝试选择上面的常见问题，或者联系人工客服。'
  
  // 移除加载消息，添加真实回复
  messages.value.pop()
  messages.value.push({
    type: 'bot',
    content: answer
  })
  
  isStreaming.value = false
  await nextTick()
  scrollToBottom()
}

// 调用智能客服 API
const callLLM = async (message) => {
  isStreaming.value = true
  
  // 添加加载中的消息
  const loadingMsg = {
    type: 'bot',
    content: '',
    loading: true
  }
  messages.value.push(loadingMsg)
  
  await nextTick()
  scrollToBottom()
  
  try {
    // 调用后端智能客服接口
    const response = await fetch('/api/chat/ask', {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
      },
      body: JSON.stringify({ question: message })
    })
    
    // 检查HTTP状态码
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const result = await response.json()
    
    // 检查响应结构
    if (result && result.code === 200 && result.data) {
      messages.value.pop()
      // 提取响应内容：result.data.response
      const botResponse = result.data.response || result.data.message || '暂无回答'
      messages.value.push({
        type: 'bot',
        content: botResponse
      })
    } else {
      messages.value.pop()
      const errorMsg = result?.message || '抱歉，暂时无法处理您的问题。'
      messages.value.push({
        type: 'bot',
        content: errorMsg
      })
    }
  } catch (error) {
    console.error('智能客服调用失败:', error)
    messages.value.pop()
    messages.value.push({
      type: 'bot',
      content: '网络开小差了，请稍后再试。'
    })
  } finally {
    isStreaming.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatContentRef.value) {
    chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
  }
}

onMounted(() => {
  // 可以在这里检查是否有未读消息
})
</script>

<style lang="scss" scoped>
.customer-service {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 9999;
}

.float-button {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
  position: relative;
  
  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.6);
  }
  
  .el-icon {
    font-size: 28px;
    color: #fff;
  }
  
  .badge {
    position: absolute;
    top: -5px;
    right: -5px;
    background: #f56c6c;
    color: #fff;
    border-radius: 10px;
    padding: 2px 6px;
    font-size: 12px;
    font-weight: bold;
  }
}

.chat-window {
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  .chat-header {
    padding: 16px 20px;
    background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .avatar {
        font-size: 32px;
        color: #fff;
      }
      
      .text {
        .title {
          color: #fff;
          font-size: 16px;
          font-weight: 600;
        }
        
        .status {
          display: flex;
          align-items: center;
          gap: 6px;
          color: rgba(255, 255, 255, 0.9);
          font-size: 12px;
          
          .dot {
            width: 8px;
            height: 8px;
            background: var(--color-accent);
            border-radius: 50%;
            animation: pulse 2s infinite;
          }
        }
      }
    }
    
    .close {
      font-size: 20px;
      color: #fff;
      cursor: pointer;
      transition: transform 0.3s;
      
      &:hover {
        transform: rotate(90deg);
      }
    }
  }
  
  .chat-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    background: #f5f7fa;
    
    .message {
      margin-bottom: 16px;
      display: flex;
      
      &.user-message {
        justify-content: flex-end;
        
        .message-bubble {
          background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
          color: #fff;
          border-radius: 16px 16px 4px 16px;
        }
      }
      
      &.bot-message {
        justify-content: flex-start;
        
        .message-bubble {
          background: #fff;
          color: #333;
          border-radius: 16px 16px 16px 4px;
        }
      }
      
      .message-bubble {
        max-width: 80%;
        padding: 12px 16px;
        font-size: 14px;
        line-height: 1.6;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }
    
    .faq-section {
      margin: 16px 0;
      
      .faq-title {
        font-size: 14px;
        color: #999;
        margin-bottom: 8px;
        padding-left: 8px;
      }
      
      .faq-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 12px;
        background: #fff;
        border-radius: 8px;
        margin-bottom: 8px;
        cursor: pointer;
        transition: all 0.3s;
        font-size: 13px;
        color: var(--color-accent);
        
        &:hover {
          background: rgba(128, 144, 118, 0.1);
          transform: translateX(4px);
        }
        
        .el-icon {
          font-size: 16px;
        }
      }
    }
  }
  
  .chat-input {
    padding: 12px 16px;
    background: #fff;
    border-top: 1px solid #e4e7ed;
    
    :deep(.el-input__wrapper) {
      border-radius: 20px;
    }
    
    :deep(.el-input-group__append) {
      border-radius: 0 20px 20px 0;
      background: linear-gradient(135deg, #284139 0%, #1a2d26 100%);
      color: #fff;
      
      .el-button {
        background: transparent;
        border: none;
        color: #fff;
        
        &:hover {
          background: transparent;
        }
      }
    }
  }
}

// 加载动画
.loading-dots {
  display: flex;
  gap: 4px;
  margin-top: 8px;
  
  span {
    width: 8px;
    height: 8px;
    background: var(--color-primary);
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out both;
    
    &:nth-child(1) {
      animation-delay: -0.32s;
    }
    
    &:nth-child(2) {
      animation-delay: -0.16s;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s;
}

.slide-enter-from {
  opacity: 0;
  transform: translateY(20px) translateX(20px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateY(20px) translateX(20px);
}
</style>
