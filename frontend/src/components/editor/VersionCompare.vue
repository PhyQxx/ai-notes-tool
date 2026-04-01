<template>
  <div class="version-compare">
    <div class="compare-header">
      <el-tag type="success">新增</el-tag>
      <el-tag type="danger">删除</el-tag>
    </div>
    <div class="compare-content">
      <div
        v-for="(line, index) in diffLines"
        :key="index"
        :class="['diff-line', line.type]"
      >
        <span class="line-number">{{ line.lineNumber }}</span>
        <span class="line-content" v-html="line.content"></span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  oldContent: string;
  newContent: string;
}>();

interface DiffLine {
  type: 'added' | 'removed' | 'unchanged' | 'empty';
  lineNumber: string;
  content: string;
}

const diffLines = computed<DiffLine[]>(() => {
  const oldLines = props.oldContent.split('\n');
  const newLines = props.newContent.split('\n');
  const result: DiffLine[] = [];

  // 简单的逐行对比算法
  const maxLines = Math.max(oldLines.length, newLines.length);

  for (let i = 0; i < maxLines; i++) {
    const oldLine = oldLines[i] || '';
    const newLine = newLines[i] || '';

    if (oldLine === newLine) {
      // 内容相同
      result.push({
        type: 'unchanged',
        lineNumber: (i + 1).toString(),
        content: escapeHtml(newLine)
      });
    } else if (!oldLine && newLine) {
      // 新增行
      result.push({
        type: 'added',
        lineNumber: '+',
        content: `<el-tag size="small" type="success">新增</el-tag> ${escapeHtml(newLine)}`
      });
    } else if (oldLine && !newLine) {
      // 删除行
      result.push({
        type: 'removed',
        lineNumber: '-',
        content: `<el-tag size="small" type="danger">删除</el-tag> ${escapeHtml(oldLine)}`
      });
    } else {
      // 内容修改 - 删除旧行，添加新行
      result.push({
        type: 'removed',
        lineNumber: '-',
        content: `<el-tag size="small" type="danger">删除</el-tag> ${escapeHtml(oldLine)}`
      });
      result.push({
        type: 'added',
        lineNumber: '+',
        content: `<el-tag size="small" type="success">新增</el-tag> ${escapeHtml(newLine)}`
      });
    }
  }

  return result;
});

const escapeHtml = (text: string) => {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
};
</script>

<style scoped lang="scss">
.version-compare {
  .compare-header {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--el-border-color);
  }

  .compare-content {
    font-family: 'Courier New', monospace;
    font-size: 14px;
    line-height: 1.8;
    background-color: var(--el-bg-color-page);
    border-radius: 8px;
    padding: 16px;
    max-height: 500px;
    overflow-y: auto;

    .diff-line {
      display: flex;
      padding: 4px 8px;
      border-radius: 4px;

      &:hover {
        background-color: var(--el-fill-color-light);
      }

      .line-number {
        flex-shrink: 0;
        width: 40px;
        text-align: right;
        color: var(--el-text-color-secondary);
        margin-right: 12px;
        user-select: none;
      }

      .line-content {
        flex: 1;
        min-width: 0;
        white-space: pre-wrap;
        word-break: break-word;
      }

      &.added {
        background-color: var(--el-color-success-light-9);

        .line-number {
          color: var(--el-color-success);
        }
      }

      &.removed {
        background-color: var(--el-color-danger-light-9);

        .line-number {
          color: var(--el-color-danger);
        }
      }

      &.unchanged {
        color: var(--el-text-color-regular);
      }

      &.empty {
        height: 4px;
        padding: 0;
      }
    }
  }
}
</style>
