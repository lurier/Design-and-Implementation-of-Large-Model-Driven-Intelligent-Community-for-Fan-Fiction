package com.fanfaction.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfaction.entity.Article;
import com.fanfaction.entity.Tag;
import com.fanfaction.mapper.ArticleMapper;
import com.fanfaction.mapper.TagMapper;
import com.fanfaction.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public IPage<Tag> getTagPage(int pageNum, int pageSize, String keyword) {
        Page<Tag> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getDeleted, 0);

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Tag::getName, keyword);
        }

        wrapper.orderByAsc(Tag::getSortOrder)
               .orderByDesc(Tag::getCreateTime);

        return page(page, wrapper);
    }

    @Override
    @Transactional
    public void addTag(Tag tag) {
        // 检查标签名是否已存在
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, tag.getName())
               .eq(Tag::getDeleted, 0);
        if (count(wrapper) > 0) {
            throw new RuntimeException("标签名已存在: " + tag.getName());
        }

        tag.setStatus(1); // 默认启用
        save(tag);
        log.info("添加标签成功: {}", tag.getName());
    }

    @Override
    @Transactional
    public void updateTag(Tag tag) {
        Tag existing = getById(tag.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new RuntimeException("标签不存在");
        }

        // 检查名称唯一性
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getName, tag.getName())
               .eq(Tag::getDeleted, 0)
               .ne(Tag::getId, tag.getId());
        if (count(wrapper) > 0) {
            throw new RuntimeException("标签名已被占用: " + tag.getName());
        }

        existing.setName(tag.getName());
        existing.setCategory(tag.getCategory());
        existing.setColor(tag.getColor());
        existing.setSortOrder(tag.getSortOrder());
        updateById(existing);
        log.info("更新标签成功: ID={}, name={}", tag.getId(), tag.getName());
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = getById(id);
        if (tag == null || tag.getDeleted() == 1) {
            throw new RuntimeException("标签不存在");
        }
        tag.setDeleted(1);
        updateById(tag);
        log.info("删除标签成功: ID={}, name={}", id, tag.getName());
    }

    @Override
    @Transactional
    public void toggleStatus(Long id) {
        Tag tag = getById(id);
        if (tag == null || tag.getDeleted() == 1) {
            throw new RuntimeException("标签不存在");
        }
        int newStatus = tag.getStatus() == 1 ? 0 : 1;
        tag.setStatus(newStatus);
        updateById(tag);
        log.info("切换标签状态: ID={}, name={}, newStatus={}", id, tag.getName(), newStatus);

        // 同步关联文章：禁用标签 → 下架文章，启用标签 → 恢复文章
        updateArticlesByTagStatus(tag.getName(), newStatus);
    }

    /**
     * 根据标签状态变化，同步处理关联文章的显示状态
     * 禁用(status=0)：将该标签的所有已发布文章(status=1)设置为已下架(status=2)
     * 启用(status=1)：将该标签的所有已下架文章(status=2)恢复为已发布(status=1)
     */
    private void updateArticlesByTagStatus(String tagName, int tagStatus) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .like(Article::getTags, tagName);

        if (tagStatus == 0) {
            // 禁用标签：把包含该标签的已发布文章改为已下架
            wrapper.eq(Article::getStatus, 1); // 只处理已发布状态的文章
            List<Article> articles = articleMapper.selectList(wrapper);
            for (Article article : articles) {
                article.setStatus(2); // 已下架
                articleMapper.updateById(article);
            }
            log.info("标签 [{}] 被禁用，下架了 {} 篇关联文章", tagName, articles.size());
        } else {
            // 启用标签：把因该标签被下架的文章恢复为已发布
            wrapper.eq(Article::getStatus, 2); // 只处理已下架状态的文章
            List<Article> articles = articleMapper.selectList(wrapper);
            for (Article article : articles) {
                article.setStatus(1); // 恢复已发布
                articleMapper.updateById(article);
            }
            log.info("标签 [{}] 被启用，恢复了 {} 篇关联文章", tagName, articles.size());
        }
    }

    @Override
    public Set<String> getDisabledTagNames() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getDeleted, 0)
               .eq(Tag::getStatus, 0)
               .select(Tag::getName);

        List<Tag> disabledTags = list(wrapper);
        return disabledTags.stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void autoRegisterTags(String tagsString) {
        if (StrUtil.isBlank(tagsString)) {
            return;
        }

        // 获取 tags 表中已存在的所有标签名
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getDeleted, 0)
               .select(Tag::getName);
        Set<String> existingNames = list(wrapper).stream()
                .map(Tag::getName)
                .collect(Collectors.toSet());

        // 拆分传入的标签字符串，找出不在字典表中的新标签
        int maxSortOrder = getMaxSortOrder();
        int newCount = 0;

        String[] parts = tagsString.split("[,，、]");
        for (String part : parts) {
            String name = part.trim();
            if (StrUtil.isBlank(name) || existingNames.contains(name)) {
                continue;
            }

            Tag newTag = new Tag();
            newTag.setName(name);
            newTag.setCategory("theme");
            newTag.setColor("#409eff");
            newTag.setSortOrder(++maxSortOrder);
            newTag.setStatus(1); // 默认启用
            save(newTag);

            existingNames.add(name); // 避免同批次重复插入
            newCount++;
            log.info("自动注册新标签: {}", name);
        }

        if (newCount > 0) {
            log.info("自动入库完成，新增 {} 个标签", newCount);
        }
    }

    /**
     * 获取当前最大排序值，用于新标签默认排序
     */
    private int getMaxSortOrder() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getDeleted, 0)
               .orderByDesc(Tag::getSortOrder)
               .last("LIMIT 1");
        Tag top = getOne(wrapper, false);
        return top != null && top.getSortOrder() != null ? top.getSortOrder() : 0;
    }

    @Override
    @Transactional
    public int syncAllExistingTags() {
        // 查询所有未删除文章中的非空标签
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Article::getTags)
               .eq(Article::getDeleted, 0)
               .isNotNull(Article::getTags)
               .ne(Article::getTags, "");

        List<Article> articles = articleMapper.selectList(wrapper);

        // 收集所有唯一标签名
        Set<String> allTags = new HashSet<>();
        for (Article article : articles) {
            if (StrUtil.isNotBlank(article.getTags())) {
                for (String part : article.getTags().split("[,，、]")) {
                    String tag = part.trim();
                    if (StrUtil.isNotBlank(tag)) {
                        allTags.add(tag);
                    }
                }
            }
        }

        if (allTags.isEmpty()) {
            log.info("全量同步：没有需要同步的标签");
            return 0;
        }

        // 构造标签字符串，复用 autoRegisterTags 逻辑
        String tagsString = String.join(",", allTags);
        log.info("全量同步：article 表中共发现 {} 个唯一标签，开始入库", allTags.size());
        autoRegisterTags(tagsString);

        // 计算真正新增的数量
        LambdaQueryWrapper<Tag> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Tag::getDeleted, 0);
        long total = count(countWrapper);

        log.info("全量同步完成，tags 字典表共 {} 条记录", total);
        return allTags.size();
    }
}
