package com.fanfaction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fanfaction.entity.ReadingHistory;
import com.fanfaction.vo.ArticleVO;

public interface ReadingHistoryService extends IService<ReadingHistory> {
    /**
     * 保存或更新阅读历史
     */
    void saveOrUpdateHistory(Long userId, Long articleId, Integer readDuration, 
                            Integer scrollPosition, Double readPercentage);
    
    /**
     * 分页查询用户的阅读历史（包含文章信息）
     */
    IPage<ArticleVO> getHistoryPage(Long userId, int pageNum, int pageSize);
}
