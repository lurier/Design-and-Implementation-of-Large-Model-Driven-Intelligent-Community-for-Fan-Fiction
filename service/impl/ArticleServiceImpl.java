package com.fanfaction.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfaction.common.BusinessException;
import com.fanfaction.dto.ArticleDTO;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Interaction;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.service.AiService;
import com.fanfaction.service.ArticleService;
import com.fanfaction.service.ContentCheckService;
import com.fanfaction.service.InteractionService;
import com.fanfaction.service.TagService;
import com.fanfaction.service.UserService;
import com.fanfaction.vo.ArticleDetailVO;
import com.fanfaction.vo.ArticleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final UserService userService;
    private final InteractionService interactionService;
    private final ContentCheckService contentCheckService;
    private final AiService aiService;
    private final TagService tagService;

    // 状态映射：字符串 -> 数字
    private static final Map<String, Integer> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("DRAFT", 0);      // 草稿
        STATUS_MAP.put("PENDING", 2);    // 审核中
        STATUS_MAP.put("APPROVED", 1);   // 已发布
        STATUS_MAP.put("REJECTED", 3);   // 已驳回
    }

    @Override
    @Transactional
    public Long publishArticle(Long authorId, ArticleDTO articleDTO) {
        // 检查用户状态，封禁用户禁止发文
        checkUserBanned(authorId);

        // 敏感词校验
        checkSensitiveWords(articleDTO.getTitle(), articleDTO.getContent(), articleDTO.getSummary());

        Article article = new Article();
        // 手动复制属性，避免 BeanUtil 自动复制 status 字段导致类型转换问题
        article.setTitle(articleDTO.getTitle());
        article.setContent(articleDTO.getContent());
        article.setSummary(articleDTO.getSummary());
        article.setTags(articleDTO.getTags());
        article.setCoverImage(articleDTO.getCoverImage());
        article.setAuthorId(authorId);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setCommentCount(0);
        
        // 根据状态字符串设置状态值（字符串转整数）
        String statusStr = articleDTO.getStatus();
        if (StrUtil.isNotBlank(statusStr) && STATUS_MAP.containsKey(statusStr)) {
            article.setStatus(STATUS_MAP.get(statusStr));
        } else {
            article.setStatus(STATUS_MAP.get("PENDING")); // 默认审核中
        }
        article.setDeleted(0);

        // 自动入库：将新标签写入 tags 字典表
        tagService.autoRegisterTags(article.getTags());

        save(article);
        
        // 异步生成AI摘要和标签（不阻塞主流程）
        asyncGenerateAiContent(article.getId());
        
        return article.getId();
    }

    @Override
    public IPage<ArticleVO> getArticlePage(int pageNum, int pageSize, String sortBy, String keyword, String tag) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1);

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                    .or()
                    .like(Article::getSummary, keyword)
                    .or()
                    .like(Article::getContent, keyword)
                    .or()
                    .like(Article::getTags, keyword));
        }
        
        // 按标签筛选
        if (StrUtil.isNotBlank(tag)) {
            wrapper.like(Article::getTags, tag);
        }

        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Article::getLikeCount, Article::getViewCount, Article::getCreateTime);
        } else {
            wrapper.orderByDesc(Article::getCreateTime);
        }

        IPage<Article> articlePage = page(page, wrapper);
        return articlePage.convert(article -> convertToArticleVO(article, null));
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long articleId, Long currentUserId) {
        Article article = getById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new RuntimeException("文章不存在");
        }

        article.setViewCount(article.getViewCount() + 1);
        updateById(article);

        return convertToArticleDetailVO(article, currentUserId);
    }

    @Override
    @Transactional
    public void updateArticle(Long articleId, Long authorId, ArticleDTO articleDTO) {
        // 检查用户状态
        checkUserBanned(authorId);

        Article article = getById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权修改此文章");
        }

        // 敏感词校验
        checkSensitiveWords(articleDTO.getTitle(), articleDTO.getContent(), articleDTO.getSummary());

        // 排除 status 字段，避免类型转换问题
        BeanUtil.copyProperties(articleDTO, article, "id", "authorId", "viewCount", "likeCount", "favoriteCount", "commentCount", "status");
        
        // 设置状态（字符串转整数）
        String statusStr = articleDTO.getStatus();
        if (StrUtil.isNotBlank(statusStr) && STATUS_MAP.containsKey(statusStr)) {
            article.setStatus(STATUS_MAP.get(statusStr));
        }
        
        // 自动入库：将新标签写入 tags 字典表
        tagService.autoRegisterTags(article.getTags());

        updateById(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long articleId, Long authorId) {
        // 检查用户状态
        checkUserBanned(authorId);

        Article article = getById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new RuntimeException("文章不存在");
        }
        if (!article.getAuthorId().equals(authorId)) {
            throw new RuntimeException("无权删除此文章");
        }

        article.setDeleted(1);
        updateById(article);
    }

    @Override
    public List<Article> getUserFavoriteArticles(Long userId) {
        LambdaQueryWrapper<Interaction> interactionWrapper = new LambdaQueryWrapper<>();
        interactionWrapper.eq(Interaction::getUserId, userId)
                .eq(Interaction::getType, 2);
        List<Long> articleIds = interactionService.list(interactionWrapper)
                .stream()
                .map(Interaction::getArticleId)
                .collect(Collectors.toList());

        if (articleIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.in(Article::getId, articleIds)
                .eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getCreateTime);

        return list(articleWrapper);
    }

    @Override
    public List<Article> getUserLikedArticles(Long userId) {
        LambdaQueryWrapper<Interaction> interactionWrapper = new LambdaQueryWrapper<>();
        interactionWrapper.eq(Interaction::getUserId, userId)
                .eq(Interaction::getType, 1);
        List<Long> articleIds = interactionService.list(interactionWrapper)
                .stream()
                .map(Interaction::getArticleId)
                .collect(Collectors.toList());

        if (articleIds.isEmpty()) {
            return List.of();
        }

        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.in(Article::getId, articleIds)
                .eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getCreateTime);

        return list(articleWrapper);
    }

    @Override
    public List<Article> getUserPublishedArticles(Long userId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getAuthorId, userId)
                .eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getCreateTime);

        return list(wrapper);
    }

    @Override
    public IPage<Article> getUserArticles(Long userId, int page, int size, String status) {
        Page<Article> pageQuery = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getAuthorId, userId)
                .eq(Article::getDeleted, 0)
                .orderByDesc(Article::getCreateTime);

        // 状态筛选
        if (StrUtil.isNotBlank(status) && STATUS_MAP.containsKey(status)) {
            wrapper.eq(Article::getStatus, STATUS_MAP.get(status));
        }

        return page(pageQuery, wrapper);
    }

    @Override
    @Transactional
    public void reviewArticle(Long articleId, String status, String comment, Long reviewerId) {
        Article article = getById(articleId);
        if (article == null || article.getDeleted() == 1) {
            throw new RuntimeException("文章不存在");
        }

        if (!STATUS_MAP.containsKey(status)) {
            throw new RuntimeException("无效的状态值");
        }

        article.setStatus(STATUS_MAP.get(status));
        article.setReviewComment(comment);
        article.setReviewerId(reviewerId);
        updateById(article);
        
        log.info("文章审核完成，ID: {}, 状态: {}, 审核人: {}", articleId, status, reviewerId);
    }

    @Override
    public IPage<Article> getPendingArticles(int page, int size) {
        Page<Article> pageQuery = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
                .eq(Article::getStatus, STATUS_MAP.get("PENDING"))
                .orderByDesc(Article::getCreateTime);

        return page(pageQuery, wrapper);
    }

    @Override
    public IPage<Article> getAllArticles(int page, int size, String status) {
        Page<Article> pageQuery = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
                .orderByDesc(Article::getCreateTime);

        // 状态筛选
        if (StrUtil.isNotBlank(status) && STATUS_MAP.containsKey(status)) {
            wrapper.eq(Article::getStatus, STATUS_MAP.get(status));
        }

        return page(pageQuery, wrapper);
    }

    @Override
    public List<Article> getHotArticles(int limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
                .eq(Article::getStatus, 1)
                .orderByDesc(Article::getViewCount)
                .orderByDesc(Article::getLikeCount)
                .last("LIMIT " + limit);
        
        return list(wrapper);
    }

    private ArticleVO convertToArticleVO(Article article, Long currentUserId) {
        ArticleVO vo = new ArticleVO();
        BeanUtil.copyProperties(article, vo);
        User author = userService.getById(article.getAuthorId());
        if (author != null) {
            vo.setAuthorName(author.getUsername());
            vo.setAuthorNickname(author.getNickname());
        }
        // 过滤被禁用的标签
        vo.setTags(filterDisabledTags(article.getTags()));
        return vo;
    }

    private ArticleDetailVO convertToArticleDetailVO(Article article, Long currentUserId) {
        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtil.copyProperties(article, vo);
        User author = userService.getById(article.getAuthorId());
        if (author != null) {
            vo.setAuthorName(author.getUsername());
            vo.setAuthorNickname(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }
        if (currentUserId != null) {
            vo.setIsLiked(interactionService.isLiked(currentUserId, article.getId()));
            vo.setIsFavorited(interactionService.isFavorited(currentUserId, article.getId()));
        } else {
            vo.setIsLiked(false);
            vo.setIsFavorited(false);
        }
        
        // 计算阅读时长（每分钟300字，向上取整）
        if (StrUtil.isNotBlank(article.getContent())) {
            int wordCount = article.getContent().length();
            int readTime = (int) Math.ceil(wordCount / 300.0);
            vo.setReadTime(readTime);
        } else {
            vo.setReadTime(0);
        }
        
        // 过滤被禁用的标签
        vo.setTags(filterDisabledTags(article.getTags()));
        return vo;
    }

    /**
     * 过滤文章标签中的禁用标签（负向过滤）
     * 只移除 tags 表中明确标记为 status=0 的标签，不在管理表中的标签一律保留
     */
    private String filterDisabledTags(String tags) {
        if (StrUtil.isBlank(tags)) {
            return tags;
        }

        Set<String> disabledTags = tagService.getDisabledTagNames();
        if (disabledTags.isEmpty()) {
            return tags;
        }

        return Arrays.stream(tags.split("[,，、]"))
                .map(String::trim)
                .filter(tag -> StrUtil.isNotBlank(tag) && !disabledTags.contains(tag))
                .collect(Collectors.joining(","));
    }

    /**
     * 检查文章标题、内容、摘要中是否包含敏感词
     */
    private void checkSensitiveWords(String title, String content, String summary) {
        if (StrUtil.isNotBlank(title) && contentCheckService.hasSensitiveWord(title)) {
            throw new BusinessException("文章标题包含敏感词，请修改后重新发布");
        }
        if (StrUtil.isNotBlank(content) && contentCheckService.hasSensitiveWord(content)) {
            throw new BusinessException("文章内容包含敏感词，请修改后重新发布");
        }
        if (StrUtil.isNotBlank(summary) && contentCheckService.hasSensitiveWord(summary)) {
            throw new BusinessException("文章摘要包含敏感词，请修改后重新发布");
        }
    }

    @Override
    @Async("aiTaskExecutor")
    public void asyncGenerateAiContent(Long articleId) {
        long taskStartTime = System.currentTimeMillis();
        
        // 记录线程信息，验证异步执行
        String threadName = Thread.currentThread().getName();
        log.info("========== 异步任务开始执行 ==========");
        log.info("当前线程: {}", threadName);
        log.info("文章ID: {}", articleId);
        
        try {
            log.info("开始异步生成文章AI内容，文章ID: {}", articleId);
            
            Article article = getById(articleId);
            if (article == null || article.getDeleted() == 1) {
                log.warn("文章不存在，跳过AI处理，文章ID: {}", articleId);
                return;
            }

            // 如果已有摘要和标签，则不再生成
            if (StrUtil.isNotBlank(article.getSummary()) && StrUtil.isNotBlank(article.getTags())) {
                log.info("文章已有摘要和标签，跳过AI处理，文章ID: {}", articleId);
                return;
            }

            String content = article.getContent();
            if (StrUtil.isBlank(content)) {
                log.warn("文章内容为空，跳过AI处理，文章ID: {}", articleId);
                return;
            }

            log.info("文章内容长度: {} 字符", content.length());

            // 生成摘要（如果没有）
            if (StrUtil.isBlank(article.getSummary())) {
                log.info("开始生成摘要...");
                long summaryStartTime = System.currentTimeMillis();
                String summary = aiService.generateSummary(content);
                long summaryEndTime = System.currentTimeMillis();
                
                if (StrUtil.isNotBlank(summary)) {
                    article.setSummary(summary);
                    log.info("✅ AI摘要生成成功，文章ID: {}, 耗时: {} ms", articleId, (summaryEndTime - summaryStartTime));
                } else {
                    log.warn("⚠️ AI摘要生成为空，文章ID: {}", articleId);
                }
            } else {
                log.info("文章已有摘要，跳过生成");
            }

            // 生成标签（如果没有）
            if (StrUtil.isBlank(article.getTags())) {
                log.info("开始生成标签...");
                long tagsStartTime = System.currentTimeMillis();
                List<String> tags = aiService.generateTags(content);
                long tagsEndTime = System.currentTimeMillis();
                
                if (tags != null && !tags.isEmpty()) {
                    // 将标签列表转换为逗号分隔的字符串
                    article.setTags(String.join(",", tags));
                    log.info("✅ AI标签生成成功，文章ID: {}, 标签数: {}, 耗时: {} ms", 
                            articleId, tags.size(), (tagsEndTime - tagsStartTime));
                    log.info("标签内容: {}", String.join(", ", tags));
                } else {
                    log.warn("⚠️ AI标签生成为空，文章ID: {}", articleId);
                }
            } else {
                log.info("文章已有标签，跳过生成");
            }

            // 更新文章
            log.info("正在更新文章记录...");
            updateById(article);
            
            long totalTime = System.currentTimeMillis() - taskStartTime;
            log.info("✅ 文章AI内容生成完成，文章ID: {}, 总耗时: {} ms", articleId, totalTime);
            log.info("========== 异步任务执行完成 ==========");
            
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - taskStartTime;
            log.error("❌ 异步生成文章AI内容失败，文章ID: {}, 耗时: {} ms, 错误: {}", 
                    articleId, totalTime, e.getMessage(), e);
            log.info("========== 异步任务执行失败 ==========");
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 检查用户是否被封禁，封禁用户禁止发文/编辑/删除
     */
    private void checkUserBanned(Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("您的账号已被封禁，无法进行操作");
        }
    }
}
