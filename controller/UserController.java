package com.fanfaction.controller;

import com.fanfaction.annotation.RequireRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fanfaction.common.Result;
import com.fanfaction.dto.RoleUpdateDTO;
import com.fanfaction.dto.UserProfileDTO;
import com.fanfaction.service.UserService;
import com.fanfaction.util.SecurityUtils;
import com.fanfaction.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        String username = securityUtils.getCurrentUsername();
        UserVO userVO = userService.getCurrentUserInfo(username);
        return Result.success(userVO);
    }

    @Operation(summary = "管理员查询用户列表")
    @GetMapping
    @RequireRole(2)
    public Result<IPage<UserVO>> getUserPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        IPage<UserVO> page = userService.getUserPage(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @Operation(summary = "管理员修改用户角色")
    @PutMapping("/role")
    @RequireRole(2)
    public Result<Void> updateUserRole(@Valid @RequestBody RoleUpdateDTO roleUpdateDTO) {
        userService.updateUserRole(roleUpdateDTO);
        return Result.success();
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public Result<Void> updateUserProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        Long userId = securityUtils.getCurrentUserId();
        userService.updateUserProfile(userId, profileDTO);
        return Result.success();
    }
}
