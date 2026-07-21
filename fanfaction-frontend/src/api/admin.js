import request from '@/utils/request'

export function getAdminCommentList(params) {
  return request.get('/admin/comments/list', { params })
}

export function getAllAdminComments(params) {
  return request.get('/admin/comments/all', { params })
}

export function updateCommentStatus(data) {
  return request.post('/admin/comment/updateStatus', data)
}

export function batchUpdateCommentStatus(data) {
  return request.post('/admin/comment/batchUpdateStatus', data)
}

export function deleteComment(id) {
  return request.delete(`/admin/comment/${id}`)
}

export function getAdminArticleList(params) {
  return request.get('/admin/articles/list', { params })
}

export function getAllAdminArticles(params) {
  return request.get('/admin/articles/all', { params })
}

export function auditArticle(data) {
  return request.post('/admin/article/audit', data)
}

export function deleteArticle(id) {
  return request.delete(`/admin/article/${id}`)
}

export function batchDeleteArticles(data) {
  return request.post('/admin/articles/batchDelete', data)
}

export function getAdminUserList(params) {
  return request.get('/admin/users/list', { params })
}

export function updateUserStatus(data) {
  return request.post('/admin/user/updateStatus', data)
}

export function updateUserRole(data) {
  return request.post('/admin/user/updateRole', data)
}

export function getDashboardStats() {
  return request.get('/admin/dashboard/stats')
}

export function getAuditPendingList(params) {
  return request.get('/admin/audit/pending-list', { params })
}

export function aiPreAudit(data) {
  return request.post('/admin/audit/ai-pre-audit', data)
}

export function handleAudit(data) {
  return request.post('/admin/audit/handle', data)
}

// ==================== 标签管理 ====================

export function getTagList(params) {
  return request.get('/admin/tags', { params })
}

export function addTag(data) {
  return request.post('/admin/tags', data)
}

export function updateTag(id, data) {
  return request.put(`/admin/tags/${id}`, data)
}

export function deleteTag(id) {
  return request.delete(`/admin/tags/${id}`)
}

export function toggleTagStatus(id) {
  return request.put(`/admin/tags/${id}/toggle`)
}
