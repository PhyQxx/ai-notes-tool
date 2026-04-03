import { http } from '../utils/request'

export interface NoteAttachment {
  id: number
  noteId: number
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
  uploadedBy: number
  createdAt: string
}

export function uploadAttachment(noteId: number, file: File, onProgress?: (p: number) => void) {
  const fd = new FormData()
  fd.append('file', file)
  return http.post<NoteAttachment>(`/notes/${noteId}/attachments`, fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: e => { if (onProgress && e.total) onProgress(Math.round(e.loaded / e.total * 100)) }
  })
}

export function listAttachments(noteId: number) {
  return http.get<NoteAttachment[]>(`/notes/${noteId}/attachments`)
}

export function deleteAttachment(noteId: number, id: number) {
  return http.delete(`/notes/${noteId}/attachments/${id}`)
}

export function getAttachmentDownloadUrl(noteId: number, id: number) {
  return `/api/notes/${noteId}/attachments/${id}/download`
}
