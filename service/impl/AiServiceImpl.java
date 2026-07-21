package com.fanfaction.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fanfaction.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.qwen.api-key}")
    private String apiKey;

    @Value("${ai.qwen.base-url}")
    private String baseUrl;

    @Value("${ai.qwen.model}")
    private String model;

    @Value("${ai.qwen.timeout}")
    private long timeout;

    private static final int MAX_CONTENT_LENGTH = 3000; // 限制发送给AI的内容长度

    @Override
    public String generateSummary(String content) {
        if (StrUtil.isBlank(content)) {
            log.warn("生成摘要失败：内容为空");
            return "";
        }

        try {
            // 截取内容前部分，避免过长
            String truncatedContent = content.length() > MAX_CONTENT_LENGTH 
                    ? content.substring(0, MAX_CONTENT_LENGTH) 
                    : content;

            String prompt = "请用不超过100字精炼概括以下文章的核心内容，只输出摘要，不要有任何多余文字：\n\n" + truncatedContent;

            // 记录请求信息
            log.info("========== AI摘要生成开始 ==========");
            log.info("原始内容长度: {} 字符", content.length());
            log.info("截断后内容长度: {} 字符", truncatedContent.length());
            log.info("Prompt长度: {} 字符", prompt.length());
            log.debug("Prompt内容: {}", prompt);

            long startTime = System.currentTimeMillis();
            String response = callAiApi(prompt);
            long endTime = System.currentTimeMillis();

            log.info("API调用耗时: {} ms", (endTime - startTime));
            
            if (StrUtil.isNotBlank(response)) {
                // 提取摘要内容，去除可能的引号或多余空格
                String summary = response.trim();
                // 如果摘要超过100字，强制截断
                if (summary.length() > 100) {
                    summary = summary.substring(0, 100);
                }
                log.info("AI摘要生成成功，摘要长度: {} 字符", summary.length());
                log.info("摘要内容: {}", summary);
                log.info("========== AI摘要生成完成 ==========");
                return summary;
            } else {
                log.warn("AI返回结果为空");
            }
        } catch (Exception e) {
            log.error("调用AI生成摘要失败，错误信息: {}", e.getMessage(), e);
        }

        return "";
    }

    @Override
    public Map<String, Object> detectEmotion(String content) {
        Map<String, Object> result = new HashMap<>();
        result.put("is_negative", false);
        result.put("emotion_type", "正常");

        if (StrUtil.isBlank(content)) {
            return result;
        }

        try {
            String prompt = "请分析以下评论内容的情绪，严格按JSON格式返回，不要有任何其他文字：\n"
                    + "{\"is_negative\": true或false, \"emotion_type\": \"正常/愤怒/阴阳怪气/攻击性/负面\"}\n"
                    + "判断标准：包含人身攻击、辱骂、恶意讽刺、明显的负面情绪标记为负面；正常交流、友好讨论标记为正常。\n\n"
                    + "评论内容：" + content;

            log.info("========== AI情绪检测开始 ==========");
            log.info("评论内容长度: {} 字符", content.length());

            long startTime = System.currentTimeMillis();
            String response = callAiApi(prompt);
            long endTime = System.currentTimeMillis();

            log.info("情绪检测API耗时: {} ms", (endTime - startTime));

            if (StrUtil.isNotBlank(response)) {
                String cleaned = response.trim();
                // 去掉可能的 markdown 代码块标记
                cleaned = cleaned.replaceAll("^```json\\s*", "").replaceAll("^```\\s*", "").replaceAll("\\s*```$", "");
                
                try {
                    JSONObject jsonResult = JSONUtil.parseObj(cleaned);
                    result.put("is_negative", jsonResult.getBool("is_negative", false));
                    result.put("emotion_type", jsonResult.getStr("emotion_type", "正常"));
                    log.info("情绪检测结果: is_negative={}, emotion_type={}", 
                            result.get("is_negative"), result.get("emotion_type"));
                } catch (Exception e) {
                    log.warn("解析情绪检测JSON失败，原始响应: {}", cleaned);
                }
            }

            log.info("========== AI情绪检测完成 ==========");
        } catch (Exception e) {
            log.error("AI情绪检测异常: {}", e.getMessage(), e);
        }

        return result;
    }

    @Override
    public List<String> generateTags(String content) {
        if (StrUtil.isBlank(content)) {
            log.warn("生成标签失败：内容为空");
            return Collections.emptyList();
        }

        try {
            // 截取内容前部分
            String truncatedContent = content.length() > MAX_CONTENT_LENGTH 
                    ? content.substring(0, MAX_CONTENT_LENGTH) 
                    : content;

            String prompt = "请从以下文章内容中提取3-5个核心关键词或标签，只返回标签列表，用逗号分隔，不要有其他说明文字：\n\n" + truncatedContent;

            // 记录请求信息
            log.info("========== AI标签生成开始 ==========");
            log.info("原始内容长度: {} 字符", content.length());
            log.info("截断后内容长度: {} 字符", truncatedContent.length());
            log.info("Prompt长度: {} 字符", prompt.length());
            log.debug("Prompt内容: {}", prompt);

            long startTime = System.currentTimeMillis();
            String response = callAiApi(prompt);
            long endTime = System.currentTimeMillis();

            log.info("API调用耗时: {} ms", (endTime - startTime));
            
            if (StrUtil.isNotBlank(response)) {
                // 解析返回的标签，支持多种格式
                List<String> tags = parseTags(response);
                log.info("AI标签生成成功，标签数量: {}", tags.size());
                log.info("标签内容: {}", String.join(", ", tags));
                log.info("========== AI标签生成完成 ==========");
                return tags;
            } else {
                log.warn("AI返回结果为空");
            }
        } catch (Exception e) {
            log.error("调用AI生成标签失败，错误信息: {}", e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    /**
     * 调用AI API
     */
    private String callAiApi(String prompt) {
        log.info("准备调用DeepSeek API");
        log.info("API地址: {}", baseUrl);
        log.info("使用模型: {}", model);
        log.info("超时设置: {} ms", timeout);
        
        WebClient webClient = webClientBuilder.build();

        // 构建请求体（DeepSeek/OpenAI兼容格式）
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", model);
        
        // 构建messages数组
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "你是一个专业的内容分析助手，擅长提取文章摘要和关键词。生成摘要时严格控制在100字以内。");
        messages.add(systemMessage);
        
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", prompt);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);
        requestBody.set("temperature", 0.3); // 降低温度，使输出更稳定
        requestBody.set("max_tokens", 200); // 限制最大输出长度

        log.debug("请求体: {}", requestBody.toString());

        try {
            // 发送POST请求
            log.info("正在发送请求到DeepSeek API...");
            String responseBody = webClient.post()
                    .uri(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout))
                    .onErrorResume(error -> {
                        log.error("AI API调用失败: {}", error.getMessage(), error);
                        return Mono.just("");
                    })
                    .block();

            if (StrUtil.isNotBlank(responseBody)) {
                log.info("API响应接收成功，响应长度: {} 字符", responseBody.length());
                log.debug("原始响应: {}", responseBody);
                return extractContentFromResponse(responseBody);
            } else {
                log.warn("API响应为空");
            }
        } catch (Exception e) {
            log.error("API调用异常: {}", e.getMessage(), e);
        }

        return "";
    }

    /**
     * 从 API响应中提取内容
     */
    private String extractContentFromResponse(String responseBody) {
        try {
            log.info("开始解析API响应");
            JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                
            // 通义千问响应格式: output.text
            if (jsonResponse.containsKey("output")) {
                log.debug("检测到通义千问响应格式");
                JSONObject output = jsonResponse.getJSONObject("output");
                if (output != null && output.containsKey("text")) {
                    String content = output.getStr("text");
                    log.info("成功提取内容（通义千问格式），长度: {} 字符", content.length());
                    return content;
                }
            }
                
            // 兼容其他可能的响应格式
            if (jsonResponse.containsKey("choices")) {
                log.debug("检测到OpenAI/DeepSeek响应格式");
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    if (firstChoice.containsKey("message")) {
                        JSONObject message = firstChoice.getJSONObject("message");
                        String content = message.getStr("content");
                        log.info("成功提取内容（OpenAI格式），长度: {} 字符", content.length());
                        return content;
                    }
                    String content = firstChoice.getStr("text");
                    log.info("成功提取内容（text字段），长度: {} 字符", content.length());
                    return content;
                }
            }
                
            log.warn("无法从响应中提取内容，响应结构: {}", jsonResponse.keySet());
        } catch (Exception e) {
            log.error("解析AI响应失败，错误信息: {}", e.getMessage(), e);
        }
        return "";
    }

    /**
     * 解析标签字符串为列表
     */
    private List<String> parseTags(String tagString) {
        List<String> tags = new ArrayList<>();
        
        // 尝试JSON数组格式
        if (tagString.startsWith("[") && tagString.endsWith("]")) {
            try {
                JSONArray jsonArray = JSONUtil.parseArray(tagString);
                for (Object item : jsonArray) {
                    String tag = item.toString().trim();
                    if (StrUtil.isNotBlank(tag)) {
                        tags.add(tag);
                    }
                }
                return tags;
            } catch (Exception e) {
                // 如果JSON解析失败，继续尝试其他方式
            }
        }
        
        // 按逗号、顿号、分号分割
        String[] parts = tagString.split("[,，、;；\\n]");
        for (String part : parts) {
            String tag = part.trim().replaceAll("^['\"]|['\"]$", ""); // 去除引号
            if (StrUtil.isNotBlank(tag) && tags.size() < 5) {
                tags.add(tag);
            }
        }
        
        // 最多返回5个标签
        return tags.size() > 5 ? tags.subList(0, 5) : tags;
    }
}
