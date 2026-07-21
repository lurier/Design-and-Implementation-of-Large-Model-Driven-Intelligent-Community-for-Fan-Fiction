package com.fanfaction.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性Token工具类
 */
@Component
public class IdempotentTokenUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String IDEMPOTENT_TOKEN_PREFIX = "idempotent:token:";

    /**
     * 生成幂等性Token并存入Redis
     * @param expireTime 过期时间（秒）
     * @return Token字符串
     */
    public String createToken(long expireTime) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = IDEMPOTENT_TOKEN_PREFIX + token;
        
        // 存入Redis，设置过期时间
        redisTemplate.opsForValue().set(key, "1", expireTime, TimeUnit.SECONDS);
        
        return token;
    }

    /**
     * 验证Token并删除（原子操作）
     * @param token Token字符串
     * @return true-验证通过且删除成功，false-Token不存在或已失效
     */
    public boolean validateAndDeleteToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String key = IDEMPOTENT_TOKEN_PREFIX + token;
        
        // 使用Lua脚本保证原子性：先判断key是否存在，如果存在则删除
        String luaScript = "if redis.call('exists', KEYS[1]) == 1 then " +
                           "    return redis.call('del', KEYS[1]) " +
                           "else " +
                           "    return 0 " +
                           "end";
        
        Long result = redisTemplate.execute(
            new org.springframework.data.redis.core.script.DefaultRedisScript<>(luaScript, Long.class),
            java.util.Collections.singletonList(key)
        );
        
        return result != null && result == 1;
    }

    /**
     * 检查Token是否存在（不删除）
     * @param token Token字符串
     * @return true-Token存在，false-Token不存在或已失效
     */
    public boolean checkTokenExists(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String key = IDEMPOTENT_TOKEN_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}