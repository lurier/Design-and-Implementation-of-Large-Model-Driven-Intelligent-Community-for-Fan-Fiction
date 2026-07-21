package com.fanfaction.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanfaction.annotation.RequireRole;
import com.fanfaction.common.Result;
import com.fanfaction.service.admin.AdminAuditService;
import com.fanfaction.util.SecurityUtils;
import com.fanfaction.vo.AuditItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理端-智能审核")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AdminAuditService adminAuditService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "获取待审核列表")
    @GetMapping("/audit/pending-list")
    @RequireRole(2)
    public Result<IPage<AuditItemVO>> getPendingList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "审核状态: PENDING/APPROVED/REJECTED") @RequestParam(required = false) String status,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword) {
        Page<AuditItemVO> page = adminAuditService.getPendingList(pageNum, pageSize, status, keyword);
        return Result.success(page);
    }

    @Operation(summary = "AI预审内容")
    @PostMapping("/audit/ai-pre-audit")
    @RequireRole(2)
    public Result<String> aiPreAudit(@RequestBody Map<String, Object> params) {
        Long itemId = ((Number) params.get("itemId")).longValue();
        String type = (String) params.get("type");
        String content = (String) params.get("content");
        String result = adminAuditService.aiPreAudit(itemId, type, content);
        return Result.success(result);
    }

    @Operation(summary = "执行审核操作")
    @PostMapping("/audit/handle")
    @RequireRole(2)
    public Result<Void> handleAudit(@RequestBody Map<String, Object> params) {
        Long itemId = ((Number) params.get("itemId")).longValue();
        String type = (String) params.get("type");
        String action = (String) params.get("action");
        String reviewComment = (String) params.getOrDefault("reviewComment", "");
        Long reviewerId = securityUtils.getCurrentUserId();

        boolean success = adminAuditService.handleAudit(itemId, type, action, reviewComment, reviewerId);
        return success ? Result.success() : Result.error("审核对象不存在");
    }
}
