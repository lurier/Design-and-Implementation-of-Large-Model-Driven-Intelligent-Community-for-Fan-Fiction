package com.fanfaction.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanfaction.annotation.RequireRole;
import com.fanfaction.common.Result;
import com.fanfaction.service.admin.AdminCommentService;
import com.fanfaction.vo.AdminCommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理端-评论管理")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @Operation(summary = "分页获取评论列表")
    @GetMapping("/comments/list")
    @RequireRole(2)
    public Result<IPage<AdminCommentVO>> getCommentList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "文章ID筛选") @RequestParam(required = false) Long articleId,
            @Parameter(description = "状态筛选 0-待审核 1-通过 2-驳回 3-已删除") @RequestParam(required = false) Integer status) {
        Page<AdminCommentVO> page = adminCommentService.getCommentList(pageNum, pageSize, articleId, status);
        return Result.success(page);
    }

    @Operation(summary = "获取所有评论列表(不分页)")
    @GetMapping("/comments/all")
    @RequireRole(2)
    public Result<List<AdminCommentVO>> getAllComments(
            @Parameter(description = "文章ID筛选") @RequestParam(required = false) Long articleId,
            @Parameter(description = "状态筛选 0-待审核 1-通过 2-驳回 3-已删除") @RequestParam(required = false) Integer status) {
        List<AdminCommentVO> comments = adminCommentService.getAllComments(articleId, status);
        return Result.success(comments);
    }

    @Operation(summary = "修改评论状态")
    @PostMapping("/comment/updateStatus")
    @RequireRole(2)
    public Result<Void> updateCommentStatus(@RequestBody Map<String, Object> params) {
        Long commentId = ((Number) params.get("commentId")).longValue();
        Integer status = (Integer) params.get("status");
        boolean success = adminCommentService.updateCommentStatus(commentId, status);
        return success ? Result.success() : Result.error("评论不存在");
    }

    @Operation(summary = "批量修改评论状态")
    @PostMapping("/comment/batchUpdateStatus")
    @RequireRole(2)
    public Result<Integer> batchUpdateCommentStatus(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> commentIds = ((List<?>) params.get("commentIds")).stream()
                .map(id -> ((Number) id).longValue())
                .collect(java.util.stream.Collectors.toList());
        Integer status = (Integer) params.get("status");
        int count = adminCommentService.batchUpdateCommentStatus(commentIds, status);
        return Result.success(count);
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comment/{id}")
    @RequireRole(2)
    public Result<Void> deleteComment(@PathVariable Long id) {
        boolean success = adminCommentService.deleteComment(id);
        return success ? Result.success() : Result.error("评论不存在");
    }
}
