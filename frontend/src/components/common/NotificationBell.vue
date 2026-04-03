<template>
  <el-popover
    placement="bottom-end"
    :width="360"
    trigger="click"
    @show="handleShow"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="!hasUnread" :max="99">
        <el-button :icon="Bell" circle size="small" />
      </el-badge>
    </template>

    <div class="notification-panel">
      <div class="panel-header">
        <span class="title">通知</span>
        <el-button
          v-if="hasUnread"
          type="primary"
          link
          size="small"
          @click="handleMarkAllRead"
        >
          全部已读
        </el-button>
      </div>

      <div v-if="notifications.length === 0" class="empty-state">
        <el-empty description="暂无通知" :image-size="60" />
      </div>

      <div v-else class="notification-list">
        <div
          v-for="item in notifications.slice(0, 10)"
          :key="item.id"
          class="notification-item"
          :class="{ unread: !item.isRead }"
          @click="handleClick(item)"
        >
          <div class="item-dot" />
          <div class="item-content">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-desc">{{ item.content }}</div>
            <div class="item-time">{{ formatTime(item.createdAt) }}</div>
          </div>
        </div>
      </div>

      <div v-if="notifications.length > 10" class="panel-footer">
        <el-button link type="primary" @click="goToNotifications">
          查看全部
        </el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { Bell } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useNotificationStore } from '@/stores/notification';
import type { NotificationItem } from '@/api/notification';

const router = useRouter();
const store = useNotificationStore();
const { notifications, unreadCount, hasUnread } = store;

const handleShow = () => {
  store.fetchNotifications();
};

const handleClick = async (item: NotificationItem) => {
  if (!item.isRead) {
    await store.markAsRead(item.id);
  }
  if (item.link) {
    router.push(item.link);
  }
};

const handleMarkAllRead = () => {
  store.markAllRead();
};

const goToNotifications = () => {
  router.push('/notifications');
};

const formatTime = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr.replace(' ', 'T'));
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}小时前`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString();
};
</script>

<style scoped lang="scss">
.notification-panel {
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .title {
      font-weight: 600;
      font-size: 15px;
    }
  }

  .notification-list {
    max-height: 400px;
    overflow-y: auto;
  }

  .notification-item {
    display: flex;
    gap: 10px;
    padding: 10px 4px;
    cursor: pointer;
    border-radius: 6px;
    transition: background 0.2s;

    &:hover {
      background-color: var(--el-fill-color-light);
    }

    .item-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background-color: var(--el-border-color);
      margin-top: 6px;
      flex-shrink: 0;
    }

    &.unread .item-dot {
      background-color: var(--el-color-danger);
    }

    &.unread .item-title {
      font-weight: 600;
    }

    .item-content {
      flex: 1;
      min-width: 0;

      .item-title {
        font-size: 14px;
        color: var(--el-text-color-primary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-desc {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-top: 2px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .item-time {
        font-size: 12px;
        color: var(--el-text-color-placeholder);
        margin-top: 4px;
      }
    }
  }

  .panel-footer {
    text-align: center;
    padding-top: 8px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}
</style>
