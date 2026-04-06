/**
 * 笔记相关API
 */
import { http } from '../utils/request';
import type { Note, PageResult } from '../types';

/**
 * 获取笔记列表
 */
export function listNotes(params: {
  page?: number;
  size?: number;
  folderId?: number;
  keyword?: string;
  sortBy?: string;
  isFavorite?: number;
}): Promise<PageResult<Note>> {
  return http.get('/notes', { params });
}

/**
 * 获取笔记详情
 */
export function getNote(id: number): Promise<Note> {
  return http.get(`/notes/${id}`);
}

/**
 * 创建笔记
 */
export function createNote(data: {
  title: string;
  content: string;
  contentType: string;
  folderId?: number;
  tags?: string[];
}): Promise<Note> {
  return http.post('/notes', data);
}

/**
 * 更新笔记
 */
export function updateNote(id: number, data: Partial<Note>): Promise<Note> {
  return http.put(`/notes/${id}`, data);
}

/**
 * 删除笔记
 */
export function deleteNote(id: number): Promise<void> {
  return http.delete(`/notes/${id}`);
}

/**
 * 搜索笔记
 */
export function searchNotes(keyword: string, params?: {
  page?: number;
  size?: number;
}): Promise<PageResult<Note>> {
  return http.get('/notes/search', {
    params: { keyword, ...params }
  });
}

export interface SearchResult {
  id: number;
  title: string;
  titleHighlight: string;
  contentPreview: string;
  contentType: string;
  isFavorite: boolean;
  isTop: boolean;
  tags: string;
  updatedAt: string;
  matchCount: number;
}

export function fullTextSearch(keyword: string, scope: string = 'all'): Promise<PageResult<SearchResult>> {
  return http.get('/notes/search/fulltext', {
    params: { keyword, scope }
  });
}

/**
 * 收藏/取消收藏笔记
 */
export function toggleFavorite(id: number): Promise<void> {
  return http.post(`/notes/${id}/favorite`);
}

/**
 * 置顶/取消置顶笔记
 */
export function toggleTop(id: number): Promise<void> {
  return http.post(`/notes/${id}/top`);
}

/**
 * 获取推荐笔记
 */
export function getRecommendNotes(noteId?: number, limit: number = 5): Promise<Note[]> {
  return http.get('/notes/recommend', { params: { noteId, limit } });
}

/**
 * 获取知识图谱数据
 */
export interface GraphNode {
  id: number;
  title: string;
  tags: string[];
}

export interface GraphEdge {
  source: number;
  target: number;
  type: 'link' | 'tag';
}

export interface GraphData {
  nodes: GraphNode[];
  edges: GraphEdge[];
}

export function getGraphData(): Promise<GraphData> {
  return http.get('/notes/graph');
}
