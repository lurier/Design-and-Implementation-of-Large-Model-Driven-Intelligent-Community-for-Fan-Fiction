package com.fanfaction.controller;

import com.fanfaction.common.Result;
import com.fanfaction.service.ChatService;
import com.fanfaction.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能客服控制器
 */
@Tag(name = "智能客服")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SecurityUtils securityUtils;

    @Operation(summary = "获取客服响应")
    @PostMapping("/ask")
    public Result<Map<String, String>> askQuestion(
            @Parameter(description = "用户问题") @RequestBody Map<String, String> request) {
        
        String question = request.get("question");
        if (question == null || question.trim().isEmpty()) {
            return Result.error(400, "请输入您的问题");
        }

        Long userId = securityUtils.getCurrentUserId();
        String response = chatService.getChatResponse(userId, question);
        
        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        result.put("question", question);
        
        return Result.success(result);
    }

    @Operation(summary = "搜索相关文章")
    @GetMapping("/search")
    public Result<java.util.List<ChatService.ArticleSearchResult>> searchArticles(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "5") int limit) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error(400, "请输入搜索关键词");
        }

        java.util.List<ChatService.ArticleSearchResult> results = chatService.searchArticles(keyword, limit);
        return Result.success(results);
    }

    @Operation(summary = "获取客服欢迎语")
    @GetMapping("/welcome")
    public Result<Map<String, String>> getWelcomeMessage() {
        Map<String, String> result = new HashMap<>();
        result.put("message", "您好！我是Fan Faction的智能客服，请问有什么可以帮助您的？");
        result.put("tips", "您可以问我：如何发布文章、如何收藏文章、如何申请成为创作者等问题");
        return Result.success(result);
    }
}