import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'

// 封禁弹窗标志位，防止多个请求同时触发多次弹窗
let isShowingBlockDialog = false

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 关闭 POST 请求的自动重试（如使用 axios-retry，则强制设为 0）
    if (config.method === 'post') {
      config.retry = 0
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 处理账号被封禁的逻辑
 * - 调用 logout 清空用户状态
 * - 弹出不可关闭的提示框
 * - 点击确定后跳转登录页
 */
function handleAccountBlocked() {
  // 防重复弹窗：如果已经在显示弹窗，直接返回
  if (isShowingBlockDialog) {
    return
  }
  isShowingBlockDialog = true

  // 调用 logout 清空用户状态
  const userStore = useUserStore()
  userStore.logout()

  // 弹出不可关闭的提示框
  ElMessageBox.alert(
    '您的账号因违规已被管理员封禁，如有疑问请联系客服。',
    '账号封禁',
    {
      confirmButtonText: '确定',
      type: 'error',
      showClose: false,           // 不显示关闭按钮
      closeOnClickModal: false,   // 点击遮罩层不关闭
      closeOnPressEscape: false,  // 按 ESC 不关闭
      callback: () => {
        isShowingBlockDialog = false  // 重置标志位
        router.push('/login')
      }
    }
  )
}

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      // 检查是否是账号被封禁的特殊错误码
      if (res.code === 40301) {
        handleAccountBlocked()
        return Promise.reject(new Error(res.message || '账号被封禁'))
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 403 && data?.code === 40301) {
        // 账号被封禁
        handleAccountBlocked()
      } else if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else {
        ElMessage.error(data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request
