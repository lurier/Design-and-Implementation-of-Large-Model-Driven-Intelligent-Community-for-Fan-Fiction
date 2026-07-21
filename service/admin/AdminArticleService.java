package com.fanfaction.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.UserMapper;
import com.fanfaction.vo.AdminArticleVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminArticleService {

    private static final Logger logger = LoggerFactory.getLogger(AdminArticleService.class);

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public Page<AdminArticleVO> getArticleList(int pageNum, int pageSize, Long authorId, Integer status, String keyword) {
        logger.info("管理端文章列表查询 - pageNum: {}, pageSize: {}, authorId: {}, status: {}, keyword: {}", pageNum, pageSize, authorId, status, keyword);
        
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        if (authorId != null) {
            wrapper.eq(Article::getAuthorId, authorId);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getContent, keyword));
        }
        wrapper.orderByDesc(Article::getCreateTime);
        
        Page<Article> articlePage = articleMapper.selectPage(page, wrapper);
        
        Page<AdminArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        
        List<Article> articles = articlePage.getRecords();
        if (articles.isEmpty()) {
            voPage.setRecords(List.of());
            return voPage;
        }
        
        List<Long> authorIds = articles.stream()
                .map(Article::getAuthorId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> reviewerIds = articles.stream()
                .map(Article::getReviewerId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> allUserIds = new ArrayList<>(authorIds);
        allUserIds.addAll(reviewerIds);
        
        Map<Long, User> userMap = userMapper.selectBatchIds(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        List<AdminArticleVO> voList = articles.stream().map(article -> {
            AdminArticleVO vo = new AdminArticleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setContent(article.getContent());
            vo.setSummary(article.getSummary());
            vo.setTags(article.getTags());
            vo.setCoverImage(article.getCoverImage());
            vo.setAuthorId(article.getAuthorId());
            vo.setViewCount(article.getViewCount());
            vo.setLikeCount(article.getLikeCount());
            vo.setFavoriteCount(article.getFavoriteCount());
            vo.setCommentCount(article.getCommentCount());
            vo.setStatus(article.getStatus());
            vo.setReviewComment(article.getReviewComment());
            vo.setReviewerId(article.getReviewerId());
            vo.setCreateTime(article.getCreateTime());
            vo.setUpdateTime(article.getUpdateTime());
            
            User author = userMap.get(article.getAuthorId());
            if (author != null) {
                vo.setAuthorUsername(author.getUsername());
                vo.setAuthorNickname(author.getNickname());
            }
            
            if (article.getReviewerId() != null) {
                User reviewer = userMap.get(article.getReviewerId());
                if (reviewer != null) {
                    vo.setReviewerUsername(reviewer.getUsername());
                }
            }
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        logger.info("管理端文章列表查询成功 - 总记录数: {}, 当前页记录数: {}", voPage.getTotal(), voList.size());
        return voPage;
    }

    public List<AdminArticleVO> getAllArticles(Long authorId, Integer status, String keyword) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        if (authorId != null) {
            wrapper.eq(Article::getAuthorId, authorId);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getContent, keyword));
        }
        wrapper.orderByDesc(Article::getCreateTime);
        
        List<Article> articles = articleMapper.selectList(wrapper);
        if (articles.isEmpty()) {
            return List.of();
        }
        
        List<Long> authorIds = articles.stream()
                .map(Article::getAuthorId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> reviewerIds = articles.stream()
                .map(Article::getReviewerId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        List<Long> allUserIds = new ArrayList<>(authorIds);
        allUserIds.addAll(reviewerIds);
        
        Map<Long, User> userMap = userMapper.selectBatchIds(allUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        return articles.stream().map(article -> {
            AdminArticleVO vo = new AdminArticleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setContent(article.getContent());
            vo.setSummary(article.getSummary());
            vo.setTags(article.getTags());
            vo.setCoverImage(article.getCoverImage());
            vo.setAuthorId(article.getAuthorId());
            vo.setViewCount(article.getViewCount());
            vo.setLikeCount(article.getLikeCount());
            vo.setFavoriteCount(article.getFavoriteCount());
            vo.setCommentCount(article.getCommentCount());
            vo.setStatus(article.getStatus());
            vo.setReviewComment(article.getReviewComment());
            vo.setReviewerId(article.getReviewerId());
            vo.setCreateTime(article.getCreateTime());
            vo.setUpdateTime(article.getUpdateTime());
            
            User author = userMap.get(article.getAuthorId());
            if (author != null) {
                vo.setAuthorUsername(author.getUsername());
                vo.setAuthorNickname(author.getNickname());
            }
            
            if (article.getReviewerId() != null) {
                User reviewer = userMap.get(article.getReviewerId());
                if (reviewer != null) {
                    vo.setReviewerUsername(reviewer.getUsername());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public boolean auditArticle(Long articleId, Integer status, String reviewComment, Long reviewerId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return false;
        }
        article.setStatus(status);
        article.setReviewComment(reviewComment);
        article.setReviewerId(reviewerId);
        return articleMapper.updateById(article) > 0;
    }

    @Transactional
    public boolean deleteArticle(Long articleId) {
        return articleMapper.deleteById(articleId) > 0;
    }

    @Transactional
    public int batchDeleteArticles(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return 0;
        }
        return articleMapper.deleteBatchIds(articleIds);
    }
}
