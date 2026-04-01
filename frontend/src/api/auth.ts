/**
 * 认证相关API
 */
import { http } from '../utils/request';
import type { LoginRequest, RegisterRequest, LoginResponse, User } from '../types';

/**
 * 用户登录
 */
export function login(data: LoginRequest): Promise<LoginResponse> {
  return http.post('/auth/login', data);
}

/**
 * 用户注册
 */
export function register(data: RegisterRequest): Promise<LoginResponse> {
  return http.post('/auth/register', data);
}

/**
 * 刷新Token
 */
export function refreshToken(token: string): Promise<{ token: string; refreshToken: string }> {
  return http.post('/auth/refresh', { token });
}

/**
 * 获取个人信息
 */
export function getProfile(): Promise<User> {
  return http.get('/auth/profile');
}

/**
 * 更新个人信息
 */
export function updateProfile(data: Partial<User>): Promise<User> {
  return http.put('/auth/profile', data);
}
