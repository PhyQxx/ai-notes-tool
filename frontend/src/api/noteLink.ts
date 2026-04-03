import { http } from '../utils/request';

export interface NoteLink {
  id: number;
  sourceNoteId: number;
  targetNoteId: number;
  sourceTitle: string;
  targetTitle: string;
  createdAt: string;
}

export function getBacklinks(noteId: number): Promise<NoteLink[]> {
  return http.get(`/notes/${noteId}/backlinks`);
}

export function searchTitles(keyword: string): Promise<any[]> {
  return http.get('/notes/search-titles', { params: { keyword } });
}
