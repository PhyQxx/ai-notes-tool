<template>
  <div class="note-kanban-view">
    <div class="kanban-header">
      <div class="group-selector">
        <span>分组依据：</span>
        <el-select v-model="groupByKey" size="small" style="width: 150px">
          <el-option
            v-for="key in attributeKeys"
            :key="key"
            :label="key"
            :value="key"
          />
        </el-select>
      </div>
    </div>

    <div class="kanban-container">
      <div v-for="column in columns" :key="column.name" class="kanban-column">
        <div class="column-header">
          <span class="column-name">{{ column.name }}</span>
          <span class="column-count">{{ column.notes.length }}</span>
        </div>

        <draggable
          v-model="column.notes"
          group="notes"
          item-key="id"
          class="column-content"
          @change="(e: any) => handleDragChange(e, column.name)"
        >
          <template #item="{ element }">
            <div class="kanban-card" @click="handleCardClick(element)">
              <div class="card-title">{{ element.title }}</div>
              <div class="card-tags" v-if="element.tags">
                <el-tag v-for="tag in parseTags(element.tags)" :key="tag" size="small" effect="plain">
                  {{ tag }}
                </el-tag>
              </div>
              <div class="card-meta">
                <span>{{ formatTime(element.updatedAt) }}</span>
              </div>
            </div>
          </template>
        </draggable>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import draggable from 'vuedraggable';
import type { Note } from '@/types';
import { useNoteStore } from '@/stores/note';

const props = defineProps<{
  notes: Note[];
}>();

const emit = defineEmits<{
  'open': [id: number];
  'update-note': [id: number, data: any];
}>();

const noteStore = useNoteStore();
const groupByKey = ref('状态'); // 默认按状态分组

const attributeKeys = computed(() => {
  const keys = new Set<string>();
  props.notes.forEach(note => {
    if (note.customAttributes) {
      Object.keys(note.customAttributes).forEach(k => keys.add(k));
    }
  });
  if (keys.size === 0) return ['状态'];
  return Array.from(keys);
});

interface KanbanColumn {
  name: string;
  notes: Note[];
}

const columns = ref<KanbanColumn[]>([]);

const initColumns = () => {
  const groups: Record<string, Note[]> = { '未分类': [] };
  
  // 1. 获取该属性的所有可能值，并分组
  props.notes.forEach(note => {
    const val = note.customAttributes?.[groupByKey.value] || '未分类';
    if (!groups[val]) groups[val] = [];
    groups[val].push(note);
  });

  // 2. 转换为列数组
  const cols = Object.keys(groups).map(name => ({
    name,
    notes: groups[name]
  }));

  // 排序：未分类放在最后，其他按字母
  cols.sort((a, b) => {
    if (a.name === '未分类') return 1;
    if (b.name === '未分类') return -1;
    return a.name.localeCompare(b.name);
  });

  columns.value = cols;
};

// 初始化和监听数据变化
watch(() => props.notes, initColumns, { immediate: true, deep: true });
watch(groupByKey, initColumns);

const handleDragChange = async (event: any, newColumnName: string) => {
  if (event.added) {
    const note = event.added.element;
    const noteId = note.id;
    
    // 更新属性
    const attributes = { ...(note.customAttributes || {}) };
    attributes[groupByKey.value] = newColumnName;
    
    try {
      await noteStore.updateNote(noteId, { customAttributes: attributes });
      emit('update-note', noteId, { customAttributes: attributes });
    } catch (e) {
      console.error('更新分组属性失败', e);
      initColumns(); // 失败则回滚
    }
  }
};

const handleCardClick = (note: Note) => {
  emit('open', note.id);
};

const parseTags = (tags: any) => {
  if (Array.isArray(tags)) return tags;
  if (typeof tags === 'string') return tags.split(',').filter(Boolean);
  return [];
};

const formatTime = (t: string) => {
  return new Date(t).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' });
};
</script>

<style scoped lang="scss">
.note-kanban-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;

  .kanban-header {
    padding: 0 0 12px 0;
    display: flex;
    align-items: center;
    
    .group-selector {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .kanban-container {
    flex: 1;
    display: flex;
    gap: 16px;
    overflow-x: auto;
    padding-bottom: 16px;
    align-items: flex-start;

    &::-webkit-scrollbar {
      height: 6px;
    }
  }

  .kanban-column {
    flex: 0 0 300px;
    background-color: var(--el-fill-color-light);
    border-radius: 12px;
    display: flex;
    flex-direction: column;
    max-height: 100%;
    border: 1px solid var(--el-border-color-lighter);

    .column-header {
      padding: 12px 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .column-name {
        font-weight: 600;
        font-size: 14px;
        color: var(--el-text-color-primary);
      }

      .column-count {
        background: var(--el-fill-color-darker);
        color: var(--el-text-color-secondary);
        font-size: 11px;
        padding: 2px 8px;
        border-radius: 10px;
      }
    }

    .column-content {
      flex: 1;
      padding: 8px;
      overflow-y: auto;
      min-height: 100px;

      &::-webkit-scrollbar {
        width: 4px;
      }
    }
  }

  .kanban-card {
    background-color: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    padding: 12px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 2px 4px rgba(0,0,0,0.02);

    &:hover {
      border-color: var(--el-color-primary);
      box-shadow: 0 4px 12px rgba(0,0,0,0.05);
      transform: translateY(-2px);
    }

    .card-title {
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 8px;
      color: var(--el-text-color-primary);
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .card-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
      margin-bottom: 8px;
    }

    .card-meta {
      display: flex;
      justify-content: flex-end;
      font-size: 11px;
      color: var(--el-text-color-placeholder);
    }
  }
}
</style>
