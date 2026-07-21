package com.fanfaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fanfaction.annotation.RequireRole;
import com.fanfaction.common.Result;
import com.fanfaction.dto.ArticleDTO;
import com.fanfaction.entity.Article;
import com.fanfaction.service.ArticleService;
import com.fanfaction.util.SecurityUtils;
import com.fanfaction.vo.ArticleDetailVO;
import com.fanfaction.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "文章管理")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "发布文章")
    @PostMapping
    @RequireRole(1)
    @com.fanfaction.annotation.Idempotent(expireTime = 300, message = "文章正在发布中，请勿重复提交")
    public Result<Long> publishArticle(@Valid @RequestBody ArticleDTO articleDTO) {
        Long authorId = securityUtils.getCurrentUserId();
        Long articleId = articleService.publishArticle(authorId, articleDTO);
        return Result.success(articleId);
    }

    @Operation(summary = "分页查询文章列表")
    @GetMapping
    public Result<IPage<ArticleVO>> getArticlePage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "排序方式 time/hot") @RequestParam(defaultValue = "time") String sortBy,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "标签筛选") @RequestParam(required = false) String tag) {
        IPage<ArticleVO> page = articleService.getArticlePage(pageNum, pageSize, sortBy, keyword, tag);
        return Result.success(page);
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> getArticleDetail(@PathVariable Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        ArticleDetailVO detail = articleService.getArticleDetail(id, currentUserId);
        return Result.success(detail);
    }

    @Operation(summary = "编辑文章")
    @PutMapping("/{id}")
    @RequireRole(1)
    public Result<Void> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleDTO articleDTO) {
        Long authorId = securityUtils.getCurrentUserId();
        articleService.updateArticle(id, authorId, articleDTO);
        return Result.success();
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    @RequireRole(1)
    public Result<Void> deleteArticle(@PathVariable Long id) {
        Long authorId = securityUtils.getCurrentUserId();
        articleService.deleteArticle(id, authorId);
        return Result.success();
    }

    @Operation(summary = "分页获取我的作品列表")
    @GetMapping("/user/published")
    public Result<IPage<Article>> getUserArticles(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status) {
        Long userId = securityUtils.getCurrentUserId();
        IPage<Article> pageResult = articleService.getUserArticles(userId, page, size, status);
        return Result.success(pageResult);
    }

    @Operation(summary = "审核文章（管理员）")
    @PostMapping("/{id}/review")
    @RequireRole(2)
    public Result<Void> reviewArticle(
            @PathVariable Long id,
            @Parameter(description = "审核状态: DRAFT/PENDING/APPROVED/REJECTED") @RequestParam String status,
            @Parameter(description = "审核意见") @RequestParam(required = false) String comment) {
        Long reviewerId = securityUtils.getCurrentUserId();
        articleService.reviewArticle(id, status, comment, reviewerId);
        return Result.success();
    }

    @Operation(summary = "获取待审核文章列表（管理员）")
    @GetMapping("/review/pending")
    @RequireRole(2)
    public Result<IPage<Article>> getPendingArticles(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        IPage<Article> pageResult = articleService.getPendingArticles(page, size);
        return Result.success(pageResult);
    }

    @Operation(summary = "获取所有文章列表（管理员，支持状态筛选）")
    @GetMapping("/admin/list")
    @RequireRole(2)
    public Result<IPage<Article>> getAllArticlesForAdmin(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "状态筛选: DRAFT/PENDING/APPROVED/REJECTED") @RequestParam(required = false) String status) {
        IPage<Article> pageResult = articleService.getAllArticles(page, size, status);
        return Result.success(pageResult);
    }
}