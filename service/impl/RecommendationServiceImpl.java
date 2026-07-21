package com.fanfaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Interaction;
import com.fanfaction.entity.ReadingHistory;
import com.fanfaction.service.ArticleService;
import com.fanfaction.service.InteractionService;
import com.fanfaction.service.ReadingHistoryService;
import com.fanfaction.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final InteractionService interactionService;
    private final ArticleService articleService;
    private final ReadingHistoryService readingHistoryService;

    @Override
    public List<Article> recommendArticlesForUser(Long userId, int topN) {
        System.out.println("开始计算推荐，用户ID: " + userId);
        log.info("开始为用户 {} 生成个性化推荐，推荐数量: {}", userId, topN);
        
        try {
            // 1. 获取用户最近交互的文章ID（点赞、收藏、阅读），最多取10篇
            Set<Long> userInteractedArticleIds = getUserRecentInteractedArticles(userId, 10);
            
            // 2. 如果用户没有交互记录，返回热门文章作为兜底
            if (userInteractedArticleIds.isEmpty()) {
                System.out.println("用户无历史记录，返回热门文章");
                log.info("用户 {} 没有交互记录，返回热门文章作为推荐", userId);
                return getHotArticles(topN);
            }

            // 3. 查询用户交互过的文章详情，提取标签
            List<Article> userArticles = userInteractedArticleIds.stream()
                .map(articleService::getById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            // 4. 统计标签出现次数，找出用户的核心兴趣标签
            Map<String, Integer> tagCountMap = new HashMap<>();
            for (Article article : userArticles) {
                if (article.getTags() != null && !article.getTags().isEmpty()) {
                    String[] tags = article.getTags().split(",");
                    for (String tag : tags) {
                        tag = tag.trim();
                        if (!tag.isEmpty()) {
                            tagCountMap.merge(tag, 1, Integer::sum);
                        }
                    }
                }
            }

            // 5. 按标签出现次数排序，取前5个核心标签
            List<String> coreTags = tagCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

            log.info("用户 {} 的核心兴趣标签: {}", userId, coreTags);

            // 6. 根据核心标签查找相似文章（排除已交互过的）
            List<Article> recommendedArticles = findSimilarArticlesByTags(coreTags, userInteractedArticleIds, topN);
            
            System.out.println("找到相似文章数量: " + recommendedArticles.size());
            if (recommendedArticles.isEmpty()) {
                System.out.println("同类文章已读完或无匹配标签文章");
            }

            // 7. 如果推荐结果不足，补充热门文章
            if (recommendedArticles.size() < topN) {
                int remaining = topN - recommendedArticles.size();
                List<Article> hotArticles = getHotArticles(remaining);
                
                // 过滤掉用户已交互过的文章
                hotArticles = hotArticles.stream()
                    .filter(article -> !userInteractedArticleIds.contains(article.getId()))
                    .collect(Collectors.toList());
                
                recommendedArticles.addAll(hotArticles);
            }

            log.info("为用户 {} 成功推荐 {} 篇文章", userId, recommendedArticles.size());
            return recommendedArticles;

        } catch (Exception e) {
            log.error("为用户 {} 生成推荐失败，返回热门文章作为兜底", userId, e);
            return getHotArticles(topN);
        }
    }

    @Override
    public double calculateUserSimilarity(Long userId1, Long userId2) {
        Set<Long> articles1 = getUserInteractedArticles(userId1);
        Set<Long> articles2 = getUserInteractedArticles(userId2);
        return calculateUserSimilarityWithSets(articles1, articles2);
    }

    /**
     * 获取用户最近交互的文章ID（包括点赞、收藏、阅读历史）
     */
    private Set<Long> getUserRecentInteractedArticles(Long userId, int limit) {
        Set<Long> articleIds = new LinkedHashSet<>();
        
        // 1. 获取点赞和收藏记录
        LambdaQueryWrapper<Interaction> interactionWrapper = new LambdaQueryWrapper<>();
        interactionWrapper.eq(Interaction::getUserId, userId)
               .in(Interaction::getType, 1, 2) // 1=点赞, 2=收藏
               .orderByDesc(Interaction::getCreateTime)
               .last("LIMIT " + limit);
        List<Interaction> interactions = interactionService.list(interactionWrapper);
        interactions.forEach(i -> articleIds.add(i.getArticleId()));
        
        // 2. 获取阅读历史记录
        LambdaQueryWrapper<ReadingHistory> readingWrapper = new LambdaQueryWrapper<>();
        readingWrapper.eq(ReadingHistory::getUserId, userId)
               .orderByDesc(ReadingHistory::getLastReadTime)
               .last("LIMIT " + limit);
        List<ReadingHistory> readingHistories = readingHistoryService.list(readingWrapper);
        readingHistories.forEach(rh -> articleIds.add(rh.getArticleId()));
        
        return articleIds;
    }

    /**
     * 根据标签查找相似文章
     */
    private List<Article> findSimilarArticlesByTags(List<String> tags, Set<Long> excludeArticleIds, int limit) {
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建标签匹配条件
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
               .eq(Article::getDeleted, 0);
        
        // 添加标签匹配条件（OR关系）
        if (!tags.isEmpty()) {
            wrapper.and(query -> {
                for (int i = 0; i < tags.size(); i++) {
                    if (i == 0) {
                        query.like(Article::getTags, tags.get(i));
                    } else {
                        query.or().like(Article::getTags, tags.get(i));
                    }
                }
            });
        }

        // 排除用户已交互过的文章
        if (!excludeArticleIds.isEmpty()) {
            wrapper.notIn(Article::getId, excludeArticleIds);
        }

        // 按阅读量排序
        wrapper.orderByDesc(Article::getViewCount);

        return articleService.list(wrapper).stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 获取热门文章（按阅读量降序）
     */
    private List<Article> getHotArticles(int limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
               .eq(Article::getDeleted, 0)
               .orderByDesc(Article::getViewCount)
               .last("LIMIT " + limit);
        return articleService.list(wrapper);
    }

    /**
     * 获取用户的交互记录
     */
    private List<Interaction> getUserInteractions(Long userId) {
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interaction::getUserId, userId)
               .in(Interaction::getType, 1, 2); // 1=点赞, 2=收藏
        return interactionService.list(wrapper);
    }

    /**
     * 获取用户交互过的文章ID集合
     */
    private Set<Long> getUserInteractedArticles(Long userId) {
        return getUserInteractions(userId).stream()
            .map(Interaction::getArticleId)
            .collect(Collectors.toSet());
    }

    /**
     * 构建用户-文章交互矩阵
     */
    private Map<Long, Set<Long>> buildUserArticleMatrix() {
        // 获取所有交互记录
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Interaction::getType, 1, 2);
        List<Interaction> allInteractions = interactionService.list(wrapper);

        // 构建映射：userId -> Set<articleId>
        Map<Long, Set<Long>> userArticleMap = new HashMap<>();
        for (Interaction interaction : allInteractions) {
            userArticleMap.computeIfAbsent(interaction.getUserId(), k -> new HashSet<>())
                         .add(interaction.getArticleId());
        }

        return userArticleMap;
    }

    /**
     * 计算两个用户之间的余弦相似度
     * 公式：similarity = |A ∩ B| / sqrt(|A| * |B|)
     */
    private double calculateUserSimilarityWithSets(Set<Long> articles1, Set<Long> articles2) {
        if (articles1.isEmpty() || articles2.isEmpty()) {
            return 0.0;
        }

        // 计算交集
        Set<Long> intersection = new HashSet<>(articles1);
        intersection.retainAll(articles2);

        if (intersection.isEmpty()) {
            return 0.0;
        }

        // 计算余弦相似度
        double intersectionSize = intersection.size();
        double magnitude = Math.sqrt((double) articles1.size() * articles2.size());

        return intersectionSize / magnitude;
    }
}
