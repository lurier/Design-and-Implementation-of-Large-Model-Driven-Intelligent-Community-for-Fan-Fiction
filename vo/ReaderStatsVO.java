package com.fanfaction.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 读者数据统计 VO
 */
@Data
public class ReaderStatsVO {
    
    /**
     * 总阅读文章数
     */
    private Integer totalRead;
    
    /**
     * 总阅读时长（分钟）
     */
    private Integer totalMinutes;
    
    /**
     * 收藏文章数
     */
    private Integer favoriteCount;
    
    /**
     * 评论总数
     */
    private Integer commentCount;
    
    /**
     * 点赞总数
     */
    private Integer likeCount;
    
    /**
     * 偏好分类统计（分类名 -> 数量）
     */
    private List<Map<String, Object>> categoryDistribution;
    
    /**
     * 活跃时段统计（小时 -> 数量）
     */
    private List<Map<String, Object>> hourlyActivity;
    
    /**
     * 阅读趋势（日期 -> 数量）
     */
    private List<Map<String, Object>> readingTrend;
}
