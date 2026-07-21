import request from '@/utils/request'

// 保存阅读进度
export function saveReadingProgress(data) {
  return request.post('/reading/progress', data)
}

// 获取阅读进度
export function getReadingProgress(articleId) {
  return request.get(`/reading/progress/${articleId}`)
}

// 添加书签
export function addBookmark(data) {
  return request.post('/reading/bookmark', data)
}

// 删除书签
export function deleteBookmark(id) {
  return request.delete(`/reading/bookmark/${id}`)
}

// 获取文章的所有书签
export function getBookmarksByArticle(articleId) {
  return request.get(`/reading/bookmarks/article/${articleId}`)
}

// 获取用户的所有书签
export function getUserBookmarks() {
  return request.get('/reading/bookmarks')
}
