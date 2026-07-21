package com.fanfaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfaction.entity.Bookmark;
import com.fanfaction.entity.ReadingProgress;
import com.fanfaction.mapper.BookmarkMapper;
import com.fanfaction.mapper.ReadingProgressMapper;
import com.fanfaction.service.ReadingHistoryService;
import com.fanfaction.service.ReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingServiceImpl implements ReadingService {

    private final ReadingProgressMapper readingProgressMapper;
    private final BookmarkMapper bookmarkMapper;
    private final ReadingHistoryService readingHistoryService;

    @Override
    @Transactional
    public void saveReadingProgress(Long userId, Long articleId, Integer scrollPosition, Double readPercentage) {
        LambdaQueryWrapper<ReadingProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingProgress::getUserId, userId)
                .eq(ReadingProgress::getArticleId, articleId);
        
        ReadingProgress progress = readingProgressMapper.selectOne(wrapper);
        
        if (progress == null) {
            progress = new ReadingProgress();
            progress.setUserId(userId);
            progress.setArticleId(articleId);
            progress.setScrollPosition(scrollPosition);
            progress.setReadPercentage(readPercentage);
            progress.setLastReadTime(LocalDateTime.now());
            readingProgressMapper.insert(progress);
        } else {
            progress.setScrollPosition(scrollPosition);
            progress.setReadPercentage(readPercentage);
            progress.setLastReadTime(LocalDateTime.now());
            readingProgressMapper.updateById(progress);
        }
        
        // 同时保存阅读历史记录（readDuration暂时为0，后续可以计算）
        readingHistoryService.saveOrUpdateHistory(userId, articleId, 0, scrollPosition, readPercentage);
    }

    @Override
    public ReadingProgress getReadingProgress(Long userId, Long articleId) {
        LambdaQueryWrapper<ReadingProgress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingProgress::getUserId, userId)
                .eq(ReadingProgress::getArticleId, articleId);
        return readingProgressMapper.selectOne(wrapper);
    }

    @Override
    public Bookmark addBookmark(Long userId, Long articleId, Integer position, String note) {
        Bookmark bookmark = new Bookmark();
        bookmark.setUserId(userId);
        bookmark.setArticleId(articleId);
        bookmark.setPosition(position);
        bookmark.setNote(note);
        bookmarkMapper.insert(bookmark);
        return bookmark;
    }

    @Override
    public void deleteBookmark(Long bookmarkId, Long userId) {
        Bookmark bookmark = bookmarkMapper.selectById(bookmarkId);
        if (bookmark != null && bookmark.getUserId().equals(userId)) {
            bookmarkMapper.deleteById(bookmarkId);
        }
    }

    @Override
    public List<Bookmark> getBookmarksByArticle(Long userId, Long articleId) {
        LambdaQueryWrapper<Bookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bookmark::getUserId, userId)
                .eq(Bookmark::getArticleId, articleId)
                .orderByAsc(Bookmark::getPosition);
        return bookmarkMapper.selectList(wrapper);
    }

    @Override
    public List<Bookmark> getUserBookmarks(Long userId) {
        LambdaQueryWrapper<Bookmark> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bookmark::getUserId, userId)
                .orderByDesc(Bookmark::getCreateTime);
        return bookmarkMapper.selectList(wrapper);
    }
}
