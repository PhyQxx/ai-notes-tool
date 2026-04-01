/**
 * 评论相关API
 */
import { http } from '../utils/request';
import type { Comment, ApiResponse } from '../types';

/**
 * 获取笔记评论列表
 */
export function listComments(noteId: number): Promise<Comment[]> {
  return http.get(`/comments`, { params: { noteId } });
}

/**
 * 创建评论
 */
export function createComment(data: {
  noteId: number;
  parentId?: number;
  content: string;
}): Promise<Comment> {
  return http.post('/comments', data);
}

/**
 * 删除评论
 */
export function deleteComment(commentId: number): Promise<void> {
  return http.delete(`/comments/${commentId}`);
}
