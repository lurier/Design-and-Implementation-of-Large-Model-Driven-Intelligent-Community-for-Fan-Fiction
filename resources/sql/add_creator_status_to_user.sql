-- Add creator_status column to user table

USE `fanfaction`;

-- Add creator_status column
-- 0: Not Applied, 1: Pending, 2: Approved, 3: Rejected
ALTER TABLE `sys_user` 
ADD COLUMN `creator_status` TINYINT DEFAULT 0 COMMENT 'Creator status: 0-Not Applied 1-Pending 2-Approved 3-Rejected' AFTER `roles`;

-- Set default status for existing users
UPDATE `sys_user` SET `creator_status` = 0 WHERE `creator_status` IS NULL;

-- Add index for optimization
CREATE INDEX `idx_creator_status` ON `sys_user` (`creator_status`);
