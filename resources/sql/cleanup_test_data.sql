USE `fanfaction`;

-- Delete remaining test articles and related data by article IDs
-- 21: creator test article, 22: creator test 2, 24-27: random test, 28-31: 科幻/言情测试

-- Delete related reading_history
DELETE FROM `reading_history` WHERE article_id IN (21, 22, 24, 25, 26, 27, 28, 29, 30, 31);

-- Delete related bookmarks
DELETE FROM `bookmark` WHERE article_id IN (21, 22, 24, 25, 26, 27, 28, 29, 30, 31);

-- Delete related comments
DELETE FROM `comment` WHERE article_id IN (21, 22, 24, 25, 26, 27, 28, 29, 30, 31);

-- Delete related interactions
DELETE FROM `interaction` WHERE article_id IN (21, 22, 24, 25, 26, 27, 28, 29, 30, 31);

-- Delete the articles themselves
DELETE FROM `article` WHERE id IN (21, 22, 24, 25, 26, 27, 28, 29, 30, 31);

-- Reset auto-increment
ALTER TABLE `article` AUTO_INCREMENT = 1;

-- Verify
SELECT 'Remaining test data cleanup completed!' AS message;
SELECT id, title FROM article ORDER BY id;
