-- 创建标签管理表
DROP TABLE IF EXISTS `tags`;

CREATE TABLE `tags` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `category` VARCHAR(50) DEFAULT 'theme' COMMENT '分类: work_type/theme/character/other',
    `color` VARCHAR(20) DEFAULT '#409eff' COMMENT '标签颜色',
    `sort_order` INT DEFAULT 0 COMMENT '排序值',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签管理表';

-- 初始化一些预设标签
INSERT INTO `tags` (`name`, `category`, `color`, `sort_order`, `status`) VALUES
('同人', 'theme', '#284139', 1, 1),
('耽美', 'theme', '#f56c6c', 2, 1),
('古风', 'theme', '#67c23a', 3, 1),
('穿越', 'theme', '#409eff', 4, 1),
('重生', 'theme', '#909399', 5, 1),
('修仙', 'theme', '#e6a23c', 6, 1),
('原创文学', 'work_type', '#b37feb', 1, 1),
('书评影评', 'work_type', '#73c0de', 2, 1),
('轻小说', 'work_type', '#5eb872', 3, 1),
('短篇', 'work_type', '#f5a623', 4, 1),
('热门CP', 'character', '#f56c6c', 1, 1),
('BG', 'character', '#409eff', 2, 1),
('BL', 'character', '#67c23a', 3, 1);
