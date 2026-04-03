/**
 * 评论相关API
 */
import { http } from '../utils/request';

export interface Comment {
  id: number;
  noteId: number;
  userId: number;
  parentId: number;
  content: string;
  mentionUserIds?: string;
  resolveStatus?: string;
  positionText?: string;
  username?: string;
  nickname?: string;
  avatar?: string;
  createdAt: string;
  replies?: Comment[];
}

export function listComments(noteId: number): Promise<Comment[]> {
  return http.get(`/notes/${noteId}/comments`);
}

export function createComment(data: {
  noteId: number;
  parentId?: number;
  content: string;
  mentionUserIds?: string;
  positionText?: string;
}): Promise<number> {
  return http.post(`/notes/${noteId}/comments`, data);
}

export function deleteComment(commentId: number): Promise<void> {
  return http.delete(`/comments/${commentId}`);
}

export function toggleCommentStatus(commentId: number, status: string): Promise<void> {
  return http.put(`/comments/${commentId}/status?status=${status}`);
}
