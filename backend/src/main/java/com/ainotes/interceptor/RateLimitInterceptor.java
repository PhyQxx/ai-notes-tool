package com.ainotes.interceptor;

import com.alibaba.fastjson2.JSON;
import com.ainotes.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * API限流拦截器（基于Redis令牌桶）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    // 全局限流：100次/秒
    private static final int GLOBAL_LIMIT = 100;
    private static final int GLOBAL_WINDOW_SECONDS = 1;

    // 用户级限流：30次/秒
    private static final int USER_LIMIT = 30;
    private static final int USER_WINDOW_SECONDS = 1;

    // AI接口单独限流：10次/分钟
    private static final int AI_LIMIT = 10;
    private static final int AI_WINDOW_SECONDS = 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String uri = request.getRequestURI();
        String ip = getClientIp(request);

        // 全局限流
        if (!checkLimit("rate:global:", String.valueOf(System.currentTimeMillis() / 1000), GLOBAL_LIMIT, GLOBAL_WINDOW_SECONDS)) {
            sendRateLimitResponse(response, 429, "请求过于频繁，请稍后再试");
            return false;
        }

        // AI接口限流
        if (uri.startsWith("/ai/")) {
            if (!checkLimit("rate:ai:" + ip, String.valueOf(System.currentTimeMillis() / 60000), AI_LIMIT, AI_WINDOW_SECONDS)) {
                sendRateLimitResponse(response, 429, "AI请求过于频繁，每分钟最多" + AI_LIMIT + "次");
                return false;
            }
        }

        // 用户级限流（如果已认证，用userId；否则用ip）
        String userId = (String) request.getAttribute("userId");
        String userKey = userId != null ? userId : ip;
        if (!checkLimit("rate:user:" + userKey, String.valueOf(System.currentTimeMillis() / 1000), USER_LIMIT, USER_WINDOW_SECONDS)) {
            sendRateLimitResponse(response, 429, "操作过于频繁，请稍后再试");
            return false;
        }

        return true;
    }

    private boolean checkLimit(String prefix, String windowKey, int limit, int windowSeconds) {
        String key = prefix + windowKey;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        return count == null || count <= limit;
    }

    private void sendRateLimitResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(code, message);
        response.getWriter().write(JSON.toJSONString(result));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
