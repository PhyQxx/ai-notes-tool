/**
 * 版本管理API
 */
import { http } from '../utils/request';
import type { ApiResponse, NoteVersion } from '../types';

/**
 * 获取笔记版本列表
 */
export function listVersions(noteId: number): Promise<ApiResponse<NoteVersion[]>> {
  return http.get(`/notes/${noteId}/versions`);
}

/**
 * 获取版本详情
 */
export function getVersion(noteId: number, versionId: number): Promise<ApiResponse<NoteVersion>> {
  return http.get(`/notes/${noteId}/versions/${versionId}`);
}

/**
 * 创建版本快照
 */
export function createVersion(noteId: number, remark?: string): Promise<ApiResponse<void>> {
  return http.post(`/notes/${noteId}/versions`, { remark });
}

/**
 * 恢复版本
 */
export function restoreVersion(noteId: number, versionId: number): Promise<ApiResponse<void>> {
  return http.post(`/notes/${noteId}/versions/${versionId}/restore`);
}

/**
 * 删除版本
 */
export function deleteVersion(noteId: number, versionId: number): Promise<ApiResponse<void>> {
  return http.delete(`/notes/${noteId}/versions/${versionId}`);
}
