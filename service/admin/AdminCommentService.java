package com.fanfaction.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanfaction.entity.Comment;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.CommentMapper;
import com.fanfaction.mapper.UserMapper;
import com.fanfaction.vo.AdminCommentVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCommentService {

    private static final Logger logger = LoggerFactory.getLogger(AdminCommentService.class);

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public Page<AdminCommentVO> getCommentList(int pageNum, int pageSize, Long articleId, Integer status) {
        logger.info("管理端评论列表查询 - pageNum: {}, pageSize: {}, articleId: {}, status: {}", pageNum, pageSize, articleId, status);
        
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        
        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);
        
        Page<AdminCommentVO> voPage = new Page<>(commentPage.getCurrent(), commentPage.getSize(), commentPage.getTotal());
        
        List<Comment> comments = commentPage.getRecords();
        if (comments.isEmpty()) {
            voPage.setRecords(List.of());
            return voPage;
        }
        
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        List<AdminCommentVO> voList = comments.stream().map(comment -> {
            AdminCommentVO vo = new AdminCommentVO();
            vo.setId(comment.getId());
            vo.setUserId(comment.getUserId());
            vo.setArticleId(comment.getArticleId());
            vo.setParentId(comment.getParentId());
            vo.setReplyUserId(comment.getReplyUserId());
            vo.setContent(comment.getContent());
            vo.setLikeCount(comment.getLikeCount());
            vo.setStatus(comment.getStatus());
            vo.setSentimentScore(comment.getSentimentScore());
            vo.setCreateTime(comment.getCreateTime());
            vo.setUpdateTime(comment.getUpdateTime());
            
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        logger.info("管理端评论列表查询成功 - 总记录数: {}, 当前页记录数: {}", voPage.getTotal(), voList.size());
        return voPage;
    }

    public List<AdminCommentVO> getAllComments(Long articleId, Integer status) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        
        if (articleId != null) {
            wrapper.eq(Comment::getArticleId, articleId);
        }
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        
        List<Comment> comments = commentMapper.selectList(wrapper);
        if (comments.isEmpty()) {
            return List.of();
        }
        
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        return comments.stream().map(comment -> {
            AdminCommentVO vo = new AdminCommentVO();
            vo.setId(comment.getId());
            vo.setUserId(comment.getUserId());
            vo.setArticleId(comment.getArticleId());
            vo.setParentId(comment.getParentId());
            vo.setReplyUserId(comment.getReplyUserId());
            vo.setContent(comment.getContent());
            vo.setLikeCount(comment.getLikeCount());
            vo.setStatus(comment.getStatus());
            vo.setSentimentScore(comment.getSentimentScore());
            vo.setCreateTime(comment.getCreateTime());
            vo.setUpdateTime(comment.getUpdateTime());
            
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setNickname(user.getNickname());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public boolean updateCommentStatus(Long commentId, Integer status) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }
        comment.setStatus(status);
        return commentMapper.updateById(comment) > 0;
    }

    @Transactional
    public int batchUpdateCommentStatus(List<Long> commentIds, Integer status) {
        if (commentIds == null || commentIds.isEmpty()) {
            return 0;
        }
        LambdaUpdateWrapper<Comment> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Comment::getId, commentIds).set(Comment::getStatus, status);
        return commentMapper.update(null, wrapper);
    }

    @Transactional
    public boolean deleteComment(Long commentId) {
        return commentMapper.deleteById(commentId) > 0;
    }
}
