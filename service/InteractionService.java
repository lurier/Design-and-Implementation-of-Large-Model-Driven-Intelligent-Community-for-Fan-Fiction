package com.fanfaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fanfaction.entity.Interaction;

public interface InteractionService extends IService<Interaction> {
    void toggleLike(Long userId, Long articleId);
    void toggleFavorite(Long userId, Long articleId);
    boolean isLiked(Long userId, Long articleId);
    boolean isFavorited(Long userId, Long articleId);
}
