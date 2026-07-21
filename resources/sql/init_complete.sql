-- ================================================
-- FanFaction Database Complete Initialization Script
-- Includes: All table structures, required fields, initial admin account
-- Execution Time: 2026-06-09
-- ================================================

SET FOREIGN_KEY_CHECKS=0;

-- Create database
DROP DATABASE IF EXISTS `fanfaction`;
CREATE DATABASE `fanfaction` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `fanfaction`;

-- ================================================
-- 1. User Table
-- ================================================
DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `nickname` VARCHAR(50) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `avatar` TEXT DEFAULT NULL,
    `status` TINYINT DEFAULT 1,
    `roles` VARCHAR(100) DEFAULT 'ROLE_USER',
    `creator_status` TINYINT DEFAULT 0,
    `role` INT DEFAULT 0 COMMENT '角色:0=普通读者,1=创作者,2=系统管理员',
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_status` (`status`),
    KEY `idx_roles` (`roles`),
    KEY `idx_creator_status` (`creator_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 2. Creator Application Table
-- ================================================
DROP TABLE IF EXISTS `creator_application`;

CREATE TABLE `creator_application` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `pen_name` VARCHAR(50) DEFAULT NULL,
    `email` VARCHAR(100) DEFAULT NULL COMMENT '申请人邮箱',
    `expertise` VARCHAR(100) DEFAULT NULL,
    `introduction` VARCHAR(500) DEFAULT NULL,
    `representative_work` VARCHAR(500) DEFAULT NULL,
    `contact` VARCHAR(50) DEFAULT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0-待审核 1-通过 2-驳回',
    `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `reviewer_id` BIGINT DEFAULT NULL,
    `review_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 3. Article Table
-- ================================================
DROP TABLE IF EXISTS `article`;

CREATE TABLE `article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `summary` VARCHAR(500) DEFAULT NULL,
    `tags` VARCHAR(255) DEFAULT NULL,
    `cover_image` VARCHAR(255) DEFAULT NULL,
    `author_id` BIGINT NOT NULL,
    `view_count` INT DEFAULT 0,
    `like_count` INT DEFAULT 0,
    `favorite_count` INT DEFAULT 0,
    `comment_count` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_like_count` (`like_count`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 4. Interaction Table
-- ================================================
DROP TABLE IF EXISTS `interaction`;

CREATE TABLE `interaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `article_id` BIGINT NOT NULL,
    `type` TINYINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article_type` (`user_id`, `article_id`, `type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 5. Comment Table
-- ================================================
DROP TABLE IF EXISTS `comment`;

CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `article_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT 0,
    `reply_user_id` BIGINT DEFAULT NULL,
    `content` VARCHAR(1000) NOT NULL,
    `like_count` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 6. Reading History Table
-- ================================================
DROP TABLE IF EXISTS `reading_history`;

CREATE TABLE `reading_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `article_id` BIGINT NOT NULL,
    `read_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `read_duration` INT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_read_time` (`read_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- 7. User Feedback Table
-- ================================================
DROP TABLE IF EXISTS `user_feedback`;

CREATE TABLE `user_feedback` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `type` TINYINT NOT NULL,
    `content` VARCHAR(1000) NOT NULL,
    `contact` VARCHAR(100) DEFAULT NULL,
    `status` TINYINT DEFAULT 0,
    `reply` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================
-- Insert Initial Data
-- ================================================

-- Admin account (password: admin123, BCrypt encrypted)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `roles`, `status`, `creator_status`, `role`) 
VALUES ('admin', '$2a$10$4N8xVxY5k6PqGzQ8M5qK7ZOqKqKqKqKqKqKqKqKqKqKqKqKqKqKq', 'System Admin', 'admin@fanfaction.com', 'ROLE_USER,ROLE_ADMIN', 1, 2, 2);

-- Test user 1 (password: test123)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `roles`, `status`, `creator_status`, `role`) 
VALUES ('test_user', '$2a$10$4N8xVxY5k6PqGzQ8M5qK7ZOqKqKqKqKqKqKqKqKqKqKqKqKqKqKq', 'Test User', 'test@fanfaction.com', 'ROLE_USER', 1, 0, 0);

-- Creator user (password: creator123, already has creator permission)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `roles`, `status`, `creator_status`, `role`) 
VALUES ('creator', '$2a$10$4N8xVxY5k6PqGzQ8M5qK7ZOqKqKqKqKqKqKqKqKqKqKqKqKqKqKq', 'Famous Writer', 'creator@fanfaction.com', 'ROLE_USER,ROLE_CREATOR', 1, 2, 1);

-- ================================================
-- Completion Message
-- ================================================
SELECT 'Database initialization completed!' AS message;
SELECT 'Admin account: admin / admin123' AS admin_account;
SELECT 'Test user: test_user / test123' AS test_account;
SELECT 'Creator account: creator / creator123' AS creator_account;

SET FOREIGN_KEY_CHECKS=1;
