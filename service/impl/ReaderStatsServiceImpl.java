package com.fanfaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Comment;
import com.fanfaction.entity.Interaction;
import com.fanfaction.entity.ReadingHistory;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.CommentMapper;
import com.fanfaction.mapper.InteractionMapper;
import com.fanfaction.mapper.ReadingHistoryMapper;
import com.fanfaction.service.ReaderStatsService;
import com.fanfaction.vo.ReaderStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * 读者统计服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReaderStatsServiceImpl implements ReaderStatsService {
    
    private final ReadingHistoryMapper readingHistoryMapper;
    private final InteractionMapper interactionMapper;
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    
    @Override
    public ReaderStatsVO getUserStats(Long userId) {
        log.info("开始统计用户 {} 的阅读数据", userId);
        
        try {
            ReaderStatsVO stats = new ReaderStatsVO();
            
            // 1. 统计阅读历史
            LambdaQueryWrapper<ReadingHistory> historyWrapper = new LambdaQueryWrapper<>();
            historyWrapper.eq(ReadingHistory::getUserId, userId);
            List<ReadingHistory> historyList = readingHistoryMapper.selectList(historyWrapper);
            log.info("用户 {} 的阅读历史数量：{}", userId, historyList.size());
            
            stats.setTotalRead(historyList.size());
            
            // 2. 计算总阅读时长（秒转分钟）
            int totalSeconds = historyList.stream()
                    .mapToInt(h -> h.getReadDuration() != null ? h.getReadDuration() : 0)
                    .sum();
            stats.setTotalMinutes(totalSeconds / 60);
            
            // 3. 统计收藏数量
            LambdaQueryWrapper<Interaction> favoriteWrapper = new LambdaQueryWrapper<>();
            favoriteWrapper.eq(Interaction::getUserId, userId)
                    .eq(Interaction::getType, 1); // 1 表示收藏
            List<Interaction> favorites = interactionMapper.selectList(favoriteWrapper);
            stats.setFavoriteCount(favorites.size());
            
            // 4. 统计评论数量
            LambdaQueryWrapper<Comment> commentWrapper = new LambdaQueryWrapper<>();
            commentWrapper.eq(Comment::getUserId, userId);
            List<Comment> comments = commentMapper.selectList(commentWrapper);
            stats.setCommentCount(comments.size());
            
            // 5. 统计点赞数量
            LambdaQueryWrapper<Interaction> likeWrapper = new LambdaQueryWrapper<>();
            likeWrapper.eq(Interaction::getUserId, userId)
                    .eq(Interaction::getType, 0); // 0 表示点赞
            List<Interaction> likes = interactionMapper.selectList(likeWrapper);
            stats.setLikeCount(likes.size());
            
            // 6. 偏好分类统计（饼图数据）
            stats.setCategoryDistribution(getCategoryDistribution(historyList));
            
            // 7. 活跃时段统计（柱状图数据）
            stats.setHourlyActivity(getHourlyActivity(historyList));
            
            // 8. 阅读趋势统计（折线图数据，近 30 天）
            stats.setReadingTrend(getReadingTrend(historyList));
            
            log.info("用户 {} 的统计数据：阅读={}, 时长={}分钟，收藏={}, 评论={}",
                    userId, stats.getTotalRead(), stats.getTotalMinutes(),
                    stats.getFavoriteCount(), stats.getCommentCount());
            
            return stats;
        } catch (Exception e) {
            log.error("统计用户 {} 的阅读数据失败", userId, e);
            // 返回空的统计数据，避免接口失败
            ReaderStatsVO stats = new ReaderStatsVO();
            stats.setTotalRead(0);
            stats.setTotalMinutes(0);
            stats.setFavoriteCount(0);
            stats.setCommentCount(0);
            stats.setLikeCount(0);
            stats.setCategoryDistribution(Collections.emptyList());
            stats.setHourlyActivity(Collections.emptyList());
            stats.setReadingTrend(Collections.emptyList());
            return stats;
        }
    }
    
    /**
     * 获取偏好分类分布（基于 tags 字段）
     */
    private List<Map<String, Object>> getCategoryDistribution(List<ReadingHistory> historyList) {
        if (historyList == null || historyList.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // 提取所有文章 ID
            Set<Long> articleIds = historyList.stream()
                    .map(ReadingHistory::getArticleId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            if (articleIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 批量查询文章
            List<Article> articles = articleMapper.selectBatchIds(articleIds);
            if (articles == null || articles.isEmpty()) {
                return Collections.emptyList();
            }
            
            Map<Long, Article> articleMap = articles.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Article::getId, a -> a, (v1, v2) -> v1));
            
            // 按标签分组统计（tags 字段可能包含多个标签，用逗号分隔）
            Map<String, Long> tagCount = historyList.stream()
                    .map(ReadingHistory::getArticleId)
                    .map(articleMap::get)
                    .filter(Objects::nonNull)
                    .map(Article::getTags)
                    .filter(Objects::nonNull)
                    .flatMap(tags -> Arrays.stream(tags.split(",")))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
            
            if (tagCount.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 转换为图表格式
            return tagCount.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("name", entry.getKey());
                        item.put("value", entry.getValue());
                        return item;
                    })
                    .sorted(Comparator.comparingLong(item -> -(Long)item.get("value")))
                    .limit(5) // 只显示前 5 个标签
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("统计偏好分类分布失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取活跃时段分布（按小时统计）
     */
    private List<Map<String, Object>> getHourlyActivity(List<ReadingHistory> historyList) {
        // 初始化 24 小时数据
        Map<Integer, Long> hourlyCount = new HashMap<>();
        for (int i = 0; i < 24; i++) {
            hourlyCount.put(i, 0L);
        }
        
        // 统计每个小时的阅读次数
        historyList.forEach(history -> {
            if (history.getLastReadTime() != null) {
                int hour = history.getLastReadTime().getHour();
                hourlyCount.put(hour, hourlyCount.get(hour) + 1);
            }
        });
        
        // 转换为图表格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("hour", i);
            item.put("value", hourlyCount.get(i));
            result.add(item);
        }
        
        return result;
    }
    
    /**
     * 获取阅读趋势（近 30 天）
     */
    private List<Map<String, Object>> getReadingTrend(List<ReadingHistory> historyList) {
        LocalDate today = LocalDate.now();
        Map<String, Long> dailyCount = new LinkedHashMap<>();
        
        // 初始化近 30 天的数据
        for (int i = 29; i >= 0; i--) {
            String date = today.minusDays(i).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
            dailyCount.put(date, 0L);
        }
        
        // 统计每天的阅读次数
        historyList.forEach(history -> {
            if (history.getLastReadTime() != null) {
                String date = history.getLastReadTime().toLocalDate().format(
                        java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                if (dailyCount.containsKey(date)) {
                    dailyCount.put(date, dailyCount.get(date) + 1);
                }
            }
        });
        
        // 转换为图表格式
        return dailyCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", entry.getKey());
                    item.put("value", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
