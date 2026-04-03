<template>
  <div class="collab-indicator" v-if="onlineUsers.length > 0">
    <el-tooltip content="在线协作者" placement="bottom">
      <div class="users">
        <div
          v-for="user in onlineUsers"
          :key="user.id"
          class="user-avatar"
          :style="{ backgroundColor: getColor(user.id) }"
        >
          {{ user.name?.charAt(0) || 'U' }}
        </div>
        <span class="user-count">{{ onlineUsers.length }}人在线</span>
      </div>
    </el-tooltip>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue';
import wsClient, { type WSMessage } from '@/utils/websocket';

interface OnlineUser {
  id: number;
  name: string;
}

const onlineUsers = ref<OnlineUser[]>([]);

const COLORS = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#9b59b6', '#1abc9c'];

const getColor = (userId: number) => {
  return COLORS[userId % COLORS.length];
};

wsClient.on('user_join', (msg: WSMessage) => {
  if (msg.userId && !onlineUsers.value.find(u => u.id === msg.userId)) {
    onlineUsers.value.push({ id: msg.userId, name: `用户${msg.userId}` });
  }
});

wsClient.on('user_leave', (msg: WSMessage) => {
  onlineUsers.value = onlineUsers.value.filter(u => u.id !== msg.userId);
});

wsClient.on('online_users', (msg: WSMessage) => {
  if (msg.users) {
    onlineUsers.value = msg.users.map(id => ({ id, name: `用户${id}` }));
  }
});

onBeforeUnmount(() => {
  wsClient.off('user_join');
  wsClient.off('user_leave');
  wsClient.off('online_users');
});
</script>

<style scoped lang="scss">
.collab-indicator {
  .users {
    display: flex;
    align-items: center;
    gap: 4px;

    .user-avatar {
      width: 26px;
      height: 26px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 12px;
      font-weight: 600;
      border: 2px solid #fff;
      box-shadow: 0 1px 3px rgba(0,0,0,0.15);
    }

    .user-count {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-left: 4px;
    }
  }
}
</style>
