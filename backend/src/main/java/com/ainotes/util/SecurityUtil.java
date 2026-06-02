package com.ainotes.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public class SecurityUtil {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录则返回null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前登录用户ID（如果不满足则抛出异常）
     *
     * @return 用户ID
     * @throws RuntimeException 未登录时抛出
     */
    public static Long getRequiredCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }
}
