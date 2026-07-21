package com.fanfaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fanfaction.dto.CommentDTO;
import com.fanfaction.entity.Comment;
import com.fanfaction.vo.CommentVO;

import java.util.List;

public interface CommentService extends IService<Comment> {
    void publishComment(Long userId, CommentDTO commentDTO);
    List<CommentVO> getCommentList(Long articleId);
    void deleteComment(Long commentId, Long userId);
}
