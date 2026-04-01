<template>
  <div class="markdown-renderer" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  content: string;
}>();

// 简单的Markdown渲染器
const renderedContent = computed(() => {
  if (!props.content) return '';

  let html = props.content;

  // 转义HTML
  html = html.replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');

  // 代码块
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>');

  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>');

  // 粗体
  html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');

  // 斜体
  html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');

  // 标题
  html = html.replace(/^### (.+)$/gm, '<h3>$1</h3>');
  html = html.replace(/^## (.+)$/gm, '<h2>$1</h2>');
  html = html.replace(/^# (.+)$/gm, '<h1>$1</h1>');

  // 列表
  html = html.replace(/^- (.+)$/gm, '<li>$1</li>');
  html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>');

  // 链接
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>');

  // 换行
  html = html.replace(/\n/g, '<br>');

  return html;
});
</script>

<style scoped lang="scss">
.markdown-renderer {
  line-height: 1.6;

  :deep(h1),
  :deep(h2),
  :deep(h3) {
    margin-top: 16px;
    margin-bottom: 8px;
    font-weight: 600;
  }

  :deep(h1) {
    font-size: 24px;
  }

  :deep(h2) {
    font-size: 20px;
  }

  :deep(h3) {
    font-size: 16px;
  }

  :deep(p) {
    margin-bottom: 12px;
  }

  :deep(ul) {
    margin: 8px 0;
    padding-left: 24px;
  }

  :deep(li) {
    margin-bottom: 4px;
  }

  :deep(code) {
    background-color: var(--el-fill-color-light);
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 0.9em;
  }

  :deep(pre) {
    background-color: #f5f5f5;
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 12px 0;

    code {
      background-color: transparent;
      padding: 0;
    }
  }

  :deep(a) {
    color: var(--el-color-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(blockquote) {
    border-left: 4px solid var(--el-color-primary);
    padding-left: 16px;
    margin: 12px 0;
    color: var(--el-text-color-secondary);
  }
}
</style>
