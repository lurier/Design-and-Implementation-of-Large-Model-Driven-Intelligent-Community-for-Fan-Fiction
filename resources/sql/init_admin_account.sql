-- 创建管理员账号
-- 用户名：admin
-- 密码：admin123 (BCrypt 加密)

USE `fanfaction`;

-- 检查 admin 用户是否已存在
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `phone`, `avatar`, `status`, `roles`, `creator_status`)
SELECT 'admin', '$2a$10$N.zdYrH0MhS9kQ8M5qK7ZOqKqKqKqKqKqKqKqKqKqKqKqKqKqKqKq', '系统管理员', 'admin@fanfaction.com', '', '', 1, 'ROLE_USER,ROLE_ADMIN', 2
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_user` WHERE `username` = 'admin'
);

-- 注意：上面的密码是示例，实际应该使用 BCrypt 加密
-- 密码 admin123 的 BCrypt 加密值类似于：$2a$10$4N8xVxY5k6PqGzQ8M5qK7Z...
-- 建议使用后端代码生成加密密码
