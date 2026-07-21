package com.fanfaction.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfaction.common.Result;
import com.fanfaction.entity.Article;
import com.fanfaction.mapper.ArticleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 分类和标签 Controller
 */
@Tag(name = "分类和标签")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final ArticleMapper articleMapper;

    /**
     * 获取作品分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取作品分类列表")
    public Result<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = new ArrayList<>();

        categories.add(createItem(1L, "同人小说"));
        categories.add(createItem(2L, "原创文学"));
        categories.add(createItem(3L, "书评影评"));
        categories.add(createItem(4L, "轻小说"));
        categories.add(createItem(5L, "短篇故事"));
        categories.add(createItem(6L, "连载系列"));
        categories.add(createItem(7L, "诗歌散文"));
        categories.add(createItem(8L, "其他"));

        return Result.success(categories);
    }

    /**
     * 获取 CP 标签列表（合并预设标签 + 数据库中已使用的标签）
     */
    @GetMapping("/cp-tags")
    @Operation(summary = "获取 CP 标签列表")
    public Result<List<Map<String, Object>>> getCpTags() {
        // 使用 LinkedHashSet 保持顺序并去重
        Set<String> nameSet = new LinkedHashSet<>();

        // 1. 预设标签
        for (String preset : PRESET_CP_TAGS) {
            nameSet.add(preset);
        }

        // 2. 从 article 表中提取所有已使用的标签
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Article::getTags)
                .isNotNull(Article::getTags)
                .ne(Article::getTags, "")
                .eq(Article::getDeleted, 0);
        List<Article> articles = articleMapper.selectList(wrapper);
        for (Article article : articles) {
            if (StrUtil.isNotBlank(article.getTags())) {
                String[] parts = article.getTags().split("[,，、]");
                for (String part : parts) {
                    String tag = part.trim();
                    if (StrUtil.isNotBlank(tag)) {
                        nameSet.add(tag);
                    }
                }
            }
        }

        // 3. 转换为前端需要的格式（id 和 name 都用标签名）
        List<Map<String, Object>> result = new ArrayList<>();
        long id = 1;
        for (String name : nameSet) {
            result.add(createItem(id++, name));
        }

        return Result.success(result);
    }

    private static final String[] PRESET_CP_TAGS = {
        "热门CP", "冷门CP", "BG", "BL", "GL",
        "原作向", "AU", "现代设定", "古代设定",
        "校园", "职场", "星际", "奇幻",
        "温馨", "虐向"
    };

    private Map<String, Object> createItem(Long id, String name) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", id);
        item.put("name", name);
        return item;
    }
}
