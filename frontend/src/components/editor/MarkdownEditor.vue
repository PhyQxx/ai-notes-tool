<template>
  <div ref="editorRef" class="markdown-editor"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import Vditor from 'vditor';
import 'vditor/dist/index.css';
import { ElMessage } from 'element-plus';

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
    theme: 'classic',
    toolbar: [
      'headings', 'bold', 'italic', 'strike', '|',
      'line', 'quote', 'list', 'ordered-list', 'check', '|',
      'code', 'inline-code', '|',
      'link', 'table', '|',
      'upload', '|',
      'undo', 'redo', '|',
      'preview', 'fullscreen', 'export'
    ],
    toolbarConfig: {
      pin: true
    },
    placeholder: '开始编写您的笔记...',
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
</script>

<style scoped>
.markdown-editor {
  width: 100%;
}
</style>
