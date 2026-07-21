package com.fanfaction.annotation;

import java.lang.annotation.*;

/**
 * 接口幂等性注解
 * 用于标记需要防止重复提交的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * Token在请求头中的名称
     */
    String headerName() default "Idempotent-Token";

    /**
     * Token过期时间（秒）
     */
    long expireTime() default 300; // 默认5分钟

    /**
     * 提示信息
     */
    String message() default "请勿重复提交，请稍后再试";
}