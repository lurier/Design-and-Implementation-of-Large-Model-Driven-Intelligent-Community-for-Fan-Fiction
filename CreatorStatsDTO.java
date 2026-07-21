package com.fanfaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创作者统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatorStatsDTO {

    /**
     * 总作品数
     */
    private Integer workCount;

    /**
     * 总字数
     */
    private Long totalWords;

    /**
     * 累计阅读量
     */
    private Long totalReads;

    /**
     * 总点赞数
     */
    private Long totalLikes;

    /**
     * 总收藏数
     */
    private Long totalFavorites;

    /**
     * 总评论数
     */
    private Long totalComments;

    /**
     * 粉丝数量
     */
    private Integer fansCount;

    /**
     * 阅读趋势数据
     */
    private ReadTrendDTO readTrend;

    /**
     * 作品表现排行
     */
    private List<WorkStatsDTO> topWorks;

    /**
     * 阅读趋势DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadTrendDTO {
        private List<String> dates;
        private List<Long> readCounts;
    }

    /**
     * 作品统计DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkStatsDTO {
        private Long id;
        private String title;
        private Long readCount;
        private Long likeCount;
        private Long commentCount;
        private Long favoriteCount;
    }
}