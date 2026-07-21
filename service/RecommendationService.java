package com.fanfaction.service;

import com.fanfaction.entity.Article;

import java.util.List;

public interface RecommendationService {
    
    /**
     * 基于UserCF算法为指定用户推荐文章
     * @param userId 用户ID
     * @param topN 推荐数量
     * @return 推荐的文章列表
     */
    List<Article> recommendArticlesForUser(Long userId, int topN);
    
    /**
     * 计算两个用户之间的余弦相似度
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 相似度分值 (0-1)
     */
    double calculateUserSimilarity(Long userId1, Long userId2);
}
