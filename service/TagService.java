package com.fanfaction.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fanfaction.entity.Tag;

import java.util.Set;

public interface TagService extends IService<Tag> {

    IPage<Tag> getTagPage(int pageNum, int pageSize, String keyword);

    void addTag(Tag tag);

    void updateTag(Tag tag);

    void deleteTag(Long id);

    void toggleStatus(Long id);

    /**
     * 获取所有禁用状态（status=0）的标签名称集合
     * 用于文章标签的负向过滤：只移除明确禁用的标签，保留所有未知标签
     */
    Set<String> getDisabledTagNames();

    /**
     * 自动入库：将标签名字符串中所有未知标签写入 tags 字典表
     * 已存在的跳过，不存在的以 status=1 自动创建
     */
    void autoRegisterTags(String tagsString);

    /**
     * 全量同步：扫描 article 表中所有历史文章的标签，写入 tags 字典表
     * 返回新注册的标签数量
     */
    int syncAllExistingTags();
}
