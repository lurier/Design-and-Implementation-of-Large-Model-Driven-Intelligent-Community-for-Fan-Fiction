package com.fanfaction.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanfaction.annotation.RequireRole;
import com.fanfaction.common.Result;
import com.fanfaction.service.admin.AdminUserService;
import com.fanfaction.vo.AdminUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理端-用户管理")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "分页获取用户列表")
    @GetMapping("/users/list")
    @RequireRole(2)
    public Result<IPage<AdminUserVO>> getUserList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "角色筛选: READER/CREATOR/ADMIN") @RequestParam(required = false) String role,
            @Parameter(description = "状态筛选: ACTIVE/BLOCKED") @RequestParam(required = false) String status,
            @Parameter(description = "关键词搜索(昵称/用户名)") @RequestParam(required = false) String keyword) {
        Page<AdminUserVO> page = adminUserService.getUserList(pageNum, pageSize, role, status, keyword);
        return Result.success(page);
    }

    @Operation(summary = "修改用户状态(封禁/解封)")
    @PostMapping("/user/updateStatus")
    @RequireRole(2)
    public Result<Void> updateUserStatus(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("userId")).longValue();
        String status = (String) params.get("status");
        boolean success = adminUserService.updateUserStatus(userId, status);
        return success ? Result.success() : Result.error("用户不存在");
    }

    @Operation(summary = "修改用户角色")
    @PostMapping("/user/updateRole")
    @RequireRole(2)
    public Result<Void> updateUserRole(@RequestBody Map<String, Object> params) {
        Long userId = ((Number) params.get("userId")).longValue();
        String role = (String) params.get("role");
        boolean success = adminUserService.updateUserRole(userId, role);
        return success ? Result.success() : Result.error("用户不存在");
    }
}
