USE `fanfaction`;

-- 添加审核相关字段
ALTER TABLE `article` 
ADD COLUMN `review_comment` TEXT DEFAULT NULL COMMENT '审核意见',
ADD COLUMN `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
ADD KEY `idx_reviewer_id` (`reviewer_id`);

-- 更新状态注释（可选，仅更新表注释不影响数据）
ALTER TABLE `article` MODIFY COLUMN `status` TINYINT DEFAULT 2 COMMENT '状态 0-草稿 1-已发布 2-审核中 3-已驳回';

-- 添加外键约束（可选）
ALTER TABLE `article` ADD CONSTRAINT `fk_reviewer_id` FOREIGN KEY (`reviewer_id`) REFERENCES `sys_user`(`id`);
