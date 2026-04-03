import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { getNotifications, getUnreadCount, markAsRead as markReadApi, markAllRead as markAllApi } from '@/api/notification';
import type { NotificationItem } from '@/api/notification';
import wsClient, { type WSMessage } from '@/utils/websocket';

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<NotificationItem[]>([]);
  const unreadCount = ref(0);

  const hasUnread = computed(() => unreadCount.value > 0);

  async function fetchNotifications(page = 1, size = 20) {
    notifications.value = await getNotifications(page, size);
  }

  async function fetchUnreadCount() {
    unreadCount.value = await getUnreadCount();
  }

  async function markAsRead(id: number) {
    await markReadApi(id);
    const item = notifications.value.find(n => n.id === id);
    if (item) item.isRead = true;
    unreadCount.value = Math.max(0, unreadCount.value - 1);
  }

  async function markAllRead() {
    await markAllApi();
    notifications.value.forEach(n => (n.isRead = true));
    unreadCount.value = 0;
  }

  // 监听 WebSocket 实时通知
  function initWSListener() {
    wsClient.on('new_notification', (msg: WSMessage) => {
      if (msg.data) {
        const notification = msg.data as NotificationItem;
        notifications.value.unshift(notification);
        if (!notification.isRead) {
          unreadCount.value++;
        }
      }
    });
  }

  function removeWSListener() {
    wsClient.off('new_notification');
  }

  return {
    notifications,
    unreadCount,
    hasUnread,
    fetchNotifications,
    fetchUnreadCount,
    markAsRead,
    markAllRead,
    initWSListener,
    removeWSListener,
  };
});
