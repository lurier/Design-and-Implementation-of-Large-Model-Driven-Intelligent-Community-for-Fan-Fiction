package com.fanfaction.controller;

import com.fanfaction.annotation.RequireRole;
import com.fanfaction.common.Result;
import com.fanfaction.entity.CreatorApplication;
import com.fanfaction.service.CreatorApplicationService;
import com.fanfaction.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 创作者申请 Controller
 */
@Tag(name = "创作者申请", description = "创作者申请相关接口")
@RestController
@RequestMapping("/api/creator-application")
@RequiredArgsConstructor
public class CreatorApplicationController {
    
    private final CreatorApplicationService creatorApplicationService;
    private final SecurityUtils securityUtils;
    
    @PostMapping
    @Operation(summary = "提交创作者申请")
    public Result<Void> submitApplication(@RequestBody CreatorApplication application) {
        Long userId = securityUtils.getCurrentUserId();
        application.setUserId(userId);
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        
        creatorApplicationService.submitApplication(application);
        return Result.success();
    }
    
    @GetMapping("/my")
    @Operation(summary = "获取我的申请")
    public Result<CreatorApplication> getMyApplication() {
        Long userId = securityUtils.getCurrentUserId();
        CreatorApplication application = creatorApplicationService.getByUserId(userId);
        return Result.success(application);
    }
    
    @PostMapping("/review")
    @RequireRole(2)
    @Operation(summary = "审核申请（管理员接口）")
    public Result<Void> reviewApplication(
            @Parameter(description = "申请 ID") @RequestParam Long id,
            @Parameter(description = "审核状态：0-审核中 1-通过 2-拒绝") @RequestParam Integer status,
            @Parameter(description = "审核意见（拒绝时必填）") @RequestParam(required = false) String comment) {
        
        // 验证参数
        if (status == null || status < 0 || status > 2) {
            return Result.error("审核状态参数错误");
        }
        
        // 如果拒绝，必须有审核意见
        if (status == 2 && (comment == null || comment.trim().isEmpty())) {
            return Result.error("拒绝申请时必须填写审核意见");
        }
        
        Long reviewerId = securityUtils.getCurrentUserId();
        creatorApplicationService.reviewApplication(id, status, comment, reviewerId);
        return Result.success();
    }
    
    @GetMapping("/list")
    @RequireRole(2)
    @Operation(summary = "获取申请列表（管理员接口）")
    public Result<java.util.List<CreatorApplication>> getApplicationList(
            @Parameter(description = "审核状态筛选") @RequestParam(required = false) Integer status) {
        java.util.List<CreatorApplication> list = creatorApplicationService.getApplicationList(status);
        return Result.success(list);
    }
}
