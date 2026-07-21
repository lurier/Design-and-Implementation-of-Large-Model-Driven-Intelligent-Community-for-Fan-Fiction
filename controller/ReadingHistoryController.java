package com.fanfaction.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fanfaction.common.Result;
import com.fanfaction.service.ReadingHistoryService;
import com.fanfaction.util.SecurityUtils;
import com.fanfaction.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "阅读历史管理")
@RestController
@RequestMapping("/api/reading-history")
@RequiredArgsConstructor
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "保存或更新阅读历史")
    @PostMapping
    public Result<Void> saveHistory(@RequestBody Map<String, Object> params) {
        Long userId = securityUtils.getCurrentUserId();
        Long articleId = Long.valueOf(params.get("articleId").toString());
        Integer readDuration = params.get("readDuration") != null ? 
                Integer.valueOf(params.get("readDuration").toString()) : null;
        Integer scrollPosition = params.get("scrollPosition") != null ? 
                Integer.valueOf(params.get("scrollPosition").toString()) : null;
        Double readPercentage = params.get("readPercentage") != null ? 
                Double.valueOf(params.get("readPercentage").toString()) : null;
        
        readingHistoryService.saveOrUpdateHistory(userId, articleId, readDuration, 
                scrollPosition, readPercentage);
        return Result.success();
    }

    @Operation(summary = "分页查询阅读历史")
    @GetMapping
    public Result<IPage<ArticleVO>> getHistoryPage(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = securityUtils.getCurrentUserId();
        IPage<ArticleVO> page = readingHistoryService.getHistoryPage(userId, pageNum, pageSize);
        return Result.success(page);
    }
}
