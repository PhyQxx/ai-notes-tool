/**
 * 笔记模板相关API
 */
import { http } from '../utils/request';

export interface NoteTemplate {
  id: number;
  name: string;
  content: string;
  icon: string;
  category: 'system' | 'custom';
  spaceId: number | null;
  variables: string;
  sortOrder: number;
  createdBy: number;
  createdAt: string;
  updatedAt: string;
}

export function listTemplates(params?: { category?: string; spaceId?: number }): Promise<NoteTemplate[]> {
  return http.get('/templates', { params });
}

export function getTemplate(id: number): Promise<NoteTemplate> {
  return http.get(`/templates/${id}`);
}

export function createTemplate(data: Partial<NoteTemplate>): Promise<number> {
  return http.post('/templates', data);
}

export function updateTemplate(id: number, data: Partial<NoteTemplate>): Promise<void> {
  return http.put(`/templates/${id}`, data);
}

export function deleteTemplate(id: number): Promise<void> {
  return http.delete(`/templates/${id}`);
}

export function applyTemplate(id: number, variables?: Record<string, string>): Promise<{ content: string }> {
  return http.post(`/templates/${id}/apply`, variables || {});
}
