package com.ainotes.controller;

import com.ainotes.dto.request.LoginRequest;
import com.ainotes.dto.request.RegisterRequest;
import com.ainotes.dto.response.LoginResponse;
import com.ainotes.entity.User;
import com.ainotes.service.AuthService;
import com.ainotes.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户认证", description = "用户注册、登录、个人信息管理")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户ID
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return Result.success("注册成功", userId);
    }

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success("登录成功", response);
    }

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新令牌
     * @return 登录响应
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return Result.success("刷新成功", response);
    }

    /**
     * 获取个人信息
     *
     * @param authentication 认证信息
     * @return 用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取个人信息")
    public Result<User> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = authService.getProfile(userId);
        return Result.success(user);
    }

    /**
     * 更新个人信息
     *
     * @param user          用户信息
     * @param authentication 认证信息
     * @return 成功信息
     */
    @PutMapping("/profile")
    @Operation(summary = "更新个人信息")
    public Result<Void> updateProfile(@RequestBody User user, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        authService.updateProfile(userId, user);
        return Result.success("更新成功");
    }

}
