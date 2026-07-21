package com.fanfaction.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfaction.common.Result;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Interaction;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.InteractionMapper;
import com.fanfaction.mapper.UserMapper;
import com.fanfaction.service.RecommendationService;
import com.fanfaction.service.SentimentAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能功能测试控制器
 * 用于验证阅读时长计算、情感分析、UserCF推荐算法三大智能模块
 * 
 * 所有接口已配置为免登录访问，可直接在浏览器测试
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class SmartFeatureTestController {

    private final SentimentAnalysisService sentimentAnalysisService;
    private final RecommendationService recommendationService;
    private final InteractionMapper interactionMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    /**
     * 1. 阅读时长计算测试接口
     * 
     * @param wordCount 文章字数
     * @return 阅读时长（分钟）
     * 
     * 测试示例：http://localhost:8080/api/test/read-time?wordCount=650
     * 期望结果：3分钟（650/300 = 2.17，向上取整为3）
     */
    @GetMapping("/read-time")
    public Result<Map<String, Object>> testReadTime(@RequestParam int wordCount) {
        log.info("========== 阅读时长计算测试 ==========");
        log.info("输入字数: {}", wordCount);
        
        // 按照每分钟300字，向上取整的规则计算
        int readTime = (int) Math.ceil(wordCount / 300.0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("wordCount", wordCount);
        result.put("readTime", readTime);
        result.put("rule", "每分钟300字，向上取整");
        result.put("calculation", String.format("%.2f / 300 = %.2f，向上取整为 %d", 
                (double)wordCount, wordCount / 300.0, readTime));
        
        log.info("计算结果: {} 分钟", readTime);
        log.info("=====================================");
        
        return Result.success(result);
    }

    /**
     * 2. 情感分析测试接口
     * 
     * @param commentText 评论文本
     * @return 情感倾向和分值
     * 
     * 测试示例1（积极）：
     * POST http://localhost:8080/api/test/sentiment
     * Body: {"commentText": "这篇文章写得太棒了！"}
     * 期望结果：positive, score > 0
     * 
     * 测试示例2（消极）：
     * POST http://localhost:8080/api/test/sentiment
     * Body: {"commentText": "完全看不懂，浪费时间"}
     * 期望结果：negative, score < 0
     */
    @PostMapping("/sentiment")
    public Result<Map<String, Object>> testSentiment(@RequestBody Map<String, String> request) {
        String commentText = request.get("commentText");
        
        log.info("========== 情感分析测试 ==========");
        log.info("评论文本: {}", commentText);
        
        try {
            // 调用HanLP情感分析服务
            double sentimentScore = sentimentAnalysisService.analyzeSentiment(commentText);
            String sentimentType = sentimentAnalysisService.getSentimentType(sentimentScore);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("commentText", commentText);
            result.put("sentimentScore", sentimentScore);
            result.put("sentimentType", sentimentType);
            result.put("sentimentLabel", getSentimentLabel(sentimentType));
            
            // 判断是否符合预期
            if (sentimentScore > 0) {
                result.put("evaluation", "✅ 积极评论，分值为正数");
            } else if (sentimentScore < 0) {
                result.put("evaluation", "✅ 消极评论，分值为负数");
            } else {
                result.put("evaluation", "⚠️ 中性评论，分值为0");
            }
            
            log.info("情感分值: {}", sentimentScore);
            log.info("情感类型: {}", sentimentType);
            log.info("评估结果: {}", result.get("evaluation"));
            log.info("=====================================");
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("情感分析测试失败", e);
            return Result.error(500, "情感分析失败: " + e.getMessage());
        }
    }

    /**
     * 3. UserCF推荐算法数据模拟与验证接口
     * 
     * 模拟数据：
     * - 用户A 点赞了 文章1、文章2
     * - 用户B 点赞了 文章2、文章3
     * - 用户C 点赞了 文章1
     * 
     * 推荐逻辑：用户C和用户A都点了文章1 → 兴趣相似 → 推荐用户A相似用户（用户B）喜欢的文章3给用户C
     * 
     * 测试示例：http://localhost:8080/api/test/usercf-mock
     * 期望结果：推荐列表中包含文章3
     */
    @GetMapping("/usercf-mock")
    public Result<Map<String, Object>> testUserCFMock() {
        log.info("========== UserCF推荐算法测试 ==========");
        
        try {
            // 步骤1：准备测试数据（模拟用户和文章）
            log.info("步骤1: 准备测试数据...");
            Map<String, Object> testData = prepareTestData();
            
            Long userAId = (Long) testData.get("userAId");
            Long userBId = (Long) testData.get("userBId");
            Long userCId = (Long) testData.get("userCId");
            Long article1Id = (Long) testData.get("article1Id");
            Long article2Id = (Long) testData.get("article2Id");
            Long article3Id = (Long) testData.get("article3Id");
            
            log.info("用户A ID: {}, 点赞文章: {}, {}", userAId, article1Id, article2Id);
            log.info("用户B ID: {}, 点赞文章: {}, {}", userBId, article2Id, article3Id);
            log.info("用户C ID: {}, 点赞文章: {}", userCId, article1Id);
            
            // 步骤2：清除旧的交互数据（避免干扰）
            log.info("步骤2: 清除旧数据...");
            clearOldInteractions(userAId, userBId, userCId);
            
            // 步骤3：插入模拟的交互数据
            log.info("步骤3: 插入模拟交互数据...");
            insertMockInteractions(userAId, userBId, userCId, article1Id, article2Id, article3Id);
            
            // 步骤4：调用UserCF算法为用户C生成推荐
            log.info("步骤4: 调用UserCF算法为用户C生成推荐...");
            List<Article> recommendedArticles = recommendationService.recommendArticlesForUser(userCId, 5);
            
            // 步骤5：分析推荐结果
            log.info("步骤5: 分析推荐结果...");
            List<Long> recommendedArticleIds = recommendedArticles.stream()
                    .map(Article::getId)
                    .collect(Collectors.toList());
            
            boolean containsArticle3 = recommendedArticleIds.contains(article3Id);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("testData", testData);
            result.put("recommendedArticles", recommendedArticles);
            result.put("recommendedArticleIds", recommendedArticleIds);
            result.put("containsArticle3", containsArticle3);
            
            if (containsArticle3) {
                result.put("evaluation", "✅ 测试通过！文章3被推荐给用户C");
                result.put("explanation", "用户C和用户A都点赞了文章1，因此兴趣相似。" +
                        "用户B和用户A都点赞了文章2，因此也兴趣相似。" +
                        "用户B点赞了文章3，而用户C还未看过，所以文章3被推荐给用户C。");
            } else {
                result.put("evaluation", "⚠️ 测试未完全符合预期，文章3未被推荐");
                result.put("explanation", "可能是因为相似度计算或推荐逻辑有其他因素影响");
            }
            
            log.info("推荐结果: {}", recommendedArticleIds);
            log.info("是否包含文章3: {}", containsArticle3);
            log.info("评估结果: {}", result.get("evaluation"));
            log.info("=====================================");
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("UserCF推荐算法测试失败", e);
            return Result.error(500, "UserCF测试失败: " + e.getMessage());
        }
    }

    /**
     * 4. 基于内容的推荐算法测试接口
     * 
     * 测试逻辑：
     * 1. 创建测试用户和带标签的文章
     * 2. 模拟用户交互（点赞/收藏/阅读）
     * 3. 调用基于内容的推荐算法
     * 4. 验证推荐结果是否包含相同标签的文章
     * 
     * 测试示例：http://localhost:8080/api/test/content-based-recommend
     */
    @GetMapping("/content-based-recommend")
    public Result<Map<String, Object>> testContentBasedRecommendation() {
        log.info("========== 基于内容的推荐算法测试 ==========");
        
        try {
            // 步骤1：准备测试数据（创建带标签的文章）
            log.info("步骤1: 准备测试数据...");
            Map<String, Object> testData = prepareContentBasedTestData();
            
            Long testUserId = (Long) testData.get("testUserId");
            Long sciFiArticleId1 = (Long) testData.get("sciFiArticleId1");
            Long sciFiArticleId2 = (Long) testData.get("sciFiArticleId2");
            Long romanceArticleId = (Long) testData.get("romanceArticleId");
            Long sciFiArticleId3 = (Long) testData.get("sciFiArticleId3"); // 未交互的科幻文章
            
            log.info("测试用户ID: {}", testUserId);
            log.info("科幻文章1 ID: {}", sciFiArticleId1);
            log.info("科幻文章2 ID: {}", sciFiArticleId2);
            log.info("言情文章 ID: {}", romanceArticleId);
            log.info("未交互的科幻文章3 ID: {}", sciFiArticleId3);
            
            // 步骤2：清除旧的交互数据
            log.info("步骤2: 清除旧数据...");
            clearOldInteractions(testUserId);
            
            // 步骤3：模拟用户交互（用户看了2篇科幻，1篇言情）
            log.info("步骤3: 插入模拟交互数据...");
            createInteraction(testUserId, sciFiArticleId1, 3); // 阅读
            createInteraction(testUserId, sciFiArticleId2, 1); // 点赞
            createInteraction(testUserId, romanceArticleId, 2); // 收藏
            
            // 步骤4：调用基于内容的推荐算法
            log.info("步骤4: 调用基于内容的推荐算法...");
            List<Article> recommendedArticles = recommendationService.recommendArticlesForUser(testUserId, 5);
            
            // 步骤5：分析推荐结果
            log.info("步骤5: 分析推荐结果...");
            List<Long> recommendedArticleIds = recommendedArticles.stream()
                    .map(Article::getId)
                    .collect(Collectors.toList());
            
            boolean containsSciFiArticle3 = recommendedArticleIds.contains(sciFiArticleId3);
            boolean notContainsInteractedArticles = !recommendedArticleIds.contains(sciFiArticleId1) && 
                                                     !recommendedArticleIds.contains(sciFiArticleId2) && 
                                                     !recommendedArticleIds.contains(romanceArticleId);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("testData", testData);
            result.put("recommendedArticles", recommendedArticles);
            result.put("recommendedArticleIds", recommendedArticleIds);
            result.put("containsSciFiArticle3", containsSciFiArticle3);
            result.put("notContainsInteractedArticles", notContainsInteractedArticles);
            
            if (containsSciFiArticle3 && notContainsInteractedArticles) {
                result.put("evaluation", "✅ 测试通过！基于内容的推荐算法正常工作");
                result.put("explanation", "用户看了2篇科幻文章和1篇言情文章，科幻是核心兴趣。" +
                        "算法正确推荐了未交互过的科幻文章3，并且排除了已交互过的文章。");
            } else {
                result.put("evaluation", "⚠️ 测试未完全符合预期");
                if (!containsSciFiArticle3) {
                    result.put("explanation", "未推荐科幻文章3，可能是标签匹配逻辑需要调整。");
                } else if (!notContainsInteractedArticles) {
                    result.put("explanation", "推荐了用户已交互过的文章，需要检查过滤逻辑。");
                }
            }
            
            log.info("推荐结果: {}", recommendedArticleIds);
            log.info("是否包含科幻文章3: {}", containsSciFiArticle3);
            log.info("是否排除已交互文章: {}", notContainsInteractedArticles);
            log.info("评估结果: {}", result.get("evaluation"));
            log.info("=====================================");
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("基于内容的推荐算法测试失败", e);
            return Result.error(500, "基于内容推荐测试失败: " + e.getMessage());
        }
    }

    /**
     * 5. 获取热门文章测试接口（新用户兜底策略）
     * 
     * 测试示例：http://localhost:8080/api/test/hot-articles?limit=5
     */
    @GetMapping("/hot-articles")
    public Result<Map<String, Object>> testGetHotArticles(@RequestParam(defaultValue = "5") int limit) {
        log.info("========== 热门文章测试 ==========");
        
        try {
            List<Article> hotArticles = recommendationService.recommendArticlesForUser(99999L, limit);
            
            Map<String, Object> result = new HashMap<>();
            result.put("limit", limit);
            result.put("hotArticles", hotArticles);
            result.put("count", hotArticles.size());
            result.put("evaluation", hotArticles.isEmpty() ? "⚠️ 没有找到热门文章" : "✅ 获取热门文章成功");
            
            log.info("获取到 {} 篇热门文章", hotArticles.size());
            log.info("=====================================");
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("获取热门文章失败", e);
            return Result.error(500, "获取热门文章失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/smart-features/health")
    public Result<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("service", "Smart Feature Test Controller");
        status.put("status", "running");
        status.put("message", "智能功能测试服务正常运行");
        
        return Result.success(status);
    }

    // ==================== 辅助方法 ====================

    /**
     * 准备测试数据（创建用户和文章）
     */
    private Map<String, Object> prepareTestData() {
        // 查找或创建测试用户
        Long userAId = getOrCreateTestUser("test_user_a", "测试用户A");
        Long userBId = getOrCreateTestUser("test_user_b", "测试用户B");
        Long userCId = getOrCreateTestUser("test_user_c", "测试用户C");
        
        // 查找或创建测试文章
        Long article1Id = getOrCreateTestArticle("测试文章1", "这是第一篇测试文章的内容，用于UserCF算法测试。");
        Long article2Id = getOrCreateTestArticle("测试文章2", "这是第二篇测试文章的内容，用于UserCF算法测试。");
        Long article3Id = getOrCreateTestArticle("测试文章3", "这是第三篇测试文章的内容，用于UserCF算法测试。");
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("userAId", userAId);
        testData.put("userBId", userBId);
        testData.put("userCId", userCId);
        testData.put("userAName", "测试用户A");
        testData.put("userBName", "测试用户B");
        testData.put("userCName", "测试用户C");
        testData.put("article1Id", article1Id);
        testData.put("article2Id", article2Id);
        testData.put("article3Id", article3Id);
        testData.put("article1Title", "测试文章1");
        testData.put("article2Title", "测试文章2");
        testData.put("article3Title", "测试文章3");
        
        return testData;
    }

    /**
     * 获取或创建测试用户
     */
    private Long getOrCreateTestUser(String username, String nickname) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User existingUser = userMapper.selectOne(wrapper);
        
        if (existingUser != null) {
            return existingUser.getId();
        }
        
        // 创建新用户
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setNickname(nickname);
        newUser.setPassword("$2a$10$ckqmY74oMoAeGhVaStOZ1uuoNmSnf5UZUrtPCnqQB5kJN6ypS5bKe"); // 加密密码
        newUser.setEmail(username + "@test.com");
        newUser.setStatus(1);
        newUser.setRoles("ROLE_USER");
        newUser.setDeleted(0);
        
        userMapper.insert(newUser);
        log.info("创建测试用户: {} (ID: {})", nickname, newUser.getId());
        
        return newUser.getId();
    }

    /**
     * 获取或创建测试文章
     */
    private Long getOrCreateTestArticle(String title, String content) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, title);
        Article existingArticle = articleMapper.selectOne(wrapper);
        
        if (existingArticle != null) {
            return existingArticle.getId();
        }
        
        // 创建新文章
        Article newArticle = new Article();
        newArticle.setTitle(title);
        newArticle.setContent(content);
        newArticle.setSummary("这是一篇测试文章的摘要");
        newArticle.setTags("测试,UserCF");
        newArticle.setAuthorId(1L); // 默认作者ID
        newArticle.setViewCount(0);
        newArticle.setLikeCount(0);
        newArticle.setFavoriteCount(0);
        newArticle.setCommentCount(0);
        newArticle.setStatus(1);
        newArticle.setDeleted(0);
        
        articleMapper.insert(newArticle);
        log.info("创建测试文章: {} (ID: {})", title, newArticle.getId());
        
        return newArticle.getId();
    }

    /**
     * 清除旧的交互数据
     */
    private void clearOldInteractions(Long... userIds) {
        for (Long userId : userIds) {
            LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Interaction::getUserId, userId);
            interactionMapper.delete(wrapper);
            log.info("清除用户 ID: {} 的交互数据", userId);
        }
    }

    /**
     * 插入模拟的交互数据
     */
    private void insertMockInteractions(Long userAId, Long userBId, Long userCId,
                                       Long article1Id, Long article2Id, Long article3Id) {
        // 用户A 点赞了 文章1、文章2
        createInteraction(userAId, article1Id, 1); // type=1 表示点赞
        createInteraction(userAId, article2Id, 1);
        
        // 用户B 点赞了 文章2、文章3
        createInteraction(userBId, article2Id, 1);
        createInteraction(userBId, article3Id, 1);
        
        // 用户C 点赞了 文章1
        createInteraction(userCId, article1Id, 1);
        
        log.info("插入模拟交互数据完成");
    }

    /**
     * 创建交互记录
     */
    private void createInteraction(Long userId, Long articleId, Integer type) {
        Interaction interaction = new Interaction();
        interaction.setUserId(userId);
        interaction.setArticleId(articleId);
        interaction.setType(type);
        interaction.setCreateTime(java.time.LocalDateTime.now());
        
        interactionMapper.insert(interaction);
        log.info("创建交互: 用户{} {} 文章{}", userId, type == 1 ? "点赞" : type == 2 ? "收藏" : "阅读", articleId);
    }

    /**
     * 准备基于内容推荐的测试数据（创建带标签的文章）
     */
    private Map<String, Object> prepareContentBasedTestData() {
        // 创建测试用户
        Long testUserId = getOrCreateTestUser("test_content_user", "基于内容测试用户");
        
        // 创建带标签的测试文章
        Long sciFiArticleId1 = getOrCreateTaggedArticle("科幻测试文章1", "这是一篇科幻小说的内容...", "科幻,未来,太空");
        Long sciFiArticleId2 = getOrCreateTaggedArticle("科幻测试文章2", "这是另一篇科幻小说的内容...", "科幻,星际,冒险");
        Long romanceArticleId = getOrCreateTaggedArticle("言情测试文章", "这是一篇言情小说的内容...", "言情,爱情,校园");
        Long sciFiArticleId3 = getOrCreateTaggedArticle("科幻测试文章3", "这是第三篇未交互的科幻小说...", "科幻,未来,科技");
        
        Map<String, Object> testData = new HashMap<>();
        testData.put("testUserId", testUserId);
        testData.put("testUserName", "基于内容测试用户");
        testData.put("sciFiArticleId1", sciFiArticleId1);
        testData.put("sciFiArticleId2", sciFiArticleId2);
        testData.put("romanceArticleId", romanceArticleId);
        testData.put("sciFiArticleId3", sciFiArticleId3);
        testData.put("sciFiArticleTitle1", "科幻测试文章1");
        testData.put("sciFiArticleTitle2", "科幻测试文章2");
        testData.put("romanceArticleTitle", "言情测试文章");
        testData.put("sciFiArticleTitle3", "科幻测试文章3");
        
        return testData;
    }

    /**
     * 获取或创建带标签的测试文章
     */
    private Long getOrCreateTaggedArticle(String title, String content, String tags) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getTitle, title);
        Article existingArticle = articleMapper.selectOne(wrapper);
        
        if (existingArticle != null) {
            return existingArticle.getId();
        }
        
        // 创建新文章
        Article newArticle = new Article();
        newArticle.setTitle(title);
        newArticle.setContent(content);
        newArticle.setSummary("这是一篇" + tags.split(",")[0] + "类型的测试文章");
        newArticle.setTags(tags);
        newArticle.setAuthorId(1L);
        newArticle.setViewCount(100);
        newArticle.setLikeCount(10);
        newArticle.setFavoriteCount(5);
        newArticle.setCommentCount(2);
        newArticle.setStatus(1);
        newArticle.setDeleted(0);
        
        articleMapper.insert(newArticle);
        log.info("创建带标签测试文章: {} (ID: {}, 标签: {})", title, newArticle.getId(), tags);
        
        return newArticle.getId();
    }

    /**
     * 获取情感标签的中文描述
     */
    private String getSentimentLabel(String sentimentType) {
        switch (sentimentType) {
            case "positive":
                return "积极";
            case "negative":
                return "消极";
            default:
                return "中性";
        }
    }
}
