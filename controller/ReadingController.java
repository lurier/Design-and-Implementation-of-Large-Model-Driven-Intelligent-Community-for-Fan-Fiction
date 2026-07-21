package com.fanfaction.controller;

import com.fanfaction.common.Result;
import com.fanfaction.entity.Bookmark;
import com.fanfaction.entity.ReadingProgress;
import com.fanfaction.service.ReadingService;
import com.fanfaction.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "阅读功能管理")
@RestController
@RequestMapping("/api/reading")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "保存阅读进度")
    @PostMapping("/progress")
    public Result<Void> saveReadingProgress(@RequestBody Map<String, Object> params) {
        Long userId = securityUtils.getCurrentUserId();
        Long articleId = Long.valueOf(params.get("articleId").toString());
        Integer scrollPosition = params.get("scrollPosition") != null 
                ? Integer.valueOf(params.get("scrollPosition").toString()) : 0;
        Double readPercentage = params.get("readPercentage") != null 
                ? Double.valueOf(params.get("readPercentage").toString()) : 0.0;
        
        readingService.saveReadingProgress(userId, articleId, scrollPosition, readPercentage);
        return Result.success();
    }

    @Operation(summary = "获取阅读进度")
    @GetMapping("/progress/{articleId}")
    public Result<ReadingProgress> getReadingProgress(@PathVariable Long articleId) {
        Long userId = securityUtils.getCurrentUserId();
        ReadingProgress progress = readingService.getReadingProgress(userId, articleId);
        return Result.success(progress);
    }

    @Operation(summary = "添加书签")
    @PostMapping("/bookmark")
    public Result<Bookmark> addBookmark(@RequestBody Map<String, Object> params) {
        Long userId = securityUtils.getCurrentUserId();
        Long articleId = Long.valueOf(params.get("articleId").toString());
        Integer position = (Integer) params.get("position");
        String note = (String) params.get("note");
        
        Bookmark bookmark = readingService.addBookmark(userId, articleId, position, note);
        return Result.success(bookmark);
    }

    @Operation(summary = "删除书签")
    @DeleteMapping("/bookmark/{id}")
    public Result<Void> deleteBookmark(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        readingService.deleteBookmark(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取文章的所有书签")
    @GetMapping("/bookmarks/article/{articleId}")
    public Result<List<Bookmark>> getBookmarksByArticle(@PathVariable Long articleId) {
        Long userId = securityUtils.getCurrentUserId();
        List<Bookmark> bookmarks = readingService.getBookmarksByArticle(userId, articleId);
        return Result.success(bookmarks);
    }

    @Operation(summary = "获取用户的所有书签")
    @GetMapping("/bookmarks")
    public Result<List<Bookmark>> getUserBookmarks() {
        Long userId = securityUtils.getCurrentUserId();
        List<Bookmark> bookmarks = readingService.getUserBookmarks(userId);
        return Result.success(bookmarks);
    }
}
