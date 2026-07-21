package com.fanfaction.controller;

import com.fanfaction.annotation.RequireRole;
import com.fanfaction.common.Result;
import com.fanfaction.dto.CreatorStatsDTO;
import com.fanfaction.service.CreatorStatsService;
import com.fanfaction.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 创作者统计控制器
 */
@Tag(name = "创作者统计")
@RestController
@RequestMapping("/api/creator/stats")
@RequiredArgsConstructor
public class CreatorStatsController {

    private final CreatorStatsService creatorStatsService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "获取创作者统计数据")
    @GetMapping
    public Result<CreatorStatsDTO> getCreatorStats(
            @Parameter(description = "日期范围 WEEK/MONTH") 
            @RequestParam(required = false, defaultValue = "WEEK") String dateRange) {
        Long authorId = securityUtils.getCurrentUserId();
        CreatorStatsDTO stats = creatorStatsService.getCreatorStats(authorId, dateRange);
        return Result.success(stats);
    }
}