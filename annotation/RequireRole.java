package com.fanfaction.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 0 = 普通读者, 1 = 创作者, 2 = 系统管理员
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /** 所需的最小角色值（默认0=任何登录用户） */
    int value() default 0;
}
