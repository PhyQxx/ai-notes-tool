import { http } from '../utils/request';

export interface TagCloudItem {
  name: string;
  count: number;
}

export interface TagColor {
  id?: number;
  name: string;
  color: string;
}

export function getTagCloud(spaceId?: number): Promise<TagCloudItem[]> {
  return http.get('/tags/cloud', { params: spaceId ? { spaceId } : {} });
}

export function getTagColors(): Promise<TagColor[]> {
  return http.get('/tags/colors');
}

export function setTagColors(colors: TagColor[]): Promise<void> {
  return http.put('/tags/colors', colors);
}

export function batchAddTags(noteIds: number[], tags: string[]): Promise<void> {
  return http.post('/tags/batch', { noteIds, tags });
}
