import request from '@/utils/request'
import { refreshIdempotentToken, getCurrentIdempotentToken } from '@/api/recommend'

// 获取创作者申请状态
export function getCreatorApplication() {
  return request.get('/creator-application/my')
}

// 提交创作者申请
export function submitCreatorApplication(data) {
  return request.post('/creator-application', data)
}

// 发布作品
export async function publishWork(data) {
  await refreshIdempotentToken()
  return request.post('/articles', { ...data, status: 'PENDING' }, {
    headers: {
      'Idempotent-Token': getCurrentIdempotentToken()
    }
  })
}

// 保存草稿
export async function saveDraft(data) {
  await refreshIdempotentToken()
  return request.post('/articles', { ...data, status: 'DRAFT' }, {
    headers: {
      'Idempotent-Token': getCurrentIdempotentToken()
    }
  })
}

// 获取我的作品列表
export function getMyWorks(params) {
  return request.get('/articles/user/published', { params })
}

// 获取作品详情
export function getWorkDetail(id) {
  return request.get(`/articles/${id}`)
}

// 更新作品
export function updateWork(id, data) {
  return request.put(`/articles/${id}`, data)
}

// 删除作品
export function deleteWork(id) {
  return request.delete(`/articles/${id}`)
}

// 获取创作者统计数据
export function getCreatorStats() {
  return request.get('/creator/stats')
}

// 获取分类列表
export function getCategories() {
  return request.get('/categories')
}

// 获取 CP 标签列表
export function getCpTags() {
  return request.get('/cp-tags')
}
