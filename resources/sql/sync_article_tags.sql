-- 从 article 表提取所有唯一标签写入 tags 字典表
-- 用途：一次性迁移历史数据中的标签到管理表
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 -D fanfaction < sync_article_tags.sql

-- 创建临时存储过程处理逗号分隔的标签拆分
DROP PROCEDURE IF EXISTS sync_article_tags;
DELIMITER $$
CREATE PROCEDURE sync_article_tags()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE tag_list TEXT;
    DECLARE cur CURSOR FOR 
        SELECT tags FROM article 
        WHERE deleted = 0 AND tags IS NOT NULL AND tags != '';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    -- 用于收集唯一标签
    DROP TEMPORARY TABLE IF EXISTS tmp_all_tags;
    CREATE TEMPORARY TABLE tmp_all_tags (tag_name VARCHAR(100) UNIQUE);

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO tag_list;
        IF done = 1 THEN LEAVE read_loop; END IF;

        -- 拆分逗号分隔的标签
        SET @remaining = tag_list;
        WHILE CHAR_LENGTH(@remaining) > 0 DO
            SET @comma_pos = LOCATE(',', @remaining);
            IF @comma_pos > 0 THEN
                SET @tag = TRIM(SUBSTRING(@remaining, 1, @comma_pos - 1));
                SET @remaining = SUBSTRING(@remaining, @comma_pos + 1);
            ELSE
                SET @tag = TRIM(@remaining);
                SET @remaining = '';
            END IF;

            IF CHAR_LENGTH(@tag) > 0 THEN
                INSERT IGNORE INTO tmp_all_tags (tag_name) VALUES (@tag);
            END IF;
        END WHILE;
    END LOOP;
    CLOSE cur;

    -- 写入 tags 表（跳过已存在的）
    INSERT IGNORE INTO tags (name, category, color, sort_order, status, create_time, update_time)
    SELECT 
        t.tag_name,
        'theme' AS category,
        '#409eff' AS color,
        COALESCE((SELECT MAX(sort_order) FROM tags WHERE deleted = 0), 0) + ROW_NUMBER() OVER (ORDER BY t.tag_name) AS sort_order,
        1 AS status,
        NOW() AS create_time,
        NOW() AS update_time
    FROM tmp_all_tags t
    WHERE NOT EXISTS (
        SELECT 1 FROM tags WHERE name = t.tag_name AND deleted = 0
    );

    SELECT COUNT(*) AS migrated_count FROM tmp_all_tags;
    DROP TEMPORARY TABLE IF EXISTS tmp_all_tags;
END$$
DELIMITER ;

CALL sync_article_tags();
DROP PROCEDURE IF EXISTS sync_article_tags;
