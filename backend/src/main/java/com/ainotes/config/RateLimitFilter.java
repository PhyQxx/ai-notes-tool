package com.ainotes.config;

import com.alibaba.fastjson2.JSON;
import com.ainotes.common.result.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 分层限流过滤器
 * - 读接口 GET: 60次/分 per user
 * - 写接口 POST/PUT/DELETE: 20次/分 per user
 * - AI 接口 /ai/**: 10次/分 per user
 * - 文件上传 /files/upload: 5次/分 per user
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    private static final int READ_LIMIT = 60;
    private static final int WRITE_LIMIT = 20;
    private static final int AI_LIMIT = 10;
    private static final int UPLOAD_LIMIT = 5;
    private static final int WINDOW_SECONDS = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        String identifier = getUserId(request);
        if (identifier == null) {
            identifier = getClientIp(request);
        }

        int limit;
        String type;

        if (uri.startsWith("/ai/")) {
            limit = AI_LIMIT;
            type = "AI";
        } else if (uri.contains("/upload") && uri.startsWith("/files/")) {
            limit = UPLOAD_LIMIT;
            type = "文件上传";
        } else if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            limit = READ_LIMIT;
            type = "读";
        } else if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            limit = WRITE_LIMIT;
            type = "写";
        } else {
            limit = WRITE_LIMIT;
            type = "写";
        }

        String windowKey = String.valueOf(System.currentTimeMillis() / (WINDOW_SECONDS * 1000));
        String redisKey = "rate:" + type.toLowerCase() + ":" + identifier + ":" + windowKey;

        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        if (count != null && count > limit) {
            log.warn("限流触发: type={}, identifier={}, count={}, limit={}", type, identifier, count, limit);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Retry-After", String.valueOf(WINDOW_SECONDS));

            // 构建详细响应
            Map<String, Object> data = new java.util.LinkedHashMap<>();
            data.put("retryAfter", WINDOW_SECONDS);
            data.put("remaining", 0);
            data.put("limit", limit);
            data.put("type", type);

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("code", 429);
            result.put("message", type + "请求过于频繁，请等待 " + WINDOW_SECONDS + " 秒后重试");
            result.put("data", data);

            response.getWriter().write(JSON.toJSONString(result));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getUserId(HttpServletRequest request) {
        if (request.getUserPrincipal() != null) {
            return request.getUserPrincipal().getName();
        }
        return null;
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
