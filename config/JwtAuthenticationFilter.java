package com.fanfaction.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfaction.entity.User;
import com.fanfaction.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fanfaction.common.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Redis key 前缀：用户封禁状态
    private static final String USER_BLOCKED_KEY = "user:blocked:";
    // 缓存过期时间（秒）
    private static final long CACHE_EXPIRE_SECONDS = 300;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                String username = jwtUtils.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 检查用户是否被封禁（优先查 Redis，缓存未命中则查库）
                    if (isUserBlocked(username)) {
                        // 用户被封禁，返回 403 + 自定义业务码 40301
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write(objectMapper.writeValueAsString(
                                Result.error(40301, "您的账号已被管理员封禁")));
                        return;
                    }

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtils.validateToken(token, username)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                log.error("JWT认证失败: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查用户是否被封禁
     * 优先从 Redis 缓存读取，缓存未命中则查数据库并写入缓存
     * Redis 异常时自动降级为直接查数据库
     */
    private boolean isUserBlocked(String username) {
        String cacheKey = USER_BLOCKED_KEY + username;
        
        // 1. 先查 Redis 缓存（异常时降级查库）
        try {
            String cachedStatus = redisTemplate.opsForValue().get(cacheKey);
            if (cachedStatus != null) {
                return "1".equals(cachedStatus);
            }
        } catch (Exception e) {
            log.warn("Redis查询封禁状态失败，降级查数据库: {}", e.getMessage());
        }
        
        // 2. 缓存未命中或 Redis 异常，查数据库
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            return false;
        }
        
        // 3. 尝试写入 Redis 缓存（异常不影响主流程）
        try {
            String statusValue = (user.getStatus() != null && user.getStatus() == 0) ? "1" : "0";
            redisTemplate.opsForValue().set(cacheKey, statusValue, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis写入封禁状态缓存失败: {}", e.getMessage());
        }
        
        return (user.getStatus() != null && user.getStatus() == 0);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
