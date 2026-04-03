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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * CSRF Token 防护过滤器
 * - GET 请求豁免
 * - POST/PUT/DELETE 需携带 X-CSRF-Token header
 * - 公开接口豁免：/auth/**, /share/**
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
@RequiredArgsConstructor
public class CsrfFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String CSRF_HEADER = "X-CSRF-Token";
    private static final long CSRF_EXPIRE_HOURS = 24;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/auth/**",
            "/share/**",
            "/csrf/token",
            "/doc.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/ws/**"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        return EXCLUDED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader(CSRF_HEADER);
        if (token == null || token.isEmpty()) {
            sendError(response, 403, "缺少 CSRF Token，请通过 GET /csrf/token 获取");
            return;
        }

        String redisKey = "csrf:" + token;
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (Boolean.FALSE.equals(exists)) {
            sendError(response, 403, "CSRF Token 无效或已过期");
            return;
        }

        // 使用后删除 token（一次性）
        redisTemplate.delete(redisKey);
        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Result.error(code, message)));
    }
}
