/**
 * 文本高亮工具函数
 */

/**
 * 将文本中的关键词用指定标签包裹，用于高亮显示
 * @param {string} text - 原始文本
 * @param {string} keyword - 要高亮的关键词
 * @param {string} highlightClass - 高亮样式类名（默认 'highlight'）
 * @returns {string} - 包含高亮标签的HTML字符串
 */
export function highlightKeyword(text, keyword, highlightClass = 'highlight') {
  if (!text || !keyword) return text
  
  // 转义特殊字符
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  
  // 使用正则表达式进行全局替换（不区分大小写）
  const regex = new RegExp(`(${escapedKeyword})`, 'gi')
  
  return text.replace(regex, `<span class="${highlightClass}">$1</span>`)
}

/**
 * 截断文本并添加省略号
 * @param {string} text - 原始文本
 * @param {number} maxLength - 最大长度
 * @returns {string} - 截断后的文本
 */
export function truncateText(text, maxLength = 100) {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}
