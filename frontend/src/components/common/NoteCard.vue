<template>
  <div class="note-card" :class="{ selected: note.isTop }" @click="$emit('click')">
    <!-- 置顶标识 -->
    <div v-if="note.isTop" class="note-pin">📌 置顶</div>

    <!-- 标题 -->
    <h4 class="note-title" v-html="note.titleHighlight || note.title || '无标题'"></h4>

    <!-- 摘要 -->
    <p class="note-preview" v-if="note.contentPreview" v-html="note.contentPreview"></p>
    <p v-else class="note-preview">{{ (note.content || '').substring(0, 100) }}...</p>

    <!-- 标签 -->
    <div class="note-tags" v-if="note.tags && note.tags.length > 0">
      <span v-for="tag in note.tags.slice(0, 3)" :key="tag" class="note-tag">{{ tag }}</span>
    </div>

    <!-- 底部 -->
    <div class="note-footer">
      <span class="note-date">{{ formatDate(note.updatedAt) }}</span>
      <div class="note-actions" @click.stop>
        <el-icon
          :class="{ 'is-active': note.isFavorite }"
          @click="$emit('toggle-favorite')"
        >
          <Star />
        </el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';
import { Star } from '@element-plus/icons-vue';
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
  background: var(--bg-white);
  border: 1px solid transparent;
  border-radius: var(--radius-lg);
  padding: 16px;
  cursor: pointer;
  transition: all 0.15s;
  box-shadow: var(--shadow-md);

  &:hover {
    background: var(--card-hover);
    border-color: var(--brand-primary-border);
    box-shadow: var(--shadow-brand-hover);
  }

  &.selected {
    background: var(--card-selected);
    border-color: var(--brand-primary);
  }

  :deep(.search-highlight),
  :deep(em) {
    color: var(--brand-primary);
    font-weight: bold;
    background: var(--brand-primary-light-5);
    padding: 0 2px;
    border-radius: 2px;
    font-style: normal;
  }

  .note-pin {
    font-size: 12px;
    color: var(--color-warning);
    margin-bottom: 6px;
  }

  .note-title {
    margin: 0 0 6px 0;
    font-size: 15px;
    font-weight: 600;
    color: var(--text-strong);
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.4;
  }

  .note-preview {
    margin: 0 0 12px 0;
    font-size: 13px;
    color: var(--text-secondary);
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .note-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 10px;
  }

  .note-tag {
    padding: 2px 8px;
    background: var(--brand-primary-light-5);
    color: var(--brand-primary);
    border-radius: var(--radius-sm);
    font-size: 11px;
    font-weight: 500;
  }

  .note-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 10px;
    border-top: 1px solid var(--border);
  }

  .note-date {
    font-size: 12px;
    color: var(--text-tertiary);
  }

  .note-actions {
    display: flex;
    gap: 4px;

    .el-icon {
      font-size: 14px;
      color: var(--text-tertiary);
      cursor: pointer;
      transition: color 0.2s;
      padding: 2px;

      &:hover {
        color: var(--text-secondary);
      }

      &.is-active {
        color: #f56c6c;
      }
    }
  }
}
</style>
