<template>
  <div class="note-table-view">
    <el-table :data="notes" stripe border style="width: 100%" @row-click="handleRowClick">
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      
      <!-- 动态显示所有存在的自定义属性列 -->
      <el-table-column
        v-for="attr in allAttributeKeys"
        :key="attr"
        :label="attr"
        min-width="120"
      >
        <template #default="{ row }">
          <el-tag v-if="isStatus(attr, row.customAttributes?.[attr])" :type="getStatusType(row.customAttributes?.[attr])" size="small">
            {{ row.customAttributes?.[attr] }}
          </el-tag>
          <span v-else>{{ row.customAttributes?.[attr] || '-' }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="updatedAt" label="更新时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.updatedAt) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Note } from '@/types';

const props = defineProps<{
  notes: Note[];
}>();

const emit = defineEmits<{
  'open': [id: number];
}>();

const allAttributeKeys = computed(() => {
  const keys = new Set<string>();
  props.notes.forEach(note => {
    if (note.customAttributes) {
      Object.keys(note.customAttributes).forEach(k => keys.add(k));
    }
  });
  return Array.from(keys);
});

const isStatus = (key: string, value: any) => {
  if (!value) return false;
  return key.toLowerCase().includes('status') || key.toLowerCase().includes('状态');
};

const getStatusType = (status: string) => {
  if (status === 'Completed' || status === '已完成') return 'success';
  if (status === 'In Progress' || status === '进行中') return 'primary';
  if (status === 'Not Started' || status === '未开始') return 'info';
  return 'info';
};

const handleRowClick = (row: Note) => {
  emit('open', row.id);
};

const formatTime = (t: string) => {
  if (!t) return '-';
  return new Date(t).toLocaleString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' });
};
</script>

<style scoped lang="scss">
.note-table-view {
  background: var(--el-bg-color);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: var(--el-box-shadow-light);

  :deep(.el-table__row) {
    cursor: pointer;
  }
}
</style>
