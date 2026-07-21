USE `fanfaction`;

-- 同步已有的评论数量到文章表
-- 创建临时表存储各文章的评论数
CREATE TEMPORARY TABLE IF NOT EXISTS temp_comment_counts (
    article_id BIGINT PRIMARY KEY,
    comment_count INT
);

-- 统计每篇文章的评论数量（只统计未删除、状态正常的评论）
INSERT INTO temp_comment_counts (article_id, comment_count)
SELECT article_id, COUNT(*) as comment_count
FROM comment
WHERE deleted = 0 AND status = 1
GROUP BY article_id;

-- 更新文章表的评论数字段
UPDATE article a
JOIN temp_comment_counts t ON a.id = t.article_id
SET a.comment_count = t.comment_count;

-- 对于没有评论的文章，确保comment_count为0
UPDATE article
SET comment_count = 0
WHERE comment_count IS NULL;

-- 删除临时表
DROP TEMPORARY TABLE IF EXISTS temp_comment_counts;

SELECT '评论数同步完成' AS result;
