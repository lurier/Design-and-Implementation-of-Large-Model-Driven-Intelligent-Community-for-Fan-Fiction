package com.fanfaction.config;

import com.fanfaction.interceptor.IdempotentInterceptor;
import com.fanfaction.interceptor.RoleAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RoleAuthInterceptor roleAuthInterceptor;
    private final IdempotentInterceptor idempotentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 角色权限拦截器
        registry.addInterceptor(roleAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/test/**", "/api/test-data/**");

        // 幂等性拦截器
        registry.addInterceptor(idempotentInterceptor)
                .addPathPatterns("/api/**");
    }
}
