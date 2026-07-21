package com.fanfaction.interceptor;

import com.fanfaction.annotation.Idempotent;
import com.fanfaction.common.BusinessException;
import com.fanfaction.util.IdempotentTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * 幂等性拦截器
 * 拦截带有@Idempotent注解的接口，防止重复提交
 */
@Component
@RequiredArgsConstructor
public class IdempotentInterceptor implements HandlerInterceptor {

    private final IdempotentTokenUtil idempotentTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // 检查方法是否有@Idempotent注解
        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        if (idempotent == null) {
            return true;
        }

        // 获取请求头中的Token
        String token = request.getHeader(idempotent.headerName());
        if (token == null || token.isEmpty()) {
            throw new BusinessException(400, "缺少幂等性Token，请刷新页面后重试");
        }

        // 验证Token并删除
        boolean isValid = idempotentTokenUtil.validateAndDeleteToken(token);
        if (!isValid) {
            throw new BusinessException(400, idempotent.message());
        }

        return true;
    }
}