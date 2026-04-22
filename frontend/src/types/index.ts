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
  spaceId?: number;
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

// AI相关
export interface AIChatMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

export interface AIConversation {
  id: number;
  noteId?: number;
  aiProvider: string;
  aiModel: string;
  title: string;
  messages: AIChatMessage[];
  createdAt: string;
  updatedAt: string;
}

export interface AIChatRequest {
  noteId?: number;
  provider: string;
  model: string;
  message: string;
  conversationId?: number;
}

export interface AIGenerateRequest {
  noteId: number;
  provider: string;
  model: string;
  prompt: string;
  type: 'summarize' | 'optimize' | 'expand' | 'rewrite' | 'continue';
}

export interface AIConfig {
  provider: string;
  model: string;
  deepseekApiKey?: string;
  glmApiKey?: string;
  minimaxApiKey?: string;
  hasApiKey?: boolean;
}

export interface AIProviderInfo {
  name: string;
  displayName: string;
  defaultModel: string;
  models: string[];
}

// 通用
export interface PageResult<T> {
  records: T[];
  total: number;
  page: number;
  size: number;
  current?: number;
  sortBy?: string;
}

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// 版本管理相关
export interface NoteVersion {
  id: number;
  noteId: number;
  versionNo: number;
  title: string;
  content: string;
  remark: string;
  createdBy: number;
  createdAt: string;
}

export interface VersionCompare {
  oldContent: string;
  newContent: string;
  diffHtml: string;
}

// 团队协作相关
export interface Space {
  id: number;
  name: string;
  description: string;
  ownerName: string;
  memberCount: number;
  createdAt: string;
  updatedAt: string;
  myRole: string;
}

export interface SpaceMember {
  id: number;
  userId: number;
  username: string;
  nickname: string;
  avatar: string;
  role: string;
  joinedAt: string;
}

export interface Comment {
  id: number;
  noteId: number;
  userId: number;
  username: string;
  nickname: string;
  avatar: string;
  parentId: number;
  content: string;
  createdAt: string;
  replies: Comment[];
}
