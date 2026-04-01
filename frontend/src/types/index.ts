/**
 * TypeScript类型定义
 */

// 用户相关
export interface User {
  id: number;
  username: string;
  email: string;
  nickname: string;
  avatar: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  nickname?: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: User;
}

// 笔记相关
export interface Note {
  id: number;
  title: string;
  content: string;
  contentType: string;
  folderId: number;
  tags: string[];
  isFavorite: boolean;
  isTop: boolean;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Folder {
  id: number;
  name: string;
  parentId: number;
  children?: Folder[];
}

// 通用
export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  size: number;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}
