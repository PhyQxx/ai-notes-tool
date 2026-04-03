import { http } from '../utils/request';

export interface NoteShare {
  id: number;
  noteId: number;
  slug: string;
  password?: string;
  viewCount: number;
  expireAt?: string;
  createdAt: string;
  createdBy: number;
}

export interface SharedNoteData {
  title: string;
  content: string;
  createdAt: string;
  author: number;
}

export function createShare(noteId: number, data: { slug?: string; password?: string; expireAt?: string }): Promise<NoteShare> {
  return http.post(`/notes/${noteId}/share`, data);
}

export function listShares(noteId: number): Promise<NoteShare[]> {
  return http.get(`/notes/${noteId}/shares`);
}

export function deleteShare(noteId: number, shareId: number): Promise<void> {
  return http.delete(`/notes/${noteId}/shares/${shareId}`);
}

export function getSharedNote(slug: string, password?: string): Promise<SharedNoteData> {
  return http.get(`/share/${slug}`, { params: password ? { password } : {} });
}
