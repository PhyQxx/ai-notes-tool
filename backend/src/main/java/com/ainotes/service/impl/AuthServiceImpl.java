package com.ainotes.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ainotes.common.exception.BusinessException;
import com.ainotes.dto.request.LoginRequest;
import com.ainotes.dto.request.RegisterRequest;
import com.ainotes.dto.response.LoginResponse;
import com.ainotes.entity.User;
import com.ainotes.mapper.UserMapper;
import com.ainotes.service.AuthService;
import com.ainotes.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 认证服务实现类
 *
 * @author AI Notes Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        // 校验用户名是否存在
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, request.getUsername());
        User existUser = userMapper.selectOne(usernameQuery);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 校验邮箱是否存在
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, request.getEmail());
        existUser = userMapper.selectOne(emailQuery);
        if (existUser != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        // 密码BCrypt加密
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setStatus(1);

        userMapper.insert(user);
        log.info("用户注册成功，用户ID：{}", user.getId());

        return user.getId();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, request.getEmail());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("邮箱或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
        }

        // 校验用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 清空密码
        user.setPassword(null);

        log.info("用户登录成功，用户ID：{}", user.getId());

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .user(user)
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证Token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException("Refresh Token无效或已过期");
        }

        // 获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BusinessException("无法从Token中获取用户信息");
        }

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成新Token
        String token = jwtUtil.generateToken(userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);

        // 清空密码
        user.setPassword(null);

        return LoginResponse.builder()
                .token(token)
                .refreshToken(newRefreshToken)
                .user(user)
                .build();
    }

    @Override
    public User getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 清空密码
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, User user) {
        User existUser = userMapper.selectById(userId);
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新字段
        if (StringUtils.hasText(user.getNickname())) {
            existUser.setNickname(user.getNickname());
        }
        if (StringUtils.hasText(user.getAvatar())) {
            existUser.setAvatar(user.getAvatar());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userMapper.updateById(existUser);
        log.info("用户更新个人信息成功，用户ID：{}", userId);
    }

}
