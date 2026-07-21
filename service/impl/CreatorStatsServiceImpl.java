package com.fanfaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfaction.dto.CreatorStatsDTO;
import com.fanfaction.entity.Article;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.service.CreatorStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 创作者统计服务实现类
 */
@Service
@RequiredArgsConstructor
public class CreatorStatsServiceImpl implements CreatorStatsService {

    private final ArticleMapper articleMapper;

    @Override
    @Transactional(readOnly = true)
    public CreatorStatsDTO getCreatorStats(Long authorId) {
        return getCreatorStats(authorId, "WEEK");
    }

    @Override
    @Transactional(readOnly = true)
    public CreatorStatsDTO getCreatorStats(Long authorId, String dateRange) {
        // 查询作者所有已发布的文章
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getAuthorId, authorId)
                .eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1); // 已发布

        List<Article> articles = articleMapper.selectList(queryWrapper);

        // 计算统计数据
        int workCount = articles.size();
        long totalWords = articles.stream()
                .mapToLong(article -> article.getContent() != null ? article.getContent().length() : 0)
                .sum();
        long totalReads = articles.stream()
                .mapToLong(Article::getViewCount)
                .sum();
        long totalLikes = articles.stream()
                .mapToLong(Article::getLikeCount)
                .sum();
        long totalFavorites = articles.stream()
                .mapToLong(Article::getFavoriteCount)
                .sum();
        long totalComments = articles.stream()
                .mapToLong(Article::getCommentCount)
                .sum();

        // 构建阅读趋势数据
        CreatorStatsDTO.ReadTrendDTO readTrend = buildReadTrend(dateRange);

        // 构建作品排行
        List<CreatorStatsDTO.WorkStatsDTO> topWorks = articles.stream()
                .map(article -> CreatorStatsDTO.WorkStatsDTO.builder()
                        .id(article.getId())
                        .title(article.getTitle())
                        .readCount(article.getViewCount() != null ? article.getViewCount().longValue() : 0L)
                        .likeCount(article.getLikeCount() != null ? article.getLikeCount().longValue() : 0L)
                        .commentCount(article.getCommentCount() != null ? article.getCommentCount().longValue() : 0L)
                        .favoriteCount(article.getFavoriteCount() != null ? article.getFavoriteCount().longValue() : 0L)
                        .build())
                .sorted((CreatorStatsDTO.WorkStatsDTO a, CreatorStatsDTO.WorkStatsDTO b) -> Long.compare(b.getReadCount(), a.getReadCount()))
                .toList();

        return CreatorStatsDTO.builder()
                .workCount(workCount)
                .totalWords(totalWords)
                .totalReads(totalReads)
                .totalLikes(totalLikes)
                .totalFavorites(totalFavorites)
                .totalComments(totalComments)
                .readTrend(readTrend)
                .topWorks(topWorks)
                .build();
    }

    /**
     * 构建阅读趋势数据
     */
    private CreatorStatsDTO.ReadTrendDTO buildReadTrend(String dateRange) {
        List<String> dates = new ArrayList<>();
        List<Long> readCounts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        int days = "MONTH".equals(dateRange) ? 30 : 7;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
            // 这里可以根据实际需求从数据库查询每日阅读量
            // 暂时使用随机模拟数据
            readCounts.add((long) (Math.random() * 500 + 100));
        }

        return CreatorStatsDTO.ReadTrendDTO.builder()
                .dates(dates)
                .readCounts(readCounts)
                .build();
    }
}