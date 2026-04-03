<template>
  <el-dialog v-model="visible" title="选择模板" width="700px" :close-on-click-modal="false">
    <div class="template-search">
      <el-input v-model="keyword" placeholder="搜索模板..." clearable prefix-icon="Search" />
    </div>

    <div class="template-grid">
      <div
        v-for="t in filteredTemplates"
        :key="t.id"
        class="template-card"
        :class="{ active: selectedId === t.id }"
        @click="selectedId = t.id"
      >
        <div class="template-icon">{{ t.icon }}</div>
        <div class="template-info">
          <div class="template-name">
            {{ t.name }}
            <el-tag v-if="t.category === 'system'" size="small" type="info">系统</el-tag>
          </div>
          <div class="template-preview">{{ previewText(t.content) }}</div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :disabled="!selectedId" @click="handleApply">
        使用模板
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { listTemplates, applyTemplate, type NoteTemplate } from '@/api/template';

const emit = defineEmits<{
  (e: 'apply', content: string): void;
}>();

const visible = ref(false);
const keyword = ref('');
const selectedId = ref<number | null>(null);
const templates = ref<NoteTemplate[]>([]);

const filteredTemplates = computed(() => {
  if (!keyword.value) return templates.value;
  const kw = keyword.value.toLowerCase();
  return templates.value.filter(t => t.name.toLowerCase().includes(kw));
});

function previewText(content: string): string {
  return content.replace(/[#*>\-|]/g, '').replace(/\{\{[^}]+\}\}/g, '...').trim().slice(0, 60) + '...';
}

async function loadTemplates() {
  try {
    templates.value = await listTemplates();
  } catch {
    templates.value = [];
  }
}

async function handleApply() {
  if (!selectedId.value) return;
  try {
    const res = await applyTemplate(selectedId.value);
    emit('apply', res.content);
    visible.value = false;
    selectedId.value = null;
  } catch {
    // error handled by interceptor
  }
}

watch(visible, val => {
  if (val) {
    keyword.value = '';
    selectedId.value = null;
    loadTemplates();
  }
});

defineExpose({ open: () => { visible.value = true; } });
</script>

<style scoped>
.template-search {
  margin-bottom: 16px;
}
.template-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}
.template-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.template-card:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.template-card.active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: 0 0 0 1px var(--el-color-primary);
}
.template-icon {
  font-size: 28px;
  line-height: 1;
  flex-shrink: 0;
}
.template-info {
  min-width: 0;
  flex: 1;
}
.template-name {
  font-weight: 500;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.template-preview {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
