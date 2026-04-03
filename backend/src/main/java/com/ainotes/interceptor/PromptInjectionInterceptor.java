package com.ainotes.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * AI Prompt注入防护拦截器
 */
@Slf4j
@Component
public class PromptInjectionInterceptor implements HandlerInterceptor {

    private static final List<String> BLOCKED_PATTERNS = List.of(
            "ignore previous instructions",
            "ignore all above",
            "disregard all above",
            "you are now",
            "pretend you are",
            "act as",
            "roleplay as",
            "system prompt",
            "reveal your instructions",
            "forget everything",
            "new instructions",
            "override",
            "<system>",
            "</system>",
            "developer mode",
            "jailbreak"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/ai/")) {
            return true;
        }

        // Check query params for simple injection patterns
        String queryString = request.getQueryString();
        if (queryString != null && containsInjection(queryString)) {
            log.warn("Prompt injection detected in query: {}", uri);
            return false;
        }

        return true;
    }

    /**
     * Service层调用：检查用户消息是否包含注入
     */
    public static boolean containsInjection(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String pattern : BLOCKED_PATTERNS) {
            if (lower.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
