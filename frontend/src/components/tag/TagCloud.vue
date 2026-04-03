<template>
  <div class="tag-cloud">
    <span
      v-for="tag in tags"
      :key="tag.name"
      class="tag-item"
      :style="{
        fontSize: getFontSize(tag.count) + 'px',
        color: getColor(tag.name),
        borderColor: getColor(tag.name),
      }"
      @click="$emit('select', tag.name)"
    >
      {{ tag.name }}
      <sup class="tag-count">{{ tag.count }}</sup>
    </span>
    <el-empty v-if="tags.length === 0" description="暂无标签" :image-size="40" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { TagCloudItem, TagColor } from '@/api/tag';

const props = defineProps<{
  tags: TagCloudItem[];
  customColors?: TagColor[];
}>();

defineEmits<{
  (e: 'select', name: string): void;
}>();

const maxCount = computed(() => Math.max(...props.tags.map(t => t.count), 1));

function getFontSize(count: number): number {
  const min = 13, max = 28;
  return min + ((count / maxCount.value) * (max - min));
}

function hashColor(name: string): string {
  let hash = 0;
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash);
  }
  const h = Math.abs(hash) % 360;
  return `hsl(${h}, 65%, 45%)`;
}

function getColor(name: string): string {
  const custom = props.customColors?.find(c => c.name === name);
  return custom?.color || hashColor(name);
}
</script>

<style scoped lang="scss">
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  padding: 8px 0;

  .tag-item {
    display: inline-flex;
    align-items: baseline;
    padding: 4px 10px;
    border-radius: 14px;
    border: 1px solid;
    cursor: pointer;
    transition: all 0.2s;
    background: color-mix(in srgb, currentColor 8%, transparent);

    &:hover {
      transform: scale(1.08);
      box-shadow: 0 2px 8px color-mix(in srgb, currentColor 30%, transparent);
    }

    .tag-count {
      font-size: 11px;
      margin-left: 3px;
      opacity: 0.7;
    }
  }
}
</style>
