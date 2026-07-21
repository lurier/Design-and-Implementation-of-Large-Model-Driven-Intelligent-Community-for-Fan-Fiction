package com.fanfaction.controller;

import com.fanfaction.common.Result;
import com.fanfaction.entity.Article;
import com.fanfaction.util.SecurityUtils;
import com.fanfaction.vo.ArticleVO;
import com.fanfaction.service.ArticleService;
import com.fanfaction.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 推荐控制器
 * 提供个性化推荐服务
 */
@Slf4j
@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final ArticleService articleService;
    private final SecurityUtils securityUtils;

    /**
     * 获取个性化推荐文章
     * 支持登录用户的个性化推荐和未登录用户的热门文章推荐
     * 
     * @param limit 推荐数量
     * @return 推荐文章列表
     */
    @GetMapping("/articles")
    public Result<List<ArticleVO>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        
        Long userId = null;
        try {
            userId = securityUtils.getCurrentUserId();
        } catch (Exception e) {
            log.info("用户未登录，返回热门文章");
        }
        
        System.out.println("开始计算推荐，用户ID: " + (userId != null ? userId : "null(未登录)"));
        log.info("为用户 {} 生成推荐，数量：{}", userId != null ? userId : "未登录", limit);
        
        List<Article> recommendedArticles;
        
        try {
            // 如果用户已登录，调用基于内容的推荐算法
            if (userId != null) {
                recommendedArticles = recommendationService.recommendArticlesForUser(userId, limit);
            } else {
                // 未登录用户直接返回热门文章
                System.out.println("用户未登录，直接返回热门文章");
                recommendedArticles = articleService.getHotArticles(limit);
            }
            
            System.out.println("推荐结果数量: " + (recommendedArticles != null ? recommendedArticles.size() : "null"));
            
            // 如果推荐结果为空，返回热门文章作为兜底
            if (recommendedArticles == null || recommendedArticles.isEmpty()) {
                log.info("暂无推荐文章，返回热门文章");
                recommendedArticles = articleService.getHotArticles(limit);
            }
            
            // 转换为 VO
            List<ArticleVO> articleVOs = recommendedArticles.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            log.info("推荐成功，共 {} 篇文章", articleVOs.size());
            return Result.success(articleVOs);
            
        } catch (Exception e) {
            log.error("推荐失败，返回热门文章", e);
            // 出错时返回热门文章
            List<Article> hotArticles = articleService.getHotArticles(limit);
            List<ArticleVO> articleVOs = hotArticles.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            return Result.success(articleVOs);
        }
    }

    /**
     * 将 Article 转换为 ArticleVO
     */
    private ArticleVO convertToVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }
}
