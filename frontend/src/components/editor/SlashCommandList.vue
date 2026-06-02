<template>
  <div class="slash-command-list" v-if="items.length">
    <button
      v-for="(item, index) in items"
      :key="index"
      :class="{ 'is-selected': index === selectedIndex }"
      @click="selectItem(index)"
    >
      <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
      <span>{{ item.title }}</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
  items: any[];
  command: (item: any) => void;
}>();

const selectedIndex = ref(0);

const selectItem = (index: number) => {
  const item = props.items[index];
  if (item) {
    props.command(item);
  }
};

const onKeyDown = ({ event }: { event: KeyboardEvent }) => {
  if (event.key === 'ArrowUp') {
    selectedIndex.value = (selectedIndex.value + props.items.length - 1) % props.items.length;
    return true;
  }
  if (event.key === 'ArrowDown') {
    selectedIndex.value = (selectedIndex.value + 1) % props.items.length;
    return true;
  }
  if (event.key === 'Enter') {
    selectItem(selectedIndex.value);
    return true;
  }
  return false;
};

defineExpose({
  onKeyDown,
});

watch(() => props.items, () => {
  selectedIndex.value = 0;
});
</script>

<style scoped lang="scss">
.slash-command-list {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  box-shadow: var(--el-box-shadow-light);
  display: flex;
  flex-direction: column;
  padding: 4px;
  overflow: hidden;
  min-width: 160px;

  button {
    background: none;
    border: none;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    text-align: left;
    width: 100%;
    cursor: pointer;
    font-size: 14px;
    color: var(--el-text-color-primary);
    border-radius: 4px;

    &:hover, &.is-selected {
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
    }

    .el-icon {
      font-size: 16px;
    }
  }
}
</style>
