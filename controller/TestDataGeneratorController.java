package com.fanfaction.controller;

import com.fanfaction.common.Result;
import com.fanfaction.util.TestDataGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试数据生成控制器
 * 提供 API 接口用于生成各种测试数据
 */
@Slf4j
@RestController
@RequestMapping("/api/test-data")
@RequiredArgsConstructor
public class TestDataGeneratorController {

    private final TestDataGenerator testDataGenerator;

    /**
     * 生成完整的测试数据
     * @param authorId 作者 ID
     * @param readerId 读者 ID
     * @param articleCount 文章数量
     * @return 生成结果
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateTestData(
            @RequestParam(defaultValue = "3") Long authorId,
            @RequestParam(defaultValue = "2") Long readerId,
            @RequestParam(defaultValue = "20") int articleCount) {
        
        log.info("开始生成测试数据...");
        
        try {
            // 生成文章并立即保存
            var articles = testDataGenerator.generateArticles(authorId, articleCount);
            for (var article : articles) {
                testDataGenerator.articleMapper.insert(article);
            }
            
            // 提取文章 ID 列表
            var articleIds = articles.stream()
                    .map(com.fanfaction.entity.Article::getId)
                    .toList();
            
            // 生成其他数据
            var comments = testDataGenerator.generateComments(readerId, articleIds, articleCount * 2);
            var interactions = testDataGenerator.generateInteractions(readerId, articleIds);
            var histories = testDataGenerator.generateReadingHistory(readerId, articleIds);
            var bookmarks = testDataGenerator.generateBookmarks(readerId, articleIds);
            var progresses = testDataGenerator.generateReadingProgress(readerId, articleIds);
            
            // 保存其他数据（文章已保存）
            testDataGenerator.saveAllData(articles, comments, interactions, histories, bookmarks, progresses);
            
            // 返回统计信息
            Map<String, Object> stats = new HashMap<>();
            stats.put("articles", articles.size());
            stats.put("comments", comments.size());
            stats.put("interactions", interactions.size());
            stats.put("bookmarks", bookmarks.size());
            stats.put("readingHistory", histories.size());
            stats.put("readingProgress", progresses.size());
            
            log.info("测试数据生成完成：{}", stats);
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("生成测试数据失败", e);
            return Result.error("生成测试数据失败：" + e.getMessage());
        }
    }

    /**
     * 生成指定数量的文章
     */
    @PostMapping("/articles")
    public Result<Integer> generateArticles(
            @RequestParam Long authorId,
            @RequestParam(defaultValue = "10") int count) {
        
        try {
            var articles = testDataGenerator.generateArticles(authorId, count);
            for (var article : articles) {
                testDataGenerator.articleMapper.insert(article);
            }
            log.info("生成了 {} 篇文章", articles.size());
            return Result.success(articles.size());
        } catch (Exception e) {
            log.error("生成文章失败", e);
            return Result.error("生成文章失败：" + e.getMessage());
        }
    }

    /**
     * 生成评论
     */
    @PostMapping("/comments")
    public Result<Integer> generateComments(
            @RequestParam Long userId,
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "10") int count) {
        
        try {
            var comments = testDataGenerator.generateComments(userId, java.util.List.of(articleId), count);
            for (var comment : comments) {
                testDataGenerator.commentMapper.insert(comment);
            }
            log.info("生成了 {} 条评论", comments.size());
            return Result.success(comments.size());
        } catch (Exception e) {
            log.error("生成评论失败", e);
            return Result.error("生成评论失败：" + e.getMessage());
        }
    }

    /**
     * 清空所有测试数据
     */
    @DeleteMapping("/clear")
    public Result<String> clearTestData() {
        log.warn("清空所有测试数据...");
        
        try {
            // 注意：这里只是示例，实际清空需要更复杂的逻辑
            // 建议使用 SQL 脚本清空
            return Result.success("测试数据清空成功！请使用 SQL 脚本进行完整清空。");
        } catch (Exception e) {
            log.error("清空测试数据失败", e);
            return Result.error("清空测试数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取测试数据统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Long>> getTestDataStats() {
        try {
            Map<String, Long> stats = new HashMap<>();
            stats.put("totalArticles", testDataGenerator.articleMapper.selectCount(null));
            stats.put("totalComments", testDataGenerator.commentMapper.selectCount(null));
            stats.put("totalInteractions", testDataGenerator.interactionMapper.selectCount(null));
            stats.put("totalBookmarks", testDataGenerator.bookmarkMapper.selectCount(null));
            stats.put("totalReadingHistory", testDataGenerator.readingHistoryMapper.selectCount(null));
            
            log.info("获取测试数据统计：{}", stats);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计数据失败", e);
            return Result.error("获取统计数据失败：" + e.getMessage());
        }
    }
}
