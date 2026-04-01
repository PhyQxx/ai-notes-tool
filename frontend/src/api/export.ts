/**
 * 导出功能API
 */
import { http } from '../utils/request';

/**
 * 导出为Markdown
 */
export function exportMarkdown(noteId: number): Promise<Blob> {
  return http.post(`/notes/${noteId}/export/md`, {}, {
    responseType: 'blob'
  });
}

/**
 * 导出为PDF
 */
export function exportPDF(noteId: number): Promise<Blob> {
  return http.post(`/notes/${noteId}/export/pdf`, {}, {
    responseType: 'blob'
  });
}

/**
 * 导出为Word
 */
export function exportWord(noteId: number): Promise<Blob> {
  return http.post(`/notes/${noteId}/export/word`, {}, {
    responseType: 'blob'
  });
}

/**
 * 通用下载方法：创建a标签触发下载
 */
export function downloadFile(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}
