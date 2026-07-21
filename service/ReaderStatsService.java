package com.fanfaction.service;

import com.fanfaction.vo.ReaderStatsVO;

/**
 * 读者统计服务接口
 */
public interface ReaderStatsService {
    
    /**
     * 获取用户阅读数据统计
     * @param userId 用户 ID
     * @return 统计数据
     */
    ReaderStatsVO getUserStats(Long userId);
}
