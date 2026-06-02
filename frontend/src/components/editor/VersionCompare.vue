<template>
  <div class="version-compare">
    <div class="compare-header">
      <div class="stats">
        <el-tag type="success">添加: {{ stats.added }}</el-tag>
        <el-tag type="danger">删除: {{ stats.removed }}</el-tag>
      </div>
      <div class="legend">
        <span class="legend-item"><i class="bg-added"></i> 新增</span>
        <span class="legend-item"><i class="bg-removed"></i> 删除</span>
      </div>
    </div>
    <div class="compare-content" ref="scrollContainer">
      <div
        v-for="(line, index) in diffLines"
        :key="index"
        :class="['diff-line', line.type]"
      >
        <div class="line-numbers">
          <span class="old-num">{{ line.oldLine || '' }}</span>
          <span class="new-num">{{ line.newLine || '' }}</span>
        </div>
        <span class="line-marker">{{ line.marker }}</span>
        <span class="line-content">{{ line.content }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { diffLines as getDiffLines } from 'diff';

const props = defineProps<{
  oldContent: string;
  newContent: string;
}>();

interface DiffLine {
  type: 'added' | 'removed' | 'unchanged';
  oldLine?: number;
  newLine?: number;
  marker: string;
  content: string;
}

const scrollContainer = ref<HTMLElement | null>(null);

const diffLines = computed(() => {
  const changes = getDiffLines(props.oldContent || '', props.newContent || '');
  const lines: DiffLine[] = [];
  
  let oldLineCount = 1;
  let newLineCount = 1;

  changes.forEach(change => {
    const changeLines = change.value.split('\n');
    // If the last character is a newline, split will create an extra empty string
    if (changeLines[changeLines.length - 1] === '') {
      changeLines.pop();
    }

    changeLines.forEach(lineContent => {
      if (change.added) {
        lines.push({
          type: 'added',
          newLine: newLineCount++,
          marker: '+',
          content: lineContent
        });
      } else if (change.removed) {
        lines.push({
          type: 'removed',
          oldLine: oldLineCount++,
          marker: '-',
          content: lineContent
        });
      } else {
        lines.push({
          type: 'unchanged',
          oldLine: oldLineCount++,
          newLine: newLineCount++,
          marker: ' ',
          content: lineContent
        });
      }
    });
  });

  return lines;
});

const stats = computed(() => {
  let added = 0;
  let removed = 0;
  diffLines.value.forEach(line => {
    if (line.type === 'added') added++;
    if (line.type === 'removed') removed++;
  });
  return { added, removed };
});
</script>

<style scoped lang="scss">
.version-compare {
  display: flex;
  flex-direction: column;
  height: 100%;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;

  .compare-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background-color: var(--el-fill-color-extra-light);
    border-bottom: 1px solid var(--el-border-color-lighter);

    .stats {
      display: flex;
      gap: 8px;
    }

    .legend {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: var(--el-text-color-secondary);

      .legend-item {
        display: flex;
        align-items: center;
        gap: 4px;

        i {
          width: 12px;
          height: 12px;
          border-radius: 2px;
          
          &.bg-added { background-color: var(--el-color-success-light-8); }
          &.bg-removed { background-color: var(--el-color-danger-light-8); }
        }
      }
    }
  }

  .compare-content {
    flex: 1;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 13px;
    line-height: 20px;
    background-color: var(--el-bg-color);
    overflow-y: auto;
    padding: 8px 0;

    .diff-line {
      display: flex;
      padding: 0 16px;
      white-space: pre-wrap;
      word-break: break-all;

      &:hover {
        background-color: var(--el-fill-color-light);
      }

      .line-numbers {
        display: flex;
        flex-shrink: 0;
        width: 80px;
        margin-right: 12px;
        color: var(--el-text-color-placeholder);
        user-select: none;
        text-align: right;

        .old-num, .new-num {
          width: 40px;
          padding-right: 8px;
        }
      }

      .line-marker {
        flex-shrink: 0;
        width: 20px;
        user-select: none;
        color: var(--el-text-color-secondary);
      }

      .line-content {
        flex: 1;
      }

      &.added {
        background-color: var(--el-color-success-light-9);
        .line-content, .line-marker { color: var(--el-color-success-dark-2); }
        .new-num { color: var(--el-color-success); }
      }

      &.removed {
        background-color: var(--el-color-danger-light-9);
        .line-content, .line-marker { color: var(--el-color-danger-dark-2); }
        .old-num { color: var(--el-color-danger); }
      }

      &.unchanged {
        color: var(--el-text-color-regular);
      }
    }
  }
}
</style>

