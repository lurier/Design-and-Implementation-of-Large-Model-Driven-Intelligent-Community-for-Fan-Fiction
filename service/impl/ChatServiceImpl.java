package com.fanfaction.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fanfaction.common.BusinessException;
import com.fanfaction.service.ArticleService;
import com.fanfaction.service.ChatService;
import com.fanfaction.service.ContentCheckService;
import com.fanfaction.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智能客服服务实现
 * 接入DeepSeek API实现智能问答
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final WebClient.Builder webClientBuilder;
    private final ContentCheckService contentCheckService;
    private final ArticleService articleService;

    @Value("${ai.qwen.api-key}")
    private String apiKey;

    @Value("${ai.qwen.base-url}")
    private String baseUrl;

    @Value("${ai.qwen.model}")
    private String model;

    @Value("${ai.qwen.timeout}")
    private long timeout;

    /**
     * 系统提示词 - 定义AI角色和知识库
     */
    private static final String SYSTEM_PROMPT = """
            你是Fan Faction平台的智能客服助手，专门为用户提供帮助和解答问题。
            
            ## 平台介绍
            Fan Faction是一个面向同人创作者和读者的内容分享平台，用户可以发布、阅读和互动同人作品。
            
            ## 可用工具
            你拥有以下工具可以使用：
            
            ### search_articles(keyword)
            - 功能：根据关键词搜索平台上的文章
            - 参数：keyword - 搜索关键词（字符串）
            - 返回：最多3篇相关文章的标题和摘要
            
            ## 意图识别与工具使用规则
            当用户的问题包含以下关键词时，视为文章搜索意图：
            - "找文章"、"找一篇"、"想看"、"推荐"、"搜索"、"有没有"、"关于"
            
            ## 工具使用策略
            1. 如果用户的问题明确涉及文章查找（如："找一篇全职高手的文"）：
               - 调用 search_articles 工具进行搜索
               - 在回复中明确告知用户"正在为您搜索..."
               - 根据搜索结果用自然、友好的语言向用户推荐文章
            
            2. 如果用户只是询问搜索功能的使用方法（如："怎么搜索文章"）：
               - 按照常见操作指南回答，不需要调用工具
            
            ## 常见操作指南
            
            ### 如何收藏文章
            1. 在文章详情页找到"收藏"按钮（通常是一个心形图标）
            2. 点击按钮即可将文章加入收藏夹
            3. 在个人中心可以查看所有收藏的文章
            4. 再次点击可取消收藏
            
            ### 如何评论文章
            1. 在文章详情页底部找到评论输入框
            2. 输入评论内容（需遵守社区规范）
            3. 点击"发表"按钮提交评论
            4. 可以对其他用户的评论进行回复
            
            ### 如何发布文章
            1. 登录账号后，点击导航栏的"发布"按钮
            2. 填写文章标题和内容
            3. 选择合适的标签（最多5个）
            4. 点击"提交审核"按钮
            5. 等待管理员审核通过后即可发布
            
            ### 如何注销账号
            1. 进入"个人中心" -> "账号设置"
            2. 找到"账号安全"选项
            3. 点击"注销账号"
            4. 按照提示完成身份验证
            5. 确认注销（此操作不可恢复）
            
            ### 如何申请成为创作者
            1. 在个人中心找到"创作者申请"入口
            2. 填写申请信息（包括创作方向、代表作品等）
            3. 提交申请后等待审核
            4. 审核通过后即可获得发布权限
            
            ### 如何修改密码
            1. 进入"个人中心" -> "账号设置"
            2. 找到"修改密码"选项
            3. 输入原密码和新密码
            4. 点击"确认修改"
            
            ### 如何找回密码
            1. 在登录页面点击"忘记密码"
            2. 输入注册时使用的邮箱或手机号
            3. 按照提示完成验证
            4. 设置新密码
            
            ## 内容规范
            - 禁止发布色情、暴力、政治敏感等违规内容
            - 尊重版权，禁止未经授权转载他人作品
            - 禁止恶意攻击、辱骂其他用户
            
            ## 响应要求
            - 使用友好、专业的语气回答
            - 回答要简洁明了，避免冗长
            - 如果无法回答，应礼貌地告知用户
            - 涉及技术问题可建议联系管理员
            - 对于文章搜索请求，必须尝试调用工具或明确告知用户正在搜索
            
            ## 重要提示
            - 你的回答必须基于以上提供的知识库内容
            - 对于知识库中没有的信息，不要编造，应告知用户无法回答
            - 如果用户询问与平台功能无关的问题，可以友好地拒绝回答
            """;

    /**
     * 工具定义 Schema（DeepSeek Function Calling 格式）
     */
    private static final JSONArray TOOLS = JSONUtil.createArray();
    
    static {
        // 定义 search_articles 工具
        JSONObject tool = JSONUtil.createObj();
        tool.set("type", "function");
        
        JSONObject functionDef = JSONUtil.createObj();
        functionDef.set("name", "search_articles");
        functionDef.set("description", "根据关键词搜索平台上的文章");
        
        JSONObject params = JSONUtil.createObj();
        params.set("type", "object");
        
        JSONObject properties = JSONUtil.createObj();
        JSONObject keyword = JSONUtil.createObj();
        keyword.set("type", "string");
        keyword.set("description", "搜索关键词");
        properties.set("keyword", keyword);
        
        params.set("properties", properties);
        
        JSONArray required = JSONUtil.createArray();
        required.add("keyword");
        params.set("required", required);
        
        functionDef.set("parameters", params);
        tool.set("function", functionDef);
        
        TOOLS.add(tool);
    }

    @Override
    public String getChatResponse(Long userId, String question) {
        // 1. 敏感词过滤
        if (contentCheckService.hasSensitiveWord(question)) {
            log.warn("用户提问包含敏感词，已拒绝回答");
            throw new BusinessException(400, "您的问题包含不当内容，请重新表述");
        }

        // 2. 判断是否需要文章推荐
        boolean needsArticleRecommendation = isArticleRecommendationRequest(question);
        
        String userPrompt = question;
        
        // 3. 如果是文章推荐请求，先搜索相关文章（模拟工具调用）
        if (needsArticleRecommendation) {
            String keyword = extractKeyword(question);
            log.info("识别到文章搜索意图，提取关键词: {}", keyword);
            
            List<ArticleSearchResult> articles = searchArticles(keyword, 3);
            
            if (!articles.isEmpty()) {
                StringBuilder articlesInfo = new StringBuilder();
                articlesInfo.append("\n\n【工具调用结果 - search_articles(").append(keyword).append(")】\n");
                articlesInfo.append("搜索结果：\n");
                
                for (int i = 0; i < articles.size(); i++) {
                    ArticleSearchResult article = articles.get(i);
                    articlesInfo.append(String.format("%d. 《%s》\n", i + 1, article.title()));
                    if (StrUtil.isNotBlank(article.summary()) && !article.summary().equals(article.title())) {
                        articlesInfo.append(String.format("   摘要：%s\n", article.summary()));
                    }
                }
                
                articlesInfo.append("\n请用纯文本格式回复，不要使用任何Markdown格式（如*号、**加粗**等），保持简洁自然。");
                userPrompt += articlesInfo.toString();
            } else {
                userPrompt += "\n\n【工具调用结果 - search_articles】\n[]\n未找到相关文章。请直接告诉用户没有找到相关文章，使用纯文本格式。";
            }
        }

        // 4. 调用DeepSeek API
        return callDeepSeekApi(userPrompt);
    }

    @Override
    public List<ArticleSearchResult> searchArticles(String keyword, int limit) {
        List<ArticleSearchResult> results = new ArrayList<>();
        
        try {
            // 使用文章服务的搜索功能
            com.baomidou.mybatisplus.core.metadata.IPage<ArticleVO> page = 
                articleService.getArticlePage(1, limit, "time", keyword, null);
            
            for (ArticleVO article : page.getRecords()) {
                String summary = article.getSummary();
                // 摘要为空时，使用标题作为摘要
                if (StrUtil.isBlank(summary)) {
                    summary = article.getTitle();
                }
                // 截取摘要前100字
                if (summary.length() > 100) {
                    summary = summary.substring(0, 100) + "...";
                }
                
                results.add(new ArticleSearchResult(
                    article.getId(), 
                    article.getTitle(), 
                    summary
                ));
            }
        } catch (Exception e) {
            log.error("搜索文章失败: {}", e.getMessage());
        }
        
        return results;
    }

    /**
     * 判断是否是文章推荐请求
     */
    private boolean isArticleRecommendationRequest(String question) {
        String lowerQuestion = question.toLowerCase();
        return lowerQuestion.contains("有没有") || 
               lowerQuestion.contains("推荐") || 
               lowerQuestion.contains("想看") ||
               lowerQuestion.contains("找") ||
               lowerQuestion.contains("查") ||
               lowerQuestion.contains("搜索") ||
               (lowerQuestion.contains("关于") && lowerQuestion.contains("文章")) ||
               (lowerQuestion.contains("文") && lowerQuestion.length() > 2);
    }

    /**
     * 从问题中提取关键词
     */
    private String extractKeyword(String question) {
        // 移除常见的疑问词、标点和量词助词
        String keyword = question.replaceAll("[？?！!。，,、]", "")
                                .replaceAll("查找|有没有|推荐|关于|找|查|搜索|文章|文|一篇|一部|一个|一些|的|了|呢|吧|吗|一下|一下吧|看看|想看看|想找|想查", "")
                                .trim();
        
        // 如果提取结果为空，返回原问题作为关键词
        if (StrUtil.isBlank(keyword)) {
            keyword = question.replaceAll("[？?！!。，,、]", "").trim();
        }
        
        return keyword;
    }

    /**
     * 调用DeepSeek API（支持工具调用）
     */
    private String callDeepSeekApiWithTools(JSONArray messages) {
        log.info("========== 智能客服调用开始（支持工具调用）==========");
        
        WebClient webClient = webClientBuilder.build();
        int maxToolCalls = 3; // 最大工具调用次数
        
        for (int i = 0; i < maxToolCalls; i++) {
            // 构建请求体（DeepSeek兼容格式，支持工具调用）
            JSONObject requestBody = JSONUtil.createObj();
            requestBody.set("model", model);
            requestBody.set("messages", messages);
            requestBody.set("tools", TOOLS);
            requestBody.set("tool_choice", "auto");
            requestBody.set("temperature", 0.7);
            requestBody.set("max_tokens", 1000);

            log.debug("请求体: {}", requestBody.toString());

            try {
                long startTime = System.currentTimeMillis();
                
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
                            return Mono.error(new BusinessException(500, "网络开小差了，请稍后再试"));
                        })
                        .block();

                long endTime = System.currentTimeMillis();
                log.info("API调用耗时: {} ms", (endTime - startTime));
                
                if (StrUtil.isNotBlank(responseBody)) {
                    log.info("API响应接收成功，响应长度: {} 字符", responseBody.length());
                    
                    // 解析响应，检查是否需要工具调用
                    JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                    String content = extractContentFromResponse(responseBody);
                    
                    // 检查是否有工具调用请求
                    if (hasToolCall(jsonResponse)) {
                        log.info("检测到工具调用请求");
                        
                        // 解析工具调用参数
                        List<ToolCall> toolCalls = parseToolCalls(jsonResponse);
                        
                        // 执行工具调用
                        for (ToolCall toolCall : toolCalls) {
                            String toolResult = executeToolCall(toolCall);
                            
                            // 将工具调用结果添加到消息历史
                            JSONObject toolMessage = JSONUtil.createObj();
                            toolMessage.set("role", "tool");
                            toolMessage.set("content", toolResult);
                            toolMessage.set("tool_call_id", toolCall.id());
                            messages.add(toolMessage);
                        }
                        
                        // 继续循环，进行下一轮调用
                        continue;
                    }
                    
                    // 如果没有工具调用，直接返回内容
                    if (StrUtil.isNotBlank(content)) {
                        log.info("智能客服响应成功");
                        log.info("========== 智能客服调用完成 ==========");
                        return content;
                    }
                } else {
                    log.warn("API响应为空");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("DeepSeek API调用异常: {}", e.getMessage(), e);
            }
        }
        
        throw new BusinessException(500, "网络开小差了，请稍后再试");
    }

    /**
     * 检查响应是否包含工具调用
     */
    private boolean hasToolCall(JSONObject response) {
        try {
            if (response.containsKey("choices")) {
                JSONArray choices = response.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    if (firstChoice.containsKey("message")) {
                        JSONObject message = firstChoice.getJSONObject("message");
                        return message.containsKey("tool_calls") && 
                               !message.getJSONArray("tool_calls").isEmpty();
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查工具调用失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析工具调用
     */
    private List<ToolCall> parseToolCalls(JSONObject response) {
        List<ToolCall> toolCalls = new ArrayList<>();
        
        try {
            JSONArray choices = response.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                
                if (message.containsKey("tool_calls")) {
                    JSONArray toolCallsArray = message.getJSONArray("tool_calls");
                    
                    for (int i = 0; i < toolCallsArray.size(); i++) {
                        JSONObject toolCallObj = toolCallsArray.getJSONObject(i);
                        String id = toolCallObj.getStr("id");
                        String toolName = toolCallObj.getJSONObject("function").getStr("name");
                        String argsJson = toolCallObj.getJSONObject("function").getStr("arguments");
                        
                        JSONObject args = JSONUtil.parseObj(argsJson);
                        String keyword = args.getStr("keyword");
                        
                        toolCalls.add(new ToolCall(id, toolName, keyword));
                        log.info("解析工具调用: {}({})", toolName, keyword);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析工具调用失败: {}", e.getMessage());
        }
        
        return toolCalls;
    }

    /**
     * 执行工具调用
     */
    private String executeToolCall(ToolCall toolCall) {
        log.info("执行工具调用: {}({})", toolCall.functionName(), toolCall.keyword());
        
        if ("search_articles".equals(toolCall.functionName())) {
            List<ArticleSearchResult> articles = searchArticles(toolCall.keyword(), 3);
            
            if (!articles.isEmpty()) {
                StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append("{\n");
                resultBuilder.append("  \"status\": \"success\",\n");
                resultBuilder.append("  \"data\": [\n");
                
                for (int i = 0; i < articles.size(); i++) {
                    ArticleSearchResult article = articles.get(i);
                    resultBuilder.append("    {\n");
                    resultBuilder.append("      \"title\": \"").append(escapeJson(article.title())).append("\",\n");
                    resultBuilder.append("      \"summary\": \"").append(escapeJson(article.summary())).append("\"\n");
                    resultBuilder.append("    }");
                    if (i < articles.size() - 1) {
                        resultBuilder.append(",");
                    }
                    resultBuilder.append("\n");
                }
                
                resultBuilder.append("  ]\n");
                resultBuilder.append("}");
                
                return resultBuilder.toString();
            } else {
                return "{\"status\": \"success\", \"data\": [], \"message\": \"未找到相关文章\"}";
            }
        }
        
        return "{\"status\": \"error\", \"message\": \"未知工具: " + toolCall.functionName() + "\"}";
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        if (StrUtil.isBlank(str)) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }

    /**
     * 工具调用记录
     */
    private record ToolCall(String id, String functionName, String keyword) {}

    /**
     * 调用DeepSeek API（旧版，不支持工具调用）
     */
    private String callDeepSeekApi(String userPrompt) {
        log.info("========== 智能客服调用开始 ==========");
        log.info("用户提问: {}", userPrompt);
        
        WebClient webClient = webClientBuilder.build();

        // 构建请求体（DeepSeek兼容格式）
        JSONObject requestBody = JSONUtil.createObj();
        requestBody.set("model", model);
        
        // 构建messages数组
        JSONArray messages = JSONUtil.createArray();
        
        // System消息
        JSONObject systemMessage = JSONUtil.createObj();
        systemMessage.set("role", "system");
        systemMessage.set("content", SYSTEM_PROMPT);
        messages.add(systemMessage);
        
        // User消息
        JSONObject userMessage = JSONUtil.createObj();
        userMessage.set("role", "user");
        userMessage.set("content", userPrompt);
        messages.add(userMessage);
        
        requestBody.set("messages", messages);
        requestBody.set("temperature", 0.7);
        requestBody.set("max_tokens", 1000);

        log.debug("请求体: {}", requestBody.toString());

        try {
            long startTime = System.currentTimeMillis();
            
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
                        return Mono.error(new BusinessException(500, "网络开小差了，请稍后再试"));
                    })
                    .block();

            long endTime = System.currentTimeMillis();
            log.info("API调用耗时: {} ms", (endTime - startTime));
            
            if (StrUtil.isNotBlank(responseBody)) {
                log.info("API响应接收成功，响应长度: {} 字符", responseBody.length());
                String content = extractContentFromResponse(responseBody);
                
                if (StrUtil.isNotBlank(content)) {
                    log.info("智能客服响应成功");
                    log.info("========== 智能客服调用完成 ==========");
                    return content;
                }
            } else {
                log.warn("API响应为空");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek API调用异常: {}", e.getMessage(), e);
        }
        
        throw new BusinessException(500, "网络开小差了，请稍后再试");
    }

    /**
     * 从API响应中提取内容
     */
    private String extractContentFromResponse(String responseBody) {
        try {
            JSONObject jsonResponse = JSONUtil.parseObj(responseBody);
                
            // OpenAI/DeepSeek响应格式
            if (jsonResponse.containsKey("choices")) {
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);
                    if (firstChoice.containsKey("message")) {
                        JSONObject message = firstChoice.getJSONObject("message");
                        return message.getStr("content").trim();
                    }
                    if (firstChoice.containsKey("text")) {
                        return firstChoice.getStr("text").trim();
                    }
                }
            }
                
            // 通义千问响应格式
            if (jsonResponse.containsKey("output")) {
                JSONObject output = jsonResponse.getJSONObject("output");
                if (output != null && output.containsKey("text")) {
                    return output.getStr("text").trim();
                }
            }
                
            log.warn("无法从响应中提取内容");
        } catch (Exception e) {
            log.error("解析API响应失败: {}", e.getMessage());
        }
        return "";
    }
}