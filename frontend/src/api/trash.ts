import { http } from '../utils/request';
import type { ApiResponse, PageResult, Note } from '../types';

export function listTrash(page = 1, size = 20): Promise<ApiResponse<PageResult<Note>>> {
  return http.get('/notes/trash', { params: { page, size } });
}

export function restoreNote(noteId: number): Promise<ApiResponse<void>> {
  return http.post(`/notes/${noteId}/restore`);
}

export function permanentDeleteNote(noteId: number): Promise<ApiResponse<void>> {
  return http.delete(`/notes/trash/${noteId}`);
}

export function emptyTrash(): Promise<ApiResponse<void>> {
  return http.delete('/notes/trash');
}
