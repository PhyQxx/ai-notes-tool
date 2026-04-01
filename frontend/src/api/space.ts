/**
 * 团队空间相关API
 */
import { http } from '../utils/request';
import type { Space, SpaceMember, ApiResponse } from '../types';

/**
 * 获取空间列表
 */
export function listSpaces(): Promise<Space[]> {
  return http.get('/spaces');
}

/**
 * 创建空间
 */
export function createSpace(data: {
  name: string;
  description: string;
}): Promise<Space> {
  return http.post('/spaces', data);
}

/**
 * 更新空间
 */
export function updateSpace(id: number, data: {
  name?: string;
  description?: string;
}): Promise<void> {
  return http.put(`/spaces/${id}`, data);
}

/**
 * 删除空间
 */
export function deleteSpace(id: number): Promise<void> {
  return http.delete(`/spaces/${id}`);
}

/**
 * 获取空间详情
 */
export function getSpaceDetail(id: number): Promise<Space> {
  return http.get(`/spaces/${id}`);
}

/**
 * 获取空间成员列表
 */
export function getMembers(spaceId: number): Promise<SpaceMember[]> {
  return http.get(`/spaces/${spaceId}/members`);
}

/**
 * 邀请成员
 */
export function inviteMember(spaceId: number, data: {
  email: string;
  role: string;
}): Promise<void> {
  return http.post(`/spaces/${spaceId}/members`, data);
}

/**
 * 移除成员
 */
export function removeMember(spaceId: number, userId: number): Promise<void> {
  return http.delete(`/spaces/${spaceId}/members/${userId}`);
}

/**
 * 更新成员角色
 */
export function updateMemberRole(spaceId: number, userId: number, role: string): Promise<void> {
  return http.put(`/spaces/${spaceId}/members/${userId}`, { role });
}

/**
 * 退出空间
 */
export function leaveSpace(spaceId: number): Promise<void> {
  return http.post(`/spaces/${spaceId}/leave`);
}
