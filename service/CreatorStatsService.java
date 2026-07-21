package com.fanfaction.service;

import com.fanfaction.dto.CreatorStatsDTO;

/**
 * 创作者统计服务接口
 */
public interface CreatorStatsService {

    /**
     * 获取创作者统计数据
     * @param authorId 作者ID
     * @return 统计数据
     */
    CreatorStatsDTO getCreatorStats(Long authorId);

    /**
     * 获取创作者统计数据（指定日期范围）
     * @param authorId 作者ID
     * @param dateRange 日期范围 WEEK/MONTH
     * @return 统计数据
     */
    CreatorStatsDTO getCreatorStats(Long authorId, String dateRange);
}