package com.ainotes.service;

import com.ainotes.dto.request.LoginRequest;
import com.ainotes.dto.request.RegisterRequest;
import com.ainotes.dto.response.LoginResponse;
import com.ainotes.entity.User;

/**
 * 认证服务接口
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
public interface AuthService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户ID
     */
    Long register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新令牌
     * @return 登录响应
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 获取个人信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getProfile(Long userId);

    /**
     * 更新个人信息
     *
     * @param userId 用户ID
     * @param user   用户信息
     */
    void updateProfile(Long userId, User user);

}
