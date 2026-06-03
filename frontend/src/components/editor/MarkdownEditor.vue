<template>
  <div class="markdown-editor-wrapper">
    <div ref="editorRef" class="markdown-editor"></div>
    
    <!-- 保存状态指示器 -->
    <div class="save-status" :class="{ offline: isOffline }">
      <el-icon v-if="isSaving" class="is-loading"><Loading /></el-icon>
      <el-icon v-else-if="isOffline"><Cloudy /></el-icon>
      <el-icon v-else><CircleCheck /></el-icon>
      <span>{{ statusText }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, computed } from 'vue';
import Vditor from 'vditor';
import 'vditor/dist/index.css';
import { ElMessage } from 'element-plus';
import { Loading, CircleCheck, Cloudy } from '@element-plus/icons-vue';
import { useThemeStore } from '@/stores/theme';
import * as Y from 'yjs';
import { YjsProvider } from '@/utils/yjs-provider';
import { aiSummarize, extractTasks, ocr } from '@/api/ai';
import { searchTitles } from '@/api/noteLink';
import { getToken } from '@/utils/storage';

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

const isSaving = ref(false);
const isOffline = ref(!navigator.onLine);

const statusText = computed(() => {
  if (isOffline.value) return '已保存至本地';
  if (isSaving.value) return '正在同步...';
  return '已同步云端';
});

const ydoc = new Y.Doc();
const ytext = ydoc.getText('markdown');
let provider: YjsProvider | null = null;
let isRemoteChange = false;

