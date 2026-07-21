package com.fanfaction.service;

import com.fanfaction.entity.Bookmark;
import com.fanfaction.entity.ReadingProgress;

import java.util.List;

public interface ReadingService {
    /**
     * 保存阅读进度
     */
    void saveReadingProgress(Long userId, Long articleId, Integer scrollPosition, Double readPercentage);
    
    /**
     * 获取阅读进度
     */
    ReadingProgress getReadingProgress(Long userId, Long articleId);
    
    /**
     * 添加书签
     */
    Bookmark addBookmark(Long userId, Long articleId, Integer position, String note);
    
    /**
     * 删除书签
     */
    void deleteBookmark(Long bookmarkId, Long userId);
    
    /**
     * 获取文章的所有书签
     */
    List<Bookmark> getBookmarksByArticle(Long userId, Long articleId);
    
    /**
     * 获取用户的所有书签
     */
    List<Bookmark> getUserBookmarks(Long userId);
}
