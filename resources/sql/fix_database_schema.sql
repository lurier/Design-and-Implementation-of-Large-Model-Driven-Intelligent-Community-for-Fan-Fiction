-- ================================================
-- FanFaction Database Schema Fix Script
-- Fixes: Missing columns and tables
-- ================================================

USE `fanfaction`;

-- ================================================
-- 1. Add 'deleted' column to tables that need it
-- ================================================

-- Add deleted column to creator_application
ALTER TABLE `creator_application` 
ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT 'Logical delete 0-No 1-Yes' AFTER `update_time`;

-- Add deleted column to reading_history
ALTER TABLE `reading_history` 
ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT 'Logical delete 0-No 1-Yes' AFTER `read_duration`;

-- Add deleted column to user_feedback
ALTER TABLE `user_feedback` 
ADD COLUMN `deleted` TINYINT DEFAULT 0 COMMENT 'Logical delete 0-No 1-Yes' AFTER `update_time`;

-- ================================================
-- 2. Add 'sentiment_score' column to comment table
-- ================================================

ALTER TABLE `comment` 
ADD COLUMN `sentiment_score` DECIMAL(5,2) DEFAULT NULL COMMENT 'Sentiment analysis score' AFTER `status`;

-- ================================================
-- 3. Create bookmark table
-- ================================================

DROP TABLE IF EXISTS `bookmark`;

CREATE TABLE `bookmark` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `article_id` BIGINT NOT NULL,
    `position` INT DEFAULT 0,
    `note` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 4. Create reading_progress table
-- ================================================

DROP TABLE IF EXISTS `reading_progress`;

CREATE TABLE `reading_progress` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `article_id` BIGINT NOT NULL,
    `scroll_position` INT DEFAULT 0,
    `read_percentage` DECIMAL(5,2) DEFAULT 0,
    `last_read_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 5. Insert test bookmark data
-- ================================================

INSERT INTO `bookmark` (`user_id`, `article_id`, `position`, `note`) VALUES
(2, 1, 100, 'First meeting scene'),
(2, 3, 250, 'Touching parting scene'),
(2, 5, 180, 'Luffy is so cool!'),
(2, 10, 320, 'Confession scene'),
(2, 19, 150, 'Proposal'),
(2, 20, 200, 'Wedding ending');

-- ================================================
-- 6. Insert test reading progress data
-- ================================================

INSERT INTO `reading_progress` (`user_id`, `article_id`, `scroll_position`, `read_percentage`) VALUES
(2, 1, 500, 75.50),
(2, 2, 300, 50.00),
(2, 3, 800, 90.25),
(2, 4, 450, 60.75),
(2, 5, 600, 80.00),
(2, 6, 200, 40.50),
(2, 7, 350, 55.25),
(2, 8, 400, 65.00),
(2, 9, 550, 70.75),
(2, 10, 700, 85.50);

-- ================================================
-- Completion Message
-- ================================================
SELECT 'Database schema fix completed!' AS message;
SELECT 'Tables and columns have been added successfully!' AS status;
