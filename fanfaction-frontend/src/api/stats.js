import request from '@/utils/request'

// 获取用户阅读数据统计
export function getUserStats() {
  return request.get('/reader-stats')
}
