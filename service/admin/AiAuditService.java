package com.fanfaction.service.admin;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * AI 内容审核服务 - 调用 DeepSeek 进行内容风控
 */
@Slf4j
@Service
public class AiAuditService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.qwen.api-key}")
    private String apiKey;

    @Value("${ai.qwen.base-url}")
    private String baseUrl;

    @Value("${ai.qwen.model}")
    private String model;

    @Value("${ai.qwen.timeout}")
    private long timeout;

    public AiAuditService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * 对内容进行AI安全审核
     * @return AiAuditResult JSON 字符串
     */
    public String auditContent(String content, String contentType) {
        if (StrUtil.isBlank(content)) {
            return buildDefaultResult("LOW", "内容为空，默认通过");
        }

        try {
            String truncated = content.length() > 2000 ? content.substring(0, 2000) : content;

            String systemPrompt = buildSystemPrompt();
            String userPrompt = "请审核以下" + (contentType.equals("ARTICLE") ? "文章" : "评论") + "内容:\n\n" + truncated;

            log.info("AI内容审核开始 - 类型: {}, 内容长度: {}", contentType, truncated.length());

            long start = System.currentTimeMillis();
            String response = callDeepSeek(systemPrompt, userPrompt);
            long cost = System.currentTimeMillis() - start;

            log.info("AI内容审核完成 - 耗时: {}ms", cost);

            if (StrUtil.isNotBlank(response)) {
                String json = extractJson(response);
                if (isValidResult(json)) {
                    return json;
                }
                log.warn("AI返回格式解析失败，原始响应: {}", response);
            }
        } catch (Exception e) {
            log.error("AI内容审核异常: {}", e.getMessage(), e);
        }
        return buildDefaultResult("MEDIUM", "AI审核异常，转人工处理");
    }

    private String buildSystemPrompt() {
        return "你是一个资深的内容安全审核专家。请严格审核用户提交的内容，判断是否违规。\n" +
               "\n" +
               "审核标准：\n" +
               "1. 政治敏感内容：涉及敏感政治话题、煽动性言论\n" +
               "2. 色情低俗内容：露骨色情描写、低俗挑逗\n" +
               "3. 暴力恐怖内容：宣扬暴力、恐怖主义\n" +
               "4. 违法信息：赌博、诈骗、毒品等\n" +
               "5. 人身攻击：辱骂、诽谤、网络暴力\n" +
               "6. 广告营销：纯广告、垃圾营销内容\n" +
               "\n" +
               "请仅输出以下JSON格式，不要包含任何其他文字：\n" +
               "{\n" +
               "  \"violation\": true/false,\n" +
               "  \"riskLevel\": \"LOW\"或\"MEDIUM\"或\"HIGH\",\n" +
               "  \"reason\": \"审核理由（50字以内）\",\n" +
               "  \"violations\": [{\"type\":\"违规类型\",\"description\":\"具体描述\"}],\n" +
               "  \"sentiment\": \"正面\"或\"中性\"或\"负面\",\n" +
               "  \"sentimentScore\": 0-100\n" +
               "}";
    }

    private String callDeepSeek(String systemPrompt, String userPrompt) {
        WebClient webClient = webClientBuilder.build();

        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);

        JSONArray messages = new JSONArray();
        JSONObject sysMsg = new JSONObject();
        sysMsg.set("role", "system");
        sysMsg.set("content", systemPrompt);
        messages.add(sysMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", userPrompt);
        messages.add(userMsg);

        requestBody.set("messages", messages);
        requestBody.set("temperature", 0.3);
        requestBody.set("max_tokens", 600);

        try {
            String responseBody = webClient.post()
                    .uri(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorResume(error -> {
                        log.error("DeepSeek API调用失败: {}", error.getMessage());
                        return Mono.just("");
                    })
                    .block();

            if (StrUtil.isNotBlank(responseBody)) {
                return extractContentFromResponse(responseBody);
            }
        } catch (Exception e) {
            log.error("DeepSeek API异常: {}", e.getMessage());
        }
        return "";
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            JSONObject json = JSONUtil.parseObj(responseBody);
            if (json.containsKey("choices")) {
                JSONArray choices = json.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject first = choices.getJSONObject(0);
                    if (first.containsKey("message")) {
                        return first.getJSONObject("message").getStr("content");
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());
        }
        return "";
    }

    private String extractJson(String text) {
        String trimmed = text.trim();
        // 去掉可能的 markdown 代码块标记
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("{");
            int end = trimmed.lastIndexOf("}");
            if (start >= 0 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }
        return trimmed;
    }

    private boolean isValidResult(String json) {
        try {
            JSONObject obj = JSONUtil.parseObj(json);
            return obj.containsKey("violation") && obj.containsKey("riskLevel");
        } catch (Exception e) {
            return false;
        }
    }

    private String buildDefaultResult(String riskLevel, String reason) {
        JSONObject result = new JSONObject();
        result.set("violation", "HIGH".equals(riskLevel));
        result.set("riskLevel", riskLevel);
        result.set("reason", reason);
        result.set("violations", new JSONArray());
        result.set("sentiment", "中性");
        result.set("sentimentScore", 50);
        return result.toString();
    }
}
