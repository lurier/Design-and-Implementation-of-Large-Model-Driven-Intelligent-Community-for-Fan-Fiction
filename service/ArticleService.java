package com.fanfaction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fanfaction.dto.ArticleDTO;
import com.fanfaction.entity.Article;
import com.fanfaction.vo.ArticleDetailVO;
import com.fanfaction.vo.ArticleVO;

import java.util.List;

public interface ArticleService extends IService<Article> {
    Long publishArticle(Long authorId, ArticleDTO articleDTO);
    IPage<ArticleVO> getArticlePage(int pageNum, int pageSize, String sortBy, String keyword, String tag);
    ArticleDetailVO getArticleDetail(Long articleId, Long currentUserId);
    void updateArticle(Long articleId, Long authorId, ArticleDTO articleDTO);
    void deleteArticle(Long articleId, Long authorId);
    List<Article> getUserFavoriteArticles(Long userId);
    List<Article> getUserLikedArticles(Long userId);
    List<Article> getUserPublishedArticles(Long userId);
    
    /**
     * 分页获取用户文章列表（支持状态筛选）
     * @param userId 用户 ID
     * @param page 页码
     * @param size 每页数量
     * @param status 状态筛选
     * @return 分页结果
     */
    IPage<Article> getUserArticles(Long userId, int page, int size, String status);
    
    /**
     * 审核文章
     * @param articleId 文章 ID
     * @param status 状态
     * @param comment 审核意见
     * @param reviewerId 审核人 ID
     */
    void reviewArticle(Long articleId, String status, String comment, Long reviewerId);
    
    /**
     * 获取待审核文章列表
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<Article> getPendingArticles(int page, int size);
    
    /**
     * 获取所有文章列表（管理员，支持状态筛选）
     * @param page 页码
     * @param size 每页数量
     * @param status 状态筛选
     * @return 分页结果
     */
    IPage<Article> getAllArticles(int page, int size, String status);
    
    /**
     * 获取热门文章
     * @param limit 数量限制
     * @return 热门文章列表
     */
    List<Article> getHotArticles(int limit);
    
    /**
     * 异步生成文章摘要和标签
     * @param articleId 文章 ID
     */
    void asyncGenerateAiContent(Long articleId);
}
