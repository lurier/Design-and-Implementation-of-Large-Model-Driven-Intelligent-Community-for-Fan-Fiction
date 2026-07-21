package com.fanfaction.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfaction.dto.CommentDTO;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Comment;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.CommentMapper;
import com.fanfaction.service.CommentService;
import com.fanfaction.service.SentimentAnalysisService;
import com.fanfaction.service.UserService;
import com.fanfaction.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final UserService userService;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public void publishComment(Long userId, CommentDTO commentDTO) {
        // 检查用户状态，封禁用户禁止评论
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("您的账号已被封禁，无法进行操作");
        }

        Comment comment = new Comment();
        BeanUtil.copyProperties(commentDTO, comment);
        comment.setUserId(userId);
        comment.setParentId(commentDTO.getParentId() != null ? commentDTO.getParentId() : 0L);
        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setDeleted(0);
        
        // 情感分析：对评论内容进行情感倾向分析
        if (commentDTO.getContent() != null && !commentDTO.getContent().trim().isEmpty()) {
            try {
                double sentimentScore = sentimentAnalysisService.analyzeSentiment(commentDTO.getContent());
                comment.setSentimentScore(sentimentScore);
                log.info("评论情感分析完成，分值: {}, 类型: {}", sentimentScore, 
                        sentimentAnalysisService.getSentimentType(sentimentScore));
            } catch (Exception e) {
                log.error("评论情感分析失败，使用默认值0.0", e);
                comment.setSentimentScore(0.0);
            }
        } else {
            comment.setSentimentScore(0.0);
        }
        
        save(comment);
        
        // 更新文章的评论数
        Article article = articleMapper.selectById(commentDTO.getArticleId());
        if (article != null) {
            article.setCommentCount(article.getCommentCount() + 1);
            articleMapper.updateById(article);
            log.info("文章评论数更新成功，文章ID: {}, 评论数: {}", article.getId(), article.getCommentCount());
        }
    }

    @Override
    public List<CommentVO> getCommentList(Long articleId) {
        // 查询该文章下的所有评论（包括主评论和所有层级的子评论）
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getDeleted, 0)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime); // 按创建时间升序，确保父评论在子评论前面
        
        List<Comment> comments = list(wrapper);
        
        // 转换为VO并返回扁平列表，由前端构建树形结构
        return comments.stream()
                .map(this::convertToCommentVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = getById(commentId);
        if (comment == null || comment.getDeleted() == 1) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }
        
        // 保存文章ID用于后续更新评论数
        Long articleId = comment.getArticleId();
        
        comment.setDeleted(1);
        updateById(comment);
        
        // 更新文章的评论数
        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            article.setCommentCount(Math.max(0, article.getCommentCount() - 1));
            articleMapper.updateById(article);
            log.info("文章评论数更新成功，文章ID: {}, 评论数: {}", article.getId(), article.getCommentCount());
        }
    }

    private CommentVO convertToCommentVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtil.copyProperties(comment, vo);
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            vo.setUserName(user.getUsername());
            vo.setUserNickname(user.getNickname());
            vo.setUserAvatar(user.getAvatar());
        }
        if (comment.getReplyUserId() != null) {
            User replyUser = userService.getById(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyUserName(replyUser.getUsername());
            }
        }
        return vo;
    }
    
    /**
     * 获取某条评论的所有回复（一级回复）
     */
    private List<CommentVO> getRepliesByParentId(Long parentId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, parentId)
                .eq(Comment::getDeleted, 0)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreateTime);
        
        List<Comment> replies = list(wrapper);
        return replies.stream()
                .map(this::convertToCommentVO)
                .collect(Collectors.toList());
    }
}