const initEditor = () => {
  vditor = new Vditor(editorRef.value!, {
    height: props.height,
    value: props.modelValue,
    mode: 'wysiwyg',
    cache: { enable: false },
    cdn: '/vditor',
    upload: {
      accept: 'image/*',
      handler: async (files: File[]) => {
        const file = files[0];
        if (!file) return;
        
        try {
          const compressedFile = await compressImage(file);
          const formData = new FormData();
          formData.append('file', compressedFile);
          
          const loading = ElMessage({
            message: '图片上传中...',
            type: 'info',
            duration: 0
          });
          
          const token = getToken();
          const response = await fetch('/api/files/upload/image', {
            method: 'POST',
            headers: { 'Authorization': token ? `Bearer ${token}` : '' },
            body: formData
          });
          
          const result = await response.json();
          loading.close();
          
          if (result.code === 200 && result.data?.url) {
            const url = result.data.url;
            const name = file.name;
            vditor?.insertValue(`![${name}](${url})`);
          } else {
            ElMessage.error(result.message || '上传失败');
          }
        } catch (error) {
          console.error('图片上传失败:', error);
          ElMessage.error('图片上传失败');
        }
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
    hint: {
      extend: [
        {
          key: '/',
          hint: (value: string) => {
            const commands = [
              { value: '# ', html: '一级标题 (H1)' },
              { value: '## ', html: '二级标题 (H2)' },
              { value: '### ', html: '三级标题 (H3)' },
              { value: '* ', html: '无序列表' },
              { value: '1. ', html: '有序列表' },
              { value: '> ', html: '引用' },
              { value: '```\n\n```', html: '代码块' },
              { value: '[]()', html: '链接' },
              { value: '[toc]\n', html: '插入目录 (TOC)' },
              { value: 'ai-summarize', html: '✨ AI 总结' },
              { value: 'ai-tasks', html: '✅ AI 提取待办' },
              { value: 'ai-ocr', html: '🖼️ AI OCR 识图' }
            ];
            return commands.filter(c => c.html.toLowerCase().includes(value.toLowerCase()));
          }
        },
        {
          key: '[[',
          hint: async (value: string) => {
            try {
              const notes = await searchTitles(value);
              return notes.map(n => ({
                value: `[[${n.title}]] `,
                html: `📄 ${n.title}`
              }));
            } catch (e) {
              return [];
            }
          }
        }
      ],
      select: (value: string) => {
        if (value === 'ai-summarize') {
          handleAISummarize();
          return '';
        }
        if (value === 'ai-tasks') {
          handleAIExtractTasks();
          return '';
        }
        if (value === 'ai-ocr') {
          handleAIOCR();
          return '';
        }
        return value;
      }
    },
    placeholder: '输入 / 唤起快捷菜单...',
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
      if (!isRemoteChange) {
        const currentText = ytext.toString();
        if (value !== currentText) {
          ydoc.transact(() => {
            ytext.delete(0, ytext.length);
            ytext.insert(0, value);
          });
        }
      }
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

const handleAISummarize = async () => {
  if (!vditor) return;
  const content = vditor.getValue();
  if (!content) {
    ElMessage.warning('笔记内容为空，无法总结');
    return;
  }

  try {
    const loading = ElMessage({
      message: 'AI 总结生成中...',
      type: 'info',
      duration: 0
    });
    
    const res = await aiSummarize(props.noteId, content);
    loading.close();
    
    // Insert at the end
    vditor.insertValue(`\n\n> **AI 总结：**\n> ${res.data}\n\n`);
    ElMessage.success('总结生成成功');
  } catch (error) {
    ElMessage.error('AI 总结失败');
  }
};

const handleAIExtractTasks = async () => {
  if (!vditor) return;
  const content = vditor.getValue();
  if (!content) {
    ElMessage.warning('笔记内容为空，无法提取待办');
    return;
  }

  try {
    const loading = ElMessage({
      message: 'AI 待办提取中...',
      type: 'info',
      duration: 0
    });
    
    const tasks = await extractTasks(content);
    loading.close();
    
    if (tasks && tasks.length > 0) {
      const taskStr = tasks.map(t => `- [ ] ${t}`).join('\n');
      vditor.insertValue(`\n\n### ✅ AI 提取的待办事项\n${taskStr}\n\n`);
      ElMessage.success('待办提取成功');
    } else {
      ElMessage.info('未发现明显的待办事项');
    }
  } catch (error) {
    ElMessage.error('待办提取失败');
  }
};

const handleAIOCR = async () => {
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.onchange = async (e: any) => {
    const file = e.target.files[0];
    if (!file) return;
    
    const loading = ElMessage({
      message: 'AI OCR 识图中...',
      type: 'info',
      duration: 0
    });
    
    try {
      const res = await ocr(file);
      loading.close();
      if (res.data) {
        vditor?.insertValue(`\n\n### 🖼️ OCR 识图结果\n${res.data}\n\n`);
        ElMessage.success('识图成功');
      }
    } catch (error) {
      loading.close();
      ElMessage.error('识图失败');
    }
  };
  input.click();
};

const compressImage = (file: File): Promise<File> => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (event) => {
      const img = new Image();
      img.src = event.target?.result as string;
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        const maxWidth = 1200;
        const maxHeight = 1200;
        let width = img.width;
        let height = img.height;

        if (width > maxWidth || height > maxHeight) {
          if (width > height) {
            height = (height * maxWidth) / width;
            width = maxWidth;
          } else {
            width = (width * maxHeight) / height;
            height = maxHeight;
          }
        }

        canvas.width = width;
        canvas.height = height;
        ctx?.drawImage(img, 0, 0, width, height);
        
        canvas.toBlob((blob) => {
          if (blob) {
            resolve(new File([blob], file.name, { type: 'image/jpeg' }));
          } else {
            resolve(file);
          }
        }, 'image/jpeg', 0.8);
      };
    };
  });
};

onMounted(() => {
  initEditor();

  if (props.noteId) {
    provider = new YjsProvider(props.noteId, ydoc);
  }

  ytext.observe(event => {
    if (vditor && !event.transaction.local) {
      isRemoteChange = true;
      const newValue = ytext.toString();
      if (vditor.getValue() !== newValue) {
        vditor.setValue(newValue);
      }
      isRemoteChange = false;
    }
  });

  window.addEventListener('ai:insert-content', handleAIInsert);
});

onBeforeUnmount(() => {
  window.removeEventListener('ai:insert-content', handleAIInsert);
  vditor?.destroy();
});

const handleAIInsert = (e: any) => {
  const content = e.detail?.content;
  if (content && vditor) {
    vditor.insertValue('\n' + content + '\n');
    ElMessage.success('内容已插入编辑器');
  }
};

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

<style scoped lang="scss">
.markdown-editor-wrapper {
  position: relative;
  height: 100%;
}

.markdown-editor {
  width: 100%;
}

.save-status {
  position: absolute;
  bottom: 16px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 20px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  box-shadow: var(--el-box-shadow-light);
  z-index: 10;
  pointer-events: none;
  opacity: 0.8;
  transition: all 0.3s;

  .el-icon {
    font-size: 14px;
    color: var(--el-color-success);
  }

  &.offline {
    color: var(--el-color-warning);
    .el-icon { color: var(--el-color-warning); }
  }
}
</style>
