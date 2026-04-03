<template>
  <div class="tag-filter-panel">
    <div class="panel-header">
      <span class="panel-title">标签筛选</span>
      <el-button v-if="selectedTags.length > 0" link type="danger" @click="clearAll">清除</el-button>
    </div>
    <div class="tag-list">
      <el-check-tag
        v-for="tag in tags"
        :key="tag.name"
        :checked="selectedTags.includes(tag.name)"
        :style="selectedTags.includes(tag.name) ? { color: getColor(tag.name), borderColor: getColor(tag.name) } : {}"
        @change="toggleTag(tag.name)"
      >
        {{ tag.name }}
        <sup>{{ tag.count }}</sup>
      </el-check-tag>
      <el-empty v-if="tags.length === 0" description="暂无标签" :image-size="40" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import type { TagCloudItem, TagColor } from '@/api/tag';

const props = defineProps<{
  tags: TagCloudItem[];
  customColors?: TagColor[];
  modelValue?: string[];
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', tags: string[]): void;
}>();

const selectedTags = ref<string[]>(props.modelValue || []);

watch(() => props.modelValue, (v) => {
  if (v) selectedTags.value = v;
});

function toggleTag(name: string) {
  const idx = selectedTags.value.indexOf(name);
  if (idx >= 0) selectedTags.value.splice(idx, 1);
  else selectedTags.value.push(name);
  emit('update:modelValue', [...selectedTags.value]);
}

function clearAll() {
  selectedTags.value = [];
  emit('update:modelValue', []);
}

function hashColor(name: string): string {
  let hash = 0;
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return `hsl(${Math.abs(hash) % 360}, 65%, 45%)`;
}

function getColor(name: string): string {
  return props.customColors?.find(c => c.name === name)?.color || hashColor(name);
}
</script>

<style scoped lang="scss">
.tag-filter-panel {
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    .panel-title { font-weight: 600; font-size: 14px; }
  }
  .tag-list {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    sup { font-size: 11px; opacity: 0.7; }
  }
}
</style>
