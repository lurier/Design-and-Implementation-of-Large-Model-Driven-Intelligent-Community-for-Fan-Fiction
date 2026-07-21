package com.fanfaction.controller;

import com.fanfaction.common.Result;
import com.fanfaction.service.ReaderStatsService;
import com.fanfaction.util.SecurityUtils;
import com.fanfaction.vo.ReaderStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 读者统计 Controller
 */
@Tag(name = "读者统计", description = "读者数据统计相关接口")
@RestController
@RequestMapping("/api/reader-stats")
@RequiredArgsConstructor
public class ReaderStatsController {
    
    private final ReaderStatsService readerStatsService;
    private final SecurityUtils securityUtils;
    
    @GetMapping
    @Operation(summary = "获取用户阅读数据统计")
    public Result<ReaderStatsVO> getUserStats() {
        Long userId = securityUtils.getCurrentUserId();
        ReaderStatsVO stats = readerStatsService.getUserStats(userId);
        return Result.success(stats);
    }
}
