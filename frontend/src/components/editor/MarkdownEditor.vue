<template>
  <div ref="editorRef" class="markdown-editor"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import Vditor from 'vditor';
import 'vditor/dist/index.css';
import { ElMessage } from 'element-plus';
import { useThemeStore } from '@/stores/theme';

const props = defineProps<{
  modelValue: string;
  height?: string;
  noteId?: number;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
  save: [];
}>();

const editorRef = ref<HTMLElement>();
let vditor: Vditor | null = null;
const themeStore = useThemeStore();

const uploadImage = async (file: File): Promise<string> => {
  const token = localStorage.getItem('token');
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch('/api/files/upload/image', {
    method: 'POST',
    headers: { 'Authorization': token ? `Bearer ${token}` : '' },
    body: formData
  });

  if (!response.ok) throw new Error('上传失败');
  const result = await response.json();
  if (result.code === 200 && result.data?.url) {
    return result.data.url;
  }
  throw new Error('上传失败');
};

const initEditor = () => {
  vditor = new Vditor(editorRef.value!, {
    height: props.height,
    value: props.modelValue,
    mode: 'wysiwyg',
    cache: { enable: false },
    cdn: '/vditor',
    upload: {
      accept: 'image/*',
      url: '/api/files/upload/image',
      headers: (() => {
        const token = localStorage.getItem('token');
        return token ? { Authorization: `Bearer ${token}` } : {};
      })(),
      success: (editor: Vditor, msg: string) => {
        try {
          const result = JSON.parse(msg);
          if (result.code === 200 && result.data?.url) {
            // Vditor handles insertion automatically on success
          } else {
            ElMessage.error(result.message || '上传失败');
          }
        } catch {
          ElMessage.error('上传失败');
        }
      },
      error: () => {
        ElMessage.error('图片上传失败');
      }
    },
    theme: themeStore.getEffectiveTheme() === 'dark' ? 'dark' : 'classic',
    toolbar: [
      'headings', 'bold', 'italic', 'strike', '|',
      'line', 'quote', 'list', 'ordered-list', 'check', '|',
      'code', 'inline-code', 'table', '|',
      'link', 'upload', '|',
      'undo', 'redo', '|',
      'edit-mode', 'outline', 'preview', 'fullscreen', 'export'
    ],
    toolbarConfig: {
      pin: true
    },
    placeholder: '开始编写您的笔记...',
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
      theme: {
        current: themeStore.getEffectiveTheme() === 'dark' ? 'dark' : 'light',
      },
      mermaid: {
        theme: themeStore.getEffectiveTheme() === 'dark' ? 'dark' : 'default',
      },
    },
    after: () => {
      if (vditor) {
        vditor.setValue(props.modelValue);
      }
    },
    input: (value) => {
      emit('update:modelValue', value);
    },
    ctrlEnter: () => {
      emit('save');
    },
    // @提及支持 - Ctrl+K 插入链接
    keydown: (event: KeyboardEvent) => {
      // Ctrl+K: 插入链接
      if (event.ctrlKey && event.key === 'k') {
        event.preventDefault();
        if (vditor) {
          const link = prompt('请输入链接地址:');
          if (link) {
            vditor.insertValue(`[${link}](${link})`);
          }
        }
        return true;
      }
      return false;
    }
  });
};

onMounted(() => {
  initEditor();
});

onBeforeUnmount(() => {
  if (vditor) {
    vditor.destroy();
    vditor = null;
  }
});

watch(() => props.modelValue, (newVal) => {
  if (vditor && vditor.getValue() !== newVal) {
    vditor.setValue(newVal);
  }
});

// 监听主题变化，更新 Vditor 主题
watch(() => themeStore.mode, () => {
  if (!vditor) return;
  const isDark = themeStore.getEffectiveTheme() === 'dark';
  vditor.setTheme(isDark ? 'dark' : 'classic', isDark ? 'dark' : 'light', isDark ? 'github-dark' : 'github');
});
</script>

<style scoped>
.markdown-editor {
  width: 100%;
}
</style>
