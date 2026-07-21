package com.fanfaction.interceptor;

import com.fanfaction.annotation.RequireRole;
import com.fanfaction.config.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 统一角色权限拦截器
 * - /api/admin/**  -> 仅 role=2（管理员）
 * - 文章发布/编辑/删除 -> 仅 role=1（创作者），管理员不能在前台发文
 * - @RequireRole 注解的方法 -> 按注解值校验
 *
 * 角色定义：0=普通读者  1=创作者  2=系统管理员
 *
 * 重要：角色判断优先使用 SecurityContext（由 JwtAuthenticationFilter 从数据库实时加载），
 * 而非 JWT Token 中的过期角色信息，确保管理员修改用户角色后立即生效。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 优先从 SecurityContext 获取实时角色（JwtAuthenticationFilter 已从数据库加载），
        // 确保管理员修改角色后立即生效，而非使用 JWT 中固化的过期角色
        Integer role = getRoleFromSecurityContext();
        if (role == null) {
            // 兜底：SecurityContext 未设置时从 JWT Token 提取
            String token = extractToken(request);
            if (StringUtils.hasText(token)) {
                try {
                    role = jwtUtils.extractRole(token);
                } catch (Exception e) {
                    log.warn("解析 Token 中的角色失败: {}", e.getMessage());
                }
            }
        }
        if (role == null) role = 0;

        // 规则1：/api/admin/** 路径仅允许管理员(role=2)
        if (uri.startsWith("/api/admin/")) {
            if (role != 2) {
                log.warn("非管理员尝试访问管理接口: uri={}, role={}", uri, role);
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅管理员可访问\",\"data\":null}");
                return false;
            }
            return true;
        }

        // 规则2：文章发布/编辑/删除 仅允许创作者(role=1)，管理员不能在前台发文
        if (uri.matches("^/api/articles(/(\\d+))?$") && ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))) {
            if (role != 1) {
                log.warn("非创作者尝试操作文章: uri={}, method={}, role={}", uri, method, role);
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足，仅创作者可发布/编辑/删除文章\",\"data\":null}");
                return false;
            }
            return true;
        }

        // 规则3：@RequireRole 注解校验
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }
            if (requireRole != null) {
                int requiredRole = requireRole.value();
                if (role < requiredRole) {
                    log.warn("角色不符: uri={}, required={}, actual={}", uri, requiredRole, role);
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足\",\"data\":null}");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 从 SecurityContext 获取用户实时角色
     * SecurityContext 由 JwtAuthenticationFilter 每次请求从数据库加载，确保角色最新
     * @return 角色值（0/1/2），若未认证则返回 null
     */
    private Integer getRoleFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return null;
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String authStr = authority.getAuthority();
            if ("ROLE_ADMIN".equals(authStr)) return 2;
            if ("ROLE_CREATOR".equals(authStr)) return 1;
        }
        // 已认证但非管理员/创作者，视为读者
        return 0;
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
