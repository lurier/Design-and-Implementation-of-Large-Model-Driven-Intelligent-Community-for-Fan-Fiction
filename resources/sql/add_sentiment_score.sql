-- 为评论表添加情感分数字段
ALTER TABLE comment ADD COLUMN sentiment_score DOUBLE COMMENT '情感分值：-1.0(消极) 到 1.0(积极)';

-- 为已有评论初始化情感分值（默认为0，表示中性）
UPDATE comment SET sentiment_score = 0.0 WHERE sentiment_score IS NULL;
