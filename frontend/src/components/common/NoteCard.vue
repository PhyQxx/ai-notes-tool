<template>
  <div class="note-card" @click="$emit('click')">
    <div class="note-header">
      <h4 class="note-title">{{ note.title || '无标题' }}</h4>
      <div class="note-actions" @click.stop>
        <el-icon
          :class="{ 'is-active': note.isFavorite }"
          @click="$emit('toggle-favorite')"
        >
          <Star />
        </el-icon>
        <el-icon v-if="note.isTop" class="is-top">
          <Top />
        </el-icon>
      </div>
    </div>

    <p class="note-preview">{{ note.content.substring(0, 100) }}...</p>

    <div class="note-meta">
      <div class="note-tags">
        <el-tag
          v-for="tag in note.tags.slice(0, 3)"
          :key="tag"
          size="small"
          type="info"
        >
          {{ tag }}
        </el-tag>
      </div>
      <span class="note-time">{{ formatDate(note.updatedAt) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';
import type { Note } from '@/types';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

defineProps<{
  note: Note;
}>();

defineEmits<{
  click: [];
  'toggle-favorite': [];
}>();

const formatDate = (date: string) => {
  return dayjs(date).fromNow();
};
</script>

<style scoped lang="scss">
.note-card {
  .note-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: 12px;

    .note-title {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .note-actions {
      display: flex;
      gap: 8px;
      margin-left: 12px;

      .el-icon {
        font-size: 16px;
        color: var(--el-text-color-placeholder);
        cursor: pointer;
        transition: color 0.2s;

        &:hover {
          color: var(--el-text-color-regular);
        }

        &.is-active {
          color: #f56c6c;
        }

        &.is-top {
          color: #409eff;
        }
      }
    }
  }

  .note-preview {
    margin: 0 0 12px 0;
    font-size: 14px;
    color: var(--el-text-color-secondary);
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .note-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;

    .note-tags {
      display: flex;
      gap: 4px;
      flex: 1;
      overflow: hidden;
    }

    .note-time {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
      white-space: nowrap;
    }
  }
}
</style>
