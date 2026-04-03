<template>
  <div ref="previewRef" class="markdown-renderer"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import Vditor from 'vditor';
import 'vditor/dist/index.css';
import { useThemeStore } from '@/stores/theme';

const props = defineProps<{
  content: string;
}>();

const previewRef = ref<HTMLElement>();
let vditorPreview: Vditor | null = null;
const themeStore = useThemeStore();

const renderContent = () => {
  if (!previewRef.value || !props.content) {
    if (previewRef.value) previewRef.value.innerHTML = '';
    return;
  }

  if (!vditorPreview) {
    vditorPreview = new Vditor(previewRef.value, {
      cdn: '/vditor',
      mode: 'wysiwyg',
      preview: {
        markdown: {
          mathBlockMarker: '$$',
          toc: true,
          footnotes: true,
        },
        highlight: {
          style: themeStore.getEffectiveTheme() === 'dark' ? 'github-dark' : 'github',
          lineNumber: true,
        },
        mermaid: {
          theme: themeStore.getEffectiveTheme() === 'dark' ? 'dark' : 'default',
        },
      },
      after: () => {
        if (vditorPreview) {
          vditorPreview.setValue(props.content);
          // Switch to preview mode
          vditorPreview.switchMode('sv');
        }
      },
    });
  } else {
    vditorPreview.setValue(props.content);
  }
};

onMounted(() => {
  renderContent();
});

watch(() => props.content, () => {
  renderContent();
});

onBeforeUnmount(() => {
  if (vditorPreview) {
    vditorPreview.destroy();
    vditorPreview = null;
  }
});
</script>

<style scoped lang="scss">
.markdown-renderer {
  line-height: 1.6;
  padding: 8px;
}
</style>
