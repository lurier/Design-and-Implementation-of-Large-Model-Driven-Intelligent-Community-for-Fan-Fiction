-- 阅读历史记录表
CREATE TABLE IF NOT EXISTS reading_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    article_id BIGINT NOT NULL COMMENT '文章ID',
    read_duration INT DEFAULT 0 COMMENT '阅读时长（秒）',
    scroll_position INT DEFAULT 0 COMMENT '最后阅读位置（像素）',
    read_percentage DECIMAL(5,2) DEFAULT 0.00 COMMENT '阅读进度百分比',
    last_read_time DATETIME NOT NULL COMMENT '最后阅读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    
    INDEX idx_user_id (user_id),
    INDEX idx_article_id (article_id),
    INDEX idx_last_read_time (last_read_time),
    UNIQUE KEY uk_user_article (user_id, article_id) COMMENT '用户-文章唯一索引，用于更新记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读历史记录表';
