package com.fanfaction.controller;

import com.fanfaction.common.Result;
import com.fanfaction.dto.CommentDTO;
import com.fanfaction.dto.InteractionDTO;
import com.fanfaction.entity.Article;
import com.fanfaction.service.ArticleService;
import com.fanfaction.service.CommentService;
import com.fanfaction.service.InteractionService;
import com.fanfaction.util.SecurityUtils;
import com.fanfaction.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "互动管理")
@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;
    private final CommentService commentService;
    private final ArticleService articleService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/like")
    public Result<Void> toggleLike(@Valid @RequestBody InteractionDTO interactionDTO) {
        Long userId = securityUtils.getCurrentUserId();
        interactionService.toggleLike(userId, interactionDTO.getArticleId());
        return Result.success();
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/favorite")
    public Result<Void> toggleFavorite(@Valid @RequestBody InteractionDTO interactionDTO) {
        Long userId = securityUtils.getCurrentUserId();
        interactionService.toggleFavorite(userId, interactionDTO.getArticleId());
        return Result.success();
    }

    @Operation(summary = "发表评论")
    @PostMapping("/comment")
    @com.fanfaction.annotation.Idempotent(expireTime = 120, message = "评论提交过于频繁，请稍后再试")
    public Result<Void> publishComment(@Valid @RequestBody CommentDTO commentDTO) {
        Long userId = securityUtils.getCurrentUserId();
        commentService.publishComment(userId, commentDTO);
        return Result.success();
    }

    @Operation(summary = "获取文章评论列表")
    @GetMapping("/comments/{articleId}")
    public Result<List<CommentVO>> getCommentList(@PathVariable Long articleId) {
        List<CommentVO> comments = commentService.getCommentList(articleId);
        return Result.success(comments);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        commentService.deleteComment(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取用户收藏的文章列表")
    @GetMapping("/user/favorites")
    public Result<List<Article>> getUserFavorites() {
        Long userId = securityUtils.getCurrentUserId();
        List<Article> articles = articleService.getUserFavoriteArticles(userId);
        return Result.success(articles);
    }

    @Operation(summary = "获取用户点赞的文章列表")
    @GetMapping("/user/likes")
    public Result<List<Article>> getUserLikedArticles() {
        Long userId = securityUtils.getCurrentUserId();
        List<Article> articles = articleService.getUserLikedArticles(userId);
        return Result.success(articles);
    }
}
