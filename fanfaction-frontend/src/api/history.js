import request from '@/utils/request'

// 保存阅读历史
export function saveReadingHistory(data) {
  return request.post('/reading-history', data)
}

// 获取阅读历史列表
export function getReadingHistory(params) {
  return request.get('/reading-history', { params })
}
