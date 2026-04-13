package com.ainotes.config;

import com.ainotes.entity.User;
import com.ainotes.mapper.UserMapper;
import com.ainotes.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * <p>
 * 支持Token自动续期：
 * 1. Token未过期但即将过期（剩余 < 30min）→ 响应头返回新Token
 * 2. Token已过期但签名有效，且用户存在 → 响应头返回新Token，请求正常放行
 * 3. Token签名无效 → 拒绝
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    private static final String HEADER_NAME = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String NEW_TOKEN_HEADER = "X-New-Token";
    /** 即将过期阈值：30分钟 */
    private static final long NEAR_EXPIRY_THRESHOLD = 30 * 60 * 1000L;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            // 1. 尝试正常验证（未过期的token）
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    setAuthentication(userId);

                    // 即将过期时，在响应头中返回新token
                    if (jwtUtil.isTokenNearExpiry(token, NEAR_EXPIRY_THRESHOLD)) {
                        String newToken = jwtUtil.generateToken(userId);
                        response.setHeader(NEW_TOKEN_HEADER, newToken);
                        log.debug("Token即将过期，已续期，用户ID：{}", userId);
                    }
                }
            } else {
                // 2. Token已过期，尝试从过期token中提取userId（签名仍有效）
                Long userId = jwtUtil.getUserIdFromExpiredToken(token);
                if (userId != null) {
                    User user = userMapper.selectById(userId);
                    if (user != null && user.getStatus() != 0) {
                        // 用户存在且未禁用，生成新token
                        setAuthentication(userId);
                        String newToken = jwtUtil.generateToken(userId);
                        response.setHeader(NEW_TOKEN_HEADER, newToken);
                        log.debug("过期Token已续期，用户ID：{}", userId);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 设置认证上下文
     */
    private void setAuthentication(Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_NAME);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

}
