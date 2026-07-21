package com.fanfaction.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.ReadingHistory;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.ReadingHistoryMapper;
import com.fanfaction.service.ArticleService;
import com.fanfaction.service.ReadingHistoryService;
import com.fanfaction.service.UserService;
import com.fanfaction.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingHistoryServiceImpl extends ServiceImpl<ReadingHistoryMapper, ReadingHistory> 
        implements ReadingHistoryService {

    private final ArticleService articleService;
    private final UserService userService;

    @Override
    @Transactional
    public void saveOrUpdateHistory(Long userId, Long articleId, Integer readDuration, 
                                   Integer scrollPosition, Double readPercentage) {
        // 查询是否已存在该用户对该文章的阅读记录
        LambdaQueryWrapper<ReadingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingHistory::getUserId, userId)
                .eq(ReadingHistory::getArticleId, articleId)
                .eq(ReadingHistory::getDeleted, 0);
        
        ReadingHistory history = getOne(wrapper);
        
        if (history != null) {
            // 更新现有记录
            history.setReadDuration(readDuration != null ? readDuration : history.getReadDuration());
            history.setScrollPosition(scrollPosition != null ? scrollPosition : history.getScrollPosition());
            history.setReadPercentage(readPercentage != null ? readPercentage : history.getReadPercentage());
            history.setLastReadTime(LocalDateTime.now());
            updateById(history);
            log.info("更新阅读历史: userId={}, articleId={}", userId, articleId);
        } else {
            // 创建新记录
            history = new ReadingHistory();
            history.setUserId(userId);
            history.setArticleId(articleId);
            history.setReadDuration(readDuration != null ? readDuration : 0);
            history.setScrollPosition(scrollPosition != null ? scrollPosition : 0);
            history.setReadPercentage(readPercentage != null ? readPercentage : 0.0);
            history.setLastReadTime(LocalDateTime.now());
            history.setDeleted(0);
            save(history);
            log.info("创建阅读历史: userId={}, articleId={}", userId, articleId);
        }
    }

    @Override
    public IPage<ArticleVO> getHistoryPage(Long userId, int pageNum, int pageSize) {
        // 分页查询阅读历史
        Page<ReadingHistory> historyPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReadingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingHistory::getUserId, userId)
                .eq(ReadingHistory::getDeleted, 0)
                .orderByDesc(ReadingHistory::getLastReadTime);
        
        IPage<ReadingHistory> historyResult = page(historyPage, wrapper);
        
        // 转换为包含文章信息的VO
        IPage<ArticleVO> voPage = new Page<>();
        BeanUtil.copyProperties(historyResult, voPage, "records");
        
        List<ArticleVO> articleVOList = historyResult.getRecords().stream()
                .map(history -> {
                    Article article = articleService.getById(history.getArticleId());
                    if (article != null && article.getDeleted() == 0) {
                        ArticleVO vo = convertToArticleVO(article);
                        // 附加阅读历史信息
                        vo.setReadPercentage(history.getReadPercentage());
                        vo.setLastReadTime(history.getLastReadTime());
                        return vo;
                    }
                    return null;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
        
        voPage.setRecords(articleVOList);
        return voPage;
    }

    private ArticleVO convertToArticleVO(Article article) {
        ArticleVO vo = new ArticleVO();
        BeanUtil.copyProperties(article, vo);
        User author = userService.getById(article.getAuthorId());
        if (author != null) {
            vo.setAuthorName(author.getUsername());
            vo.setAuthorNickname(author.getNickname());
        }
        return vo;
    }
}
