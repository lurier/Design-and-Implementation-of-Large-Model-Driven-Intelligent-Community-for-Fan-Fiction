package com.fanfaction.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Comment;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.CommentMapper;
import com.fanfaction.mapper.UserMapper;
import com.fanfaction.vo.DashboardStatsVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardService.class);

    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;

    public DashboardStatsVO getStats() {
        logger.info("管理端工作台统计数据查询");

        DashboardStatsVO stats = new DashboardStatsVO();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 用户总数
        stats.setTotalUsers(userMapper.selectCount(new LambdaQueryWrapper<>()));

        // 今日新增用户
        stats.setTodayNewUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));

        // 文章总数
        stats.setTotalArticles(articleMapper.selectCount(new LambdaQueryWrapper<>()));

        // 今日新增文章
        stats.setTodayNewArticles(articleMapper.selectCount(
                new LambdaQueryWrapper<Article>().ge(Article::getCreateTime, todayStart)));

        // 评论总数
        stats.setTotalComments(commentMapper.selectCount(new LambdaQueryWrapper<>()));

        // 今日新增评论
        stats.setTodayNewComments(commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().ge(Comment::getCreateTime, todayStart)));

        // 累计阅读量 (SUM)
        Long totalViews = 0L;
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().select(Article::getViewCount));
        for (Article a : articles) {
            if (a.getViewCount() != null) {
                totalViews += a.getViewCount();
            }
        }
        stats.setTotalViews(totalViews);

        // 近7天趋势
        stats.setArticleTrend(getArticleTrend());
        stats.setUserTrend(getUserTrend());

        logger.info("管理端工作台统计数据查询成功 - 用户: {}, 文章: {}, 评论: {}, 总阅读: {}",
                stats.getTotalUsers(), stats.getTotalArticles(), stats.getTotalComments(), stats.getTotalViews());
        return stats;
    }

    private List<DashboardStatsVO.DailyTrend> getArticleTrend() {
        List<DashboardStatsVO.DailyTrend> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            Long count = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>()
                            .ge(Article::getCreateTime, dayStart)
                            .lt(Article::getCreateTime, dayEnd));
            DashboardStatsVO.DailyTrend dt = new DashboardStatsVO.DailyTrend();
            dt.setDate(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            dt.setCount(count);
            trend.add(dt);
        }
        return trend;
    }

    private List<DashboardStatsVO.DailyTrend> getUserTrend() {
        List<DashboardStatsVO.DailyTrend> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .ge(User::getCreateTime, dayStart)
                            .lt(User::getCreateTime, dayEnd));
            DashboardStatsVO.DailyTrend dt = new DashboardStatsVO.DailyTrend();
            dt.setDate(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            dt.setCount(count);
            trend.add(dt);
        }
        return trend;
    }
}
