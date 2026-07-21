import request from '@/utils/request'

/**
 * 获取个性化推荐文章
 * 后端从 JWT Token 中自动获取当前用户 ID，无需前端传递
 * @param {number} limit - 推荐数量，默认 10
 * @returns {Promise}
 */
export function getRecommendations(limit = 10) {
  return request.get('/recommend/articles', {
    params: { limit }
  })
}

// 获取后端生成的幂等性Token
export function getIdempotentToken() {
  return request.get('/idempotent/token')
}

// 当前可用的幂等性Token（页面加载时获取，提交后刷新）
let currentIdempotentToken = null

// 获取当前Token
export function getCurrentIdempotentToken() {
  return currentIdempotentToken
}

// 刷新Token（页面加载时或提交成功后调用）
export async function refreshIdempotentToken() {
  try {
    const res = await getIdempotentToken()
    currentIdempotentToken = res.data.token
    return currentIdempotentToken
  } catch (error) {
    console.error('获取幂等性Token失败:', error)
    return null
  }
}
