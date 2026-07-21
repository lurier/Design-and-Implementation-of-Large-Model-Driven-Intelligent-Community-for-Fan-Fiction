package com.fanfaction.service;

import java.util.List;

/**
 * 智能客服服务接口
 */
public interface ChatService {

    /**
     * 获取客服响应
     *
     * @param userId 用户ID
     * @param question 用户问题
     * @return 客服回复
     */
    String getChatResponse(Long userId, String question);

    /**
     * 搜索相关文章（用于RAG）
     *
     * @param keyword 关键词
     * @param limit 返回数量
     * @return 文章列表（标题+摘要）
     */
    List<ArticleSearchResult> searchArticles(String keyword, int limit);

    /**
     * 文章搜索结果
     */
    record ArticleSearchResult(Long id, String title, String summary) {}
}