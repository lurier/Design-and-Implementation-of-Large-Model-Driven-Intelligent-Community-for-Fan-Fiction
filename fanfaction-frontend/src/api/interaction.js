import request from '@/utils/request'

export function toggleLike(articleId) {
  return request.post('/interactions/like', { articleId })
}

export function toggleFavorite(articleId) {
  return request.post('/interactions/favorite', { articleId })
}

// 获取后端生成的幂等性Token
export function getIdempotentToken() {
  return request.get('/idempotent/token')
}

// 当前可用的幂等性Token（页面加载时获取，提交后刷新）
let currentIdempotentToken = null

// 获取当前Token，如果为空则同步获取一个新的
export function getCurrentIdempotentToken() {
  if (!currentIdempotentToken) {
    // 同步阻塞获取（实际应该在页面加载时预获取）
    console.warn('幂等性Token为空，请确保在页面加载时调用 refreshIdempotentToken()')
  }
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

export function publishComment(data) {
  if (!currentIdempotentToken) {
    return Promise.reject(new Error('缺少幂等性Token'))
  }
  return request.post('/interactions/comment', data, {
    headers: {
      'Idempotent-Token': currentIdempotentToken
    }
  })
}

export function getCommentList(articleId, params) {
  return request.get(`/interactions/comments/${articleId}`, { params })
}

export function deleteComment(id) {
  return request.delete(`/interactions/comment/${id}`)
}

export function getUserFavorites() {
  return request.get('/interactions/user/favorites')
}

export function getUserLikedArticles() {
  return request.get('/interactions/user/likes')
}
