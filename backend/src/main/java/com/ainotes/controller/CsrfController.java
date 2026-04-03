package com.ainotes.controller;

import com.ainotes.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * CSRF Token 接口
 */
@RestController
@RequestMapping("/csrf")
@RequiredArgsConstructor
public class CsrfController {

    private final StringRedisTemplate redisTemplate;

    @GetMapping("/token")
    public Result<Map<String, String>> getCsrfToken() {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("csrf:" + token, "1", 24, TimeUnit.HOURS);
        return Result.success(Map.of("token", token));
    }
}
