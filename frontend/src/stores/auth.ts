/**
 * 认证状态管理
 */
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { login as loginApi, register as registerApi, getProfile } from '../api/auth';
import { getToken, setToken, removeToken, setUser, removeUser } from '../utils/storage';
import type { LoginRequest, RegisterRequest, User, LoginResponse } from '../types';

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string>(getToken() || '');
  const user = ref<User | null>(null);

  // 计算属性
  const isLoggedIn = computed(() => !!token.value);

  // Actions
  /**
   * 登录
   */
  async function login(data: LoginRequest): Promise<void> {
    try {
      const response: LoginResponse = await loginApi(data);
      token.value = response.token;
      setToken(response.token);
      user.value = response.user;
      setUser(response.user);
    } catch (error) {
      throw error;
    }
  }

  /**
   * 注册
   */
  async function register(data: RegisterRequest): Promise<void> {
    try {
      const response: LoginResponse = await registerApi(data);
      token.value = response.token;
      setToken(response.token);
      user.value = response.user;
      setUser(response.user);
    } catch (error) {
      throw error;
    }
  }

  /**
   * 登出
   */
  function logout(): void {
    token.value = '';
    user.value = null;
    removeToken();
    removeUser();
  }

  /**
   * 获取个人信息
   */
  async function fetchProfile(): Promise<void> {
    try {
      const userInfo = await getProfile();
      user.value = userInfo;
      setUser(userInfo);
    } catch (error) {
      throw error;
    }
  }

  /**
   * 初始化用户信息
   */
  async function initAuth(): Promise<void> {
    if (token.value && !user.value) {
      await fetchProfile();
    } else if (!token.value) {
      // 无Token，自动以访客身份登录
      await guestLogin();
    }
  }

  /**
   * 访客自动登录（无感进入）
   */
  async function guestLogin(): Promise<void> {
    const timestamp = Date.now();
    const guestEmail = `guest_${timestamp}@ainotes.local`;
    const guestPwd = `guest_${timestamp}`;
    try {
      // 尝试注册访客账号
      await register({ email: guestEmail, password: guestPwd, username: '访客' });
    } catch (e: any) {
      // 如果注册失败（可能已存在），尝试登录
      if (e.message && !e.message.includes('已存在')) {
        try {
          await login({ email: guestEmail, password: guestPwd });
        } catch {
          // 忽略登录失败
        }
      }
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    login,
    register,
    logout,
    fetchProfile,
    initAuth
  };
});
