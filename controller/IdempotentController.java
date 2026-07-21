package com.fanfaction.controller;

import com.fanfaction.common.Result;
import com.fanfaction.util.IdempotentTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 幂等性Token控制器
 * 提供Token生成接口
 */
@Tag(name = "幂等性管理")
@RestController
@RequestMapping("/api/idempotent")
@RequiredArgsConstructor
public class IdempotentController {

    private final IdempotentTokenUtil idempotentTokenUtil;

    @Operation(summary = "获取幂等性Token")
    @GetMapping("/token")
    public Result<Map<String, Object>> getToken(
            @RequestParam(defaultValue = "300") long expireTime) {
        String token = idempotentTokenUtil.createToken(expireTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("expireTime", expireTime);
        result.put("headerName", "Idempotent-Token");
        
        return Result.success(result);
    }

    @Operation(summary = "验证Token是否存在（不删除）")
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestParam String token) {
        boolean exists = idempotentTokenUtil.checkTokenExists(token);
        return Result.success(exists);
    }
}