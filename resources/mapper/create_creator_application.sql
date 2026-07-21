-- 创作者申请表
CREATE TABLE IF NOT EXISTS `creator_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `pen_name` VARCHAR(50) NOT NULL COMMENT '笔名',
  `expertise` VARCHAR(200) NOT NULL COMMENT '擅长领域（逗号分隔）',
  `introduction` TEXT NOT NULL COMMENT '个人简介',
  `representative_work` VARCHAR(500) DEFAULT NULL COMMENT '代表作品',
  `contact` VARCHAR(100) NOT NULL COMMENT '联系方式',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态：0-审核中 1-通过 2-拒绝',
  `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人 ID',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='创作者申请表';
