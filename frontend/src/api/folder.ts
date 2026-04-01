/**
 * 文件夹相关API
 */
import { http } from '../utils/request';
import type { Folder } from '../types';

/**
 * 获取文件夹列表
 */
export function listFolders(): Promise<Folder[]> {
  return http.get('/folders');
}

/**
 * 获取文件夹树
 */
export function getFolderTree(): Promise<Folder[]> {
  return http.get('/folders/tree');
}

/**
 * 创建文件夹
 */
export function createFolder(data: {
  name: string;
  parentId?: number;
}): Promise<Folder> {
  return http.post('/folders', data);
}

/**
 * 更新文件夹
 */
export function updateFolder(id: number, data: {
  name?: string;
  parentId?: number;
}): Promise<Folder> {
  return http.put(`/folders/${id}`, data);
}

/**
 * 删除文件夹
 */
export function deleteFolder(id: number): Promise<void> {
  return http.delete(`/folders/${id}`);
}
