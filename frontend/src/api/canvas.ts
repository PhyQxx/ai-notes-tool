import { http } from '../utils/request';

export interface Canvas {
  id: number;
  userId: number;
  title: string;
  data: string;
  createdAt: string;
  updatedAt: string;
}

export function listCanvases() {
  return http.get<Canvas[]>('/canvas');
}

export function getCanvas(id: number) {
  return http.get<Canvas>(`/canvas/${id}`);
}

export function createCanvas(title: string) {
  return http.post<Canvas>('/canvas', { title });
}

export function updateCanvas(id: number, data: string) {
  return http.put(`/canvas/${id}`, { data });
}

export function deleteCanvas(id: number) {
  return http.delete(`/canvas/${id}`);
}
