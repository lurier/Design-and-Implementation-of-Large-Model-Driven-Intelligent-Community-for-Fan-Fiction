package com.fanfaction.controller.admin;

import com.fanfaction.annotation.RequireRole;
import com.fanfaction.common.Result;
import com.fanfaction.service.admin.AdminDashboardService;
import com.fanfaction.vo.DashboardStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端-工作台")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "获取工作台统计数据")
    @GetMapping("/dashboard/stats")
    @RequireRole(2)
    public Result<DashboardStatsVO> getStats() {
        DashboardStatsVO stats = adminDashboardService.getStats();
        return Result.success(stats);
    }
}
