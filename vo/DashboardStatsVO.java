package com.fanfaction.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardStatsVO {
    /** 用户总数 */
    private Long totalUsers;
    /** 今日新增用户 */
    private Long todayNewUsers;
    /** 文章总数 */
    private Long totalArticles;
    /** 今日新增文章 */
    private Long todayNewArticles;
    /** 评论总数 */
    private Long totalComments;
    /** 今日新增评论 */
    private Long todayNewComments;
    /** 累计阅读量 */
    private Long totalViews;
    /** 近7天每日新增文章 */
    private List<DailyTrend> articleTrend;
    /** 近7天每日新增用户 */
    private List<DailyTrend> userTrend;

    @Data
    public static class DailyTrend {
        private String date;
        private Long count;
    }
}
