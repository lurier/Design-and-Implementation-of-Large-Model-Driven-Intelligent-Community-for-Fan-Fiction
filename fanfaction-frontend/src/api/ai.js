import request from '@/utils/request'

/**
 * AI 智能摘要生成
 * @param {string} content - 文章纯文本内容
 */
export function generateSummary(content) {
  return request.post('/ai/summary', { content })
}

/**
 * AI 智能标签推荐
 * @param {string} content - 文章纯文本内容
 */
export function recommendTags(content) {
  return request.post('/ai/tags', { content })
}

/**
 * AI 评论情绪检测
 * @param {string} content - 评论内容
 * @returns {Promise<{is_negative: boolean, emotion_type: string}>}
 */
export function detectEmotion(content) {
  return request.post('/ai/emotion', { content })
}
