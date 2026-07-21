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
 * AI服务测试控制器
 * 用于快速验证大模型API集成功能
 * 
 * 访问方式：http://localhost:8080/api/test/ai/generate?content=测试文章内容
 */
@Slf4j
@RestController
@RequestMapping("/api/test/ai")
@RequiredArgsConstructor
public class AiTestController {

    private final AiService aiService;

    /**
     * 测试AI摘要和标签生成功能
     * 
     * @param content 文章内容
     * @return 包含摘要和标签的结果
     */
    @GetMapping("/generate")
    public Result<Map<String, Object>> generateAiContent(@RequestParam String content) {
        log.info("========== AI测试接口被调用 ==========");
        log.info("测试内容长度: {} 字符", content.length());
        log.info("测试内容: {}", content);
        
        try {
            // 生成摘要
            log.info("开始调用AI生成摘要...");
            long summaryStartTime = System.currentTimeMillis();
            String summary = aiService.generateSummary(content);
            long summaryEndTime = System.currentTimeMillis();
            log.info("✅ 摘要生成成功，耗时: {} ms", (summaryEndTime - summaryStartTime));
            
            // 生成标签
            log.info("开始调用AI生成标签...");
            long tagsStartTime = System.currentTimeMillis();
            List<String> tags = aiService.generateTags(content);
            long tagsEndTime = System.currentTimeMillis();
            log.info("✅ 标签生成成功，标签数: {}, 耗时: {} ms", tags.size(), (tagsEndTime - tagsStartTime));
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("contentLength", content.length());
            result.put("summary", summary);
            result.put("tags", tags);
            result.put("tagCount", tags.size());
            result.put("summaryGenerationTime", (summaryEndTime - summaryStartTime) + " ms");
            result.put("tagsGenerationTime", (tagsEndTime - tagsStartTime) + " ms");
            result.put("totalTime", ((summaryEndTime - summaryStartTime) + (tagsEndTime - tagsStartTime)) + " ms");
            
            log.info("========== AI测试完成 ==========");
            log.info("返回结果: {}", result);
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("❌ AI测试失败", e);
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("errorMessage", e.getMessage());
            errorResult.put("errorType", e.getClass().getSimpleName());
            
            return Result.error(500, "AI测试失败: " + e.getMessage());
        }
    }

    /**
     * 仅测试摘要生成
     * 
     * @param content 文章内容
     * @return 摘要文本
     */
    @GetMapping("/summary")
    public Result<String> testSummary(@RequestParam String content) {
        log.info("========== 测试摘要生成 ==========");
        log.info("内容长度: {} 字符", content.length());
        
        try {
            long startTime = System.currentTimeMillis();
            String summary = aiService.generateSummary(content);
            long endTime = System.currentTimeMillis();
            
            log.info("✅ 摘要生成成功");
            log.info("摘要内容: {}", summary);
            log.info("耗时: {} ms", (endTime - startTime));
            
            return Result.success(summary);
        } catch (Exception e) {
            log.error("❌ 摘要生成失败", e);
            return Result.error(500, "摘要生成失败: " + e.getMessage());
        }
    }

    /**
     * 仅测试标签生成
     * 
     * @param content 文章内容
     * @return 标签列表
     */
    @GetMapping("/tags")
    public Result<List<String>> testTags(@RequestParam String content) {
        log.info("========== 测试标签生成 ==========");
        log.info("内容长度: {} 字符", content.length());
        
        try {
            long startTime = System.currentTimeMillis();
            List<String> tags = aiService.generateTags(content);
            long endTime = System.currentTimeMillis();
            
            log.info("✅ 标签生成成功");
            log.info("标签内容: {}", String.join(", ", tags));
            log.info("耗时: {} ms", (endTime - startTime));
            
            return Result.success(tags);
        } catch (Exception e) {
            log.error("❌ 标签生成失败", e);
            return Result.error(500, "标签生成失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "AI Test Controller");
        status.put("status", "running");
        status.put("message", "AI测试服务正常运行");
        
        return Result.success(status);
    }
}
