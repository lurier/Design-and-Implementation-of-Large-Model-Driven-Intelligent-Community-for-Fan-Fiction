package com.fanfaction.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfaction.entity.Tag;
import com.fanfaction.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动时自动同步 article 表中的标签到 tags 字典表
 * 幂等操作：已存在的标签不会重复插入
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TagSyncRunner implements ApplicationRunner {

    private final TagService tagService;

    @Override
    public void run(ApplicationArguments args) {
        // 如果 tags 表已有足够数据，跳过全量扫描（性能优化）
        long existingCount = tagService.count(new LambdaQueryWrapper<Tag>().eq(Tag::getDeleted, 0));
        if (existingCount > 100) {
            log.info("TagSyncRunner: tags 字典表已有 {} 条记录，跳过全量同步", existingCount);
            return;
        }

        log.info("TagSyncRunner: 开始全量同步 article 表中的历史标签...");
        try {
            int count = tagService.syncAllExistingTags();
            log.info("TagSyncRunner: 同步完成，article 中共发现 {} 个唯一标签", count);
        } catch (Exception e) {
            log.error("TagSyncRunner: 同步失败", e);
        }
    }
}
