import { http } from '@/utils/request';

export interface NotificationItem {
  id: number;
  userId: number;
  senderId?: number;
  type: string;
  title: string;
  content?: string;
  link?: string;
  isRead: boolean;
  createdAt: string;
}

export function getNotifications(page = 1, size = 20): Promise<NotificationItem[]> {
  return http.get('/notifications', { params: { page, size } });
}

export function getUnreadCount(): Promise<number> {
  return http.get('/notifications/unread-count');
}

export function markAsRead(id: number): Promise<void> {
  return http.put(`/notifications/${id}/read`);
}

export function markAllRead(): Promise<void> {
  return http.put('/notifications/read-all');
}
