<template>
  <div class="notification-view">
    <div class="page-header">
      <h2>通知中心</h2>
      <div class="actions">
        <el-radio-group v-model="filter" size="small" @change="loadNotifications">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="unread">未读</el-radio-button>
        </el-radio-group>
        <el-button
          v-if="hasUnread"
          type="primary"
          size="small"
          @click="handleMarkAllRead"
        >
          全部已读
        </el-button>
      </div>
    </div>

    <div v-if="filteredNotifications.length === 0" class="empty-state">
      <el-empty description="暂无通知" />
    </div>

    <div v-else class="notification-list">
      <div
        v-for="item in filteredNotifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: !item.isRead }"
        @click="handleClick(item)"
      >
        <div class="item-icon">
          <el-icon :size="20">
            <component :is="getIcon(item.type)" />
          </el-icon>
        </div>
        <div class="item-body">
          <div class="item-header">
            <span class="item-title">{{ item.title }}</span>
            <el-tag v-if="!item.isRead" type="danger" size="small" effect="dark">未读</el-tag>
          </div>
          <div class="item-content">{{ item.content }}</div>
          <div class="item-time">{{ formatTime(item.createdAt) }}</div>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="filteredNotifications.length > 0"
      class="pagination"
      layout="prev, pager, next"
      :total="100"
      :page-size="20"
      @current-change="loadNotifications"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useNotificationStore } from '@/stores/notification';
import type { NotificationItem } from '@/api/notification';
import { ChatDotRound, Share, Bell, Setting } from '@element-plus/icons-vue';

const router = useRouter();
const store = useNotificationStore();
const filter = ref<'all' | 'unread'>('all');

const filteredNotifications = computed(() => {
  if (filter.value === 'unread') {
    return store.notifications.filter(n => !n.isRead);
  }
  return store.notifications;
});

const hasUnread = computed(() => store.hasUnread);

const loadNotifications = async () => {
  await store.fetchNotifications();
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

const getIcon = (type: string) => {
  switch (type) {
    case 'COMMENT': return ChatDotRound;
    case 'SHARE': return Share;
    case 'SYSTEM': return Setting;
    default: return Bell;
  }
};

const formatTime = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr.replace(' ', 'T'));
  return date.toLocaleString();
};

onMounted(() => {
  loadNotifications();
  store.fetchUnreadCount();
});
</script>

<style scoped lang="scss">
.notification-view {
  max-width: 800px;
  margin: 0 auto;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      margin: 0;
    }

    .actions {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }

  .notification-item {
    display: flex;
    gap: 16px;
    padding: 16px;
    margin-bottom: 8px;
    border-radius: 8px;
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      box-shadow: 0 2px 8px rgba(0,0,0,0.08);
    }

    &.unread {
      border-left: 3px solid var(--el-color-primary);
      background: var(--el-color-primary-light-9);
    }

    .item-icon {
      flex-shrink: 0;
      width: 40px;
      height: 40px;
      border-radius: 8px;
      background: var(--el-fill-color-light);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--el-color-primary);
    }

    .item-body {
      flex: 1;
      min-width: 0;

      .item-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;

        .item-title {
          font-weight: 500;
          font-size: 14px;
        }
      }

      .item-content {
        font-size: 13px;
        color: var(--el-text-color-secondary);
        margin-bottom: 4px;
      }

      .item-time {
        font-size: 12px;
        color: var(--el-text-color-placeholder);
      }
    }
  }

  .pagination {
    margin-top: 24px;
    justify-content: center;
  }
}
</style>
