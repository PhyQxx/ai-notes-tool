<template>
  <div ref="editorRef" class="markdown-editor"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import Vditor from 'vditor';
import 'vditor/dist/index.css';

const props = defineProps<{
  modelValue: string;
  height?: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
  save: [];
}>();

const editorRef = ref<HTMLElement>();
let vditor: Vditor | null = null;

const initEditor = () => {
  vditor = new Vditor(editorRef.value!, {
    height: props.height,
    value: props.modelValue,
    mode: 'wysiwyg',
    cache: {
      enable: false
    },
    upload: {
      handler: async (files) => {
        // TODO: 实现图片上传
        const result = files.map((file) => {
          return {
            name: file.name,
            url: URL.createObjectURL(file)
          };
        });
        return JSON.stringify(result);
      }
    },
    theme: 'classic',
    toolbar: [
      'headings',
      'bold',
      'italic',
      'strike',
      '|',
      'line',
      'quote',
      'list',
      'ordered-list',
      'check',
      '|',
      'code',
      'inline-code',
      '|',
      'link',
      'table',
      '|',
      'upload',
      'preview',
      'fullscreen',
      'export'
    ],
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
    }
  });
};

watch(() => props.modelValue, (newValue) => {
  if (vditor && vditor.getValue() !== newValue) {
    vditor.setValue(newValue);
  }
});

onMounted(() => {
  initEditor();
});

onBeforeUnmount(() => {
  if (vditor) {
    vditor.destroy();
    vditor = null;
  }
});
</script>

<style scoped lang="scss">
.markdown-editor {
  width: 100%;

  :deep(.vditor) {
    border: none;
    background-color: var(--el-bg-color);
  }
}
</style>
