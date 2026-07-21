package com.fanfaction.controller;

import com.fanfaction.common.Result;
import com.fanfaction.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 辅助创作 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 智能摘要生成
     */
    @PostMapping("/summary")
    public Result<String> generateSummary(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        log.info("AI摘要生成请求，内容长度: {}", content != null ? content.length() : 0);

        if (content == null || content.trim().isEmpty()) {
            return Result.error("正文内容不能为空");
        }

        String summary = aiService.generateSummary(content);
        return Result.success(summary);
    }

    /**
     * 智能标签推荐
     */
    @PostMapping("/tags")
    public Result<List<String>> generateTags(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        log.info("AI标签推荐请求，内容长度: {}", content != null ? content.length() : 0);

        if (content == null || content.trim().isEmpty()) {
            return Result.error("正文内容不能为空");
        }

        List<String> tags = aiService.generateTags(content);
        return Result.success(tags);
    }

    /**
     * 评论情绪检测
     * 返回格式: { "is_negative": false, "emotion_type": "正常" }
     */
    @PostMapping("/emotion")
    public Result<Map<String, Object>> detectEmotion(@RequestBody Map<String, String> body) {
        String content = body.get("content");
        log.info("AI情绪检测请求，内容长度: {}", content != null ? content.length() : 0);

        if (content == null || content.trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }

        try {
            Map<String, Object> emotionResult = aiService.detectEmotion(content);
            return Result.success(emotionResult);
        } catch (Exception e) {
            log.error("AI情绪检测接口异常: {}", e.getMessage(), e);
            // 异常时返回默认放行结果，不阻塞评论
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("is_negative", false);
            fallback.put("emotion_type", "正常");
            return Result.success(fallback);
        }
    }
}
