package com.fanfaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Interaction;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.InteractionMapper;
import com.fanfaction.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl extends ServiceImpl<InteractionMapper, Interaction> implements InteractionService {

    private static final int TYPE_LIKE = 1;
    private static final int TYPE_FAVORITE = 2;

    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public void toggleLike(Long userId, Long articleId) {
        toggleInteraction(userId, articleId, TYPE_LIKE);
    }

    @Override
    @Transactional
    public void toggleFavorite(Long userId, Long articleId) {
        toggleInteraction(userId, articleId, TYPE_FAVORITE);
    }

    @Override
    public boolean isLiked(Long userId, Long articleId) {
        return existsInteraction(userId, articleId, TYPE_LIKE);
    }

    @Override
    public boolean isFavorited(Long userId, Long articleId) {
        return existsInteraction(userId, articleId, TYPE_FAVORITE);
    }

    private void toggleInteraction(Long userId, Long articleId, int type) {
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interaction::getUserId, userId)
                .eq(Interaction::getArticleId, articleId)
                .eq(Interaction::getType, type);
        Interaction existing = getOne(wrapper);

        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        if (existing != null) {
            removeById(existing.getId());
            // 更新文章计数
            if (type == TYPE_LIKE) {
                article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
            } else if (type == TYPE_FAVORITE) {
                article.setFavoriteCount(Math.max(0, article.getFavoriteCount() - 1));
            }
        } else {
            Interaction interaction = new Interaction();
            interaction.setUserId(userId);
            interaction.setArticleId(articleId);
            interaction.setType(type);
            save(interaction);
            // 更新文章计数
            if (type == TYPE_LIKE) {
                article.setLikeCount(article.getLikeCount() + 1);
            } else if (type == TYPE_FAVORITE) {
                article.setFavoriteCount(article.getFavoriteCount() + 1);
            }
        }
        articleMapper.updateById(article);
    }

    private boolean existsInteraction(Long userId, Long articleId, int type) {
        LambdaQueryWrapper<Interaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interaction::getUserId, userId)
                .eq(Interaction::getArticleId, articleId)
                .eq(Interaction::getType, type);
        return count(wrapper) > 0;
    }
}
