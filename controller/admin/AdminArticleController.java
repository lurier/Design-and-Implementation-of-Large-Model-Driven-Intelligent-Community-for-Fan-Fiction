package com.fanfaction.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanfaction.annotation.RequireRole;
import com.fanfaction.common.Result;
import com.fanfaction.service.admin.AdminArticleService;
import com.fanfaction.util.SecurityUtils;
import com.fanfaction.vo.AdminArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理端-文章管理")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminArticleController {

    private final AdminArticleService adminArticleService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "分页获取文章列表")
    @GetMapping("/articles/list")
    @RequireRole(2)
    public Result<IPage<AdminArticleVO>> getArticleList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "作者ID筛选") @RequestParam(required = false) Long authorId,
            @Parameter(description = "状态筛选 0-待审核 1-已发布 2-驳回 3-已删除") @RequestParam(required = false) Integer status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword) {
        Page<AdminArticleVO> page = adminArticleService.getArticleList(pageNum, pageSize, authorId, status, keyword);
        return Result.success(page);
    }

    @Operation(summary = "获取所有文章列表(不分页)")
    @GetMapping("/articles/all")
    @RequireRole(2)
    public Result<List<AdminArticleVO>> getAllArticles(
            @Parameter(description = "作者ID筛选") @RequestParam(required = false) Long authorId,
            @Parameter(description = "状态筛选 0-待审核 1-已发布 2-驳回 3-已删除") @RequestParam(required = false) Integer status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword) {
        List<AdminArticleVO> articles = adminArticleService.getAllArticles(authorId, status, keyword);
        return Result.success(articles);
    }

    @Operation(summary = "审核文章")
    @PostMapping("/article/audit")
    @RequireRole(2)
    public Result<Void> auditArticle(@RequestBody Map<String, Object> params) {
        Long articleId = ((Number) params.get("articleId")).longValue();
        Integer status = (Integer) params.get("status");
        String reviewComment = (String) params.get("reviewComment");
        Long reviewerId = securityUtils.getCurrentUserId();
        
        boolean success = adminArticleService.auditArticle(articleId, status, reviewComment, reviewerId);
        return success ? Result.success() : Result.error("文章不存在");
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/article/{id}")
    @RequireRole(2)
    public Result<Void> deleteArticle(@PathVariable Long id) {
        boolean success = adminArticleService.deleteArticle(id);
        return success ? Result.success() : Result.error("文章不存在");
    }

    @Operation(summary = "批量删除文章")
    @PostMapping("/articles/batchDelete")
    @RequireRole(2)
    public Result<Integer> batchDeleteArticles(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> articleIds = ((List<?>) params.get("articleIds")).stream()
                .map(id -> ((Number) id).longValue())
                .collect(java.util.stream.Collectors.toList());
        int count = adminArticleService.batchDeleteArticles(articleIds);
        return Result.success(count);
    }
}
