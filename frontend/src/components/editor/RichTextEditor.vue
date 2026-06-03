<template>
  <div class="rich-text-editor">
    <div class="toolbar">
      <el-button-group>
        <el-tooltip content="加粗" placement="top">
          <el-button
            :type="isActive('bold') ? 'primary' : ''"
            @click="editor?.chain().focus().toggleBold().run()"
          >
            <el-icon><Calendar /></el-icon>
            B
          </el-button>
        </el-tooltip>
        <el-tooltip content="斜体" placement="top">
          <el-button
            :type="isActive('italic') ? 'primary' : ''"
            @click="editor?.chain().focus().toggleItalic().run()"
          >
            <el-icon><Calendar /></el-icon>
            I
          </el-button>
        </el-tooltip>
        <el-tooltip content="下划线" placement="top">
          <el-button
            :type="isActive('underline') ? 'primary' : ''"
            @click="editor?.chain().focus().toggleUnderline().run()"
          >
            <el-icon><Calendar /></el-icon>
            U
          </el-button>
        </el-tooltip>
        <el-tooltip content="删除线" placement="top">
          <el-button
            :type="isActive('strike') ? 'primary' : ''"
            @click="editor?.chain().focus().toggleStrike().run()"
          >
            <el-icon><Calendar /></el-icon>
            S
          </el-button>
        </el-tooltip>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button-group>
        <el-tooltip content="标题1" placement="top">
          <el-button
            :type="isActive('heading', { level: 1 }) ? 'primary' : ''"
            @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()"
          >
            H1
          </el-button>
        </el-tooltip>
        <el-tooltip content="标题2" placement="top">
          <el-button
            :type="isActive('heading', { level: 2 }) ? 'primary' : ''"
            @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
          >
            H2
          </el-button>
        </el-tooltip>
        <el-tooltip content="标题3" placement="top">
          <el-button
            :type="isActive('heading', { level: 3 }) ? 'primary' : ''"
            @click="editor?.chain().focus().toggleHeading({ level: 3 }).run()"
          >
            H3
          </el-button>
        </el-tooltip>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button-group>
        <el-tooltip content="无序列表" placement="top">
          <el-button
            :type="isActive('bulletList') ? 'primary' : ''"
            @click="editor?.chain().focus().toggleBulletList().run()"
          >
            <el-icon><List /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="有序列表" placement="top">
          <el-button
            :type="isActive('orderedList') ? 'primary' : ''"
            @click="editor?.chain().focus().toggleOrderedList().run()"
          >
            <el-icon><Operation /></el-icon>
          </el-button>
        </el-tooltip>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button-group>
        <el-tooltip content="左对齐" placement="top">
          <el-button
            :type="isActive('textAlign', 'left') ? 'primary' : ''"
            @click="editor?.chain().focus().setTextAlign('left').run()"
          >
            <el-icon><DArrowLeft /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="居中" placement="top">
          <el-button
            :type="isActive('textAlign', 'center') ? 'primary' : ''"
            @click="editor?.chain().focus().setTextAlign('center').run()"
          >
            <el-icon><Minus /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="右对齐" placement="top">
          <el-button
            :type="isActive('textAlign', 'right') ? 'primary' : ''"
            @click="editor?.chain().focus().setTextAlign('right').run()"
          >
            <el-icon><DArrowRight /></el-icon>
          </el-button>
        </el-tooltip>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button-group>
        <el-tooltip content="插入图片" placement="top">
          <el-button @click="handleInsertImage">
            <el-icon><Picture /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="插入链接" placement="top">
          <el-button @click="handleInsertLink">
            <el-icon><Link /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="水平线" placement="top">
          <el-button @click="editor?.chain().focus().setHorizontalRule().run()">
            <el-icon><Minus /></el-icon>
          </el-button>
        </el-tooltip>
      </el-button-group>

      <el-divider direction="vertical" />

      <el-button-group>
        <el-tooltip content="撤销" placement="top">
          <el-button
            :disabled="!editor?.can().undo()"
            @click="editor?.chain().focus().undo().run()"
          >
            <el-icon><RefreshLeft /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="重做" placement="top">
          <el-button
            :disabled="!editor?.can().redo()"
            @click="editor?.chain().focus().redo().run()"
          >
            <el-icon><RefreshRight /></el-icon>
          </el-button>
        </el-tooltip>
      </el-button-group>
    </div>

    <div class="editor-content" @click="handleEditorClick">
      <editor-content :editor="editor" />
    </div>

    <!-- 图片上传对话框 -->
    <el-dialog v-model="imageDialogVisible" title="插入图片" width="500px">
      <el-form :model="imageForm" label-width="80px">
        <el-form-item label="图片URL">
          <el-input v-model="imageForm.url" placeholder="输入图片URL" />
        </el-form-item>
        <el-divider>或</el-divider>
        <el-form-item label="上传图片">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :auto-upload="false"
            :on-change="handleImageChange"
            accept="image/*"
          >
            <el-button type="primary">选择图片</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="imageDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmImage" :loading="uploading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 链接对话框 -->
    <el-dialog v-model="linkDialogVisible" title="插入链接" width="500px">
      <el-form :model="linkForm" label-width="80px">
        <el-form-item label="链接文本">
          <el-input v-model="linkForm.text" placeholder="输入链接文本" />
        </el-form-item>
        <el-form-item label="链接URL">
          <el-input v-model="linkForm.url" placeholder="输入链接URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="linkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmLink">确定</el-button>
      </template>
    </el-dialog>

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
import { ref, watch, onBeforeUnmount, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Calendar,
  List,
  Operation,
  DArrowLeft,
  Minus,
  DArrowRight,
  Picture,
  Link as LinkIcon,
  Delete,
  VideoPlay,
  Loading,
  CircleCheck,
  Cloudy,
  MagicStick
} from '@element-plus/icons-vue';
import { useEditor, EditorContent } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';
import Image from '@tiptap/extension-image';
import Link from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';
import Collaboration from '@tiptap/extension-collaboration';
import CollaborationCursor from '@tiptap/extension-collaboration-cursor';
import * as Y from 'yjs';
import { YjsProvider } from '@/utils/yjs-provider';
import { http } from '@/utils/request';
import { useAuthStore } from '@/stores/auth';
import { SlashCommand } from './slashExtension';
import { NoteLink } from './noteLinkExtension';
import 'tippy.js/dist/tippy.css';
import { aiSummarize, extractTasks, ocr } from '@/api/ai';

import { useRouter } from 'vue-router';

const props = defineProps<{
  modelValue: string;
  height?: string;
  noteId?: number;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const authStore = useAuthStore();
const router = useRouter();
const ydoc = new Y.Doc();
let provider: YjsProvider | null = null;

const isSaving = ref(false);
const isOffline = ref(!navigator.onLine);

const statusText = computed(() => {
  if (isOffline.value) return '已保存至本地';
  if (isSaving.value) return '正在同步...';
  return '已同步云端';
});

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit.configure({
      // The history extension is not compatible with the collaboration extension.
      history: false,
    }),
    Underline,
    TextAlign.configure({
      types: ['heading', 'paragraph'],
    }),
    Image,
    Link.configure({
      openOnClick: false,
    }),
    Placeholder.configure({
      placeholder: '输入 / 唤起快捷菜单...',
    }),
    Collaboration.configure({
      document: ydoc,
    }),
    CollaborationCursor.configure({
      provider: provider,
      user: {
        name: authStore.user?.nickname || authStore.user?.username || '匿名用户',
        color: '#' + Math.floor(Math.random() * 16777215).toString(16),
      },
    }),
    SlashCommand,
    NoteLink,
  ],
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML());
  },
  editorProps: {
    attributes: {
      style: `height: ${props.height || 'calc(100vh - 180px)'}; overflow-y: auto;`,
    },
    handleDrop: (view, event, slice, moved) => {
      if (!moved && event.dataTransfer && event.dataTransfer.files && event.dataTransfer.files[0]) {
        const file = event.dataTransfer.files[0];
        if (file.type.startsWith('image/')) {
          uploadAndInsertImage(file);
          return true;
        }
      }
      return false;
    },
    handlePaste: (view, event) => {
      if (event.clipboardData && event.clipboardData.files && event.clipboardData.files[0]) {
        const file = event.clipboardData.files[0];
        if (file.type.startsWith('image/')) {
          uploadAndInsertImage(file);
          return true;
        }
      }
      return false;
    }
  },
});

const uploadAndInsertImage = async (file: File) => {
  try {
    const compressedFile = await compressImage(file);
    const formData = new FormData();
    formData.append('file', compressedFile);
    
    const loading = ElMessage({
      message: '图片上传中...',
      type: 'info',
      duration: 0
    });
    
    const response = await http.post('/files/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    loading.close();
    editor.value?.chain().focus().setImage({ src: response.url }).run();
  } catch (error) {
    console.error('图片上传失败:', error);
    ElMessage.error('图片上传失败');
  }
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
  if (props.noteId) {
    provider = new YjsProvider(props.noteId, ydoc);
    // After provider is initialized, update the extension
    editor.value?.extensionManager.extensions.forEach(extension => {
      if (extension.name === 'collaborationCursor') {
        extension.options.provider = provider;
      }
    });
  }

  window.addEventListener('tiptap-insert-image', handleInsertImage);
  window.addEventListener('tiptap-ai-summarize', handleAISummarize);
  window.addEventListener('tiptap-ai-extract-tasks', handleAIExtractTasks);
  window.addEventListener('tiptap-ai-ocr', handleAIOCR);
  window.addEventListener('ai:insert-content', handleAIInsert);
});

onBeforeUnmount(() => {
  window.removeEventListener('tiptap-insert-image', handleInsertImage);
  window.removeEventListener('tiptap-ai-summarize', handleAISummarize);
  window.removeEventListener('tiptap-ai-extract-tasks', handleAIExtractTasks);
  window.removeEventListener('tiptap-ai-ocr', handleAIOCR);
  window.removeEventListener('ai:insert-content', handleAIInsert);
  editor.value?.destroy();
});

const handleAIInsert = (e: any) => {
  const content = e.detail?.content;
  if (content && editor.value) {
    editor.value.chain().focus().insertContent(`\n<p>${content.replace(/\n/g, '<br>')}</p>\n`).run();
    ElMessage.success('内容已插入编辑器');
  }
};

const handleAISummarize = async () => {
  if (!editor.value) return;
  const content = editor.value.getText();
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
    editor.value.chain().focus().insertContent(`\n\n> **AI 总结：**\n> ${res.data}\n\n`).run();
    ElMessage.success('总结生成成功');
  } catch (error) {
    ElMessage.error('AI 总结失败');
  }
};

const handleAIExtractTasks = async () => {
  if (!editor.value) return;
  const content = editor.value.getText();
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
      const taskHtml = `<h3>✅ AI 提取的待办事项</h3><ul>${tasks.map(t => `<li>[ ] ${t}</li>`).join('')}</ul>`;
      editor.value.chain().focus().insertContent(`\n\n${taskHtml}\n\n`).run();
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
        editor.value?.chain().focus().insertContent(`\n\n<h3>🖼️ OCR 识图结果</h3><p>${res.data.replace(/\n/g, '<br>')}</p>\n\n`).run();
        ElMessage.success('识图成功');
      }
    } catch (error) {
      loading.close();
      ElMessage.error('识图失败');
    }
  };
  input.click();
};

const handleEditorClick = async (event: MouseEvent) => {
  const target = event.target as HTMLElement;
  if (target.classList.contains('note-link')) {
    const title = target.innerText.replace(/\[\[|\]\]/g, '').trim();
    if (title) {
      try {
        const results = await import('@/api/noteLink').then(m => m.searchTitles(title));
        const note = results.find(n => n.title === title);
        if (note) {
          router.push(`/notes/${note.id}`);
        } else {
          ElMessage.info(`笔记 "${title}" 不存在`);
        }
      } catch (e) {
        console.error('导航失败', e);
      }
    }
  }
};

const imageDialogVisible = ref(false);
const imageForm = ref({
  url: '',
  file: null as File | null
});
const uploading = ref(false);

const linkDialogVisible = ref(false);
const linkForm = ref({
  text: '',
  url: ''
});

const isActive = (name: string, attributes = {}) => {
  return editor.value?.isActive(name, attributes);
};

const handleInsertImage = () => {
  imageForm.value.url = '';
  imageForm.value.file = null;
  imageDialogVisible.value = true;
};

const handleImageChange = (file: any) => {
  imageForm.value.file = file.raw;
};

const handleConfirmImage = async () => {
  if (imageForm.value.url) {
    editor.value?.chain().focus().setImage({ src: imageForm.value.url }).run();
    imageDialogVisible.value = false;
  } else if (imageForm.value.file) {
    uploading.value = true;
    try {
      const formData = new FormData();
      formData.append('file', imageForm.value.file);
      const response = await http.post('/files/upload/image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      editor.value?.chain().focus().setImage({ src: response.url }).run();
      imageDialogVisible.value = false;
    } catch (error) {
      console.error('图片上传失败:', error);
      ElMessage.error('图片上传失败');
    } finally {
      uploading.value = false;
    }
  }
};

const handleInsertLink = () => {
  const selectedText = window.getSelection()?.toString() || '';
  linkForm.value.text = selectedText;
  linkForm.value.url = '';
  linkDialogVisible.value = true;
};

const handleConfirmLink = () => {
  if (linkForm.value.url) {
    editor.value?.chain().focus().setLink({ href: linkForm.value.url }).run();
    if (linkForm.value.text) {
      editor.value?.chain().focus().insertContent(linkForm.value.text).run();
    }
    linkDialogVisible.value = false;
  }
};

watch(() => props.modelValue, (value) => {
  if (editor.value && value !== editor.value.getHTML()) {
    editor.value.commands.setContent(value);
  }
});

onBeforeUnmount(() => {
  editor.value?.destroy();
});
</script>

<style scoped lang="scss">
.rich-text-editor {
  display: flex;
  flex-direction: column;
  height: 100%;

  .toolbar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color);
    background-color: var(--el-bg-color);
    flex-wrap: wrap;
  }

  .editor-content {
    flex: 1;
    overflow: hidden;

    :deep(.ProseMirror) {
      outline: none;
      padding: 24px;
      min-height: 100%;

      > * + * {
        margin-top: 0.75em;
      }

      img {
        max-width: 100%;
        height: auto;
        border-radius: 8px;
        display: block;
        margin: 16px 0;
      }

      a {
        color: var(--el-color-primary);
        text-decoration: underline;
        cursor: pointer;

        &:hover {
          color: var(--el-color-primary-light-3);
        }
      }

      .note-link {
        color: var(--el-color-primary);
        background-color: var(--el-color-primary-light-9);
        padding: 0 4px;
        border-radius: 4px;
        cursor: pointer;
        font-weight: 500;
        
        &:hover {
          background-color: var(--el-color-primary-light-8);
        }
      }

      hr {
        border: none;
        border-top: 2px solid var(--el-border-color);
        margin: 24px 0;
      }

      pre {
        background: var(--el-fill-color-dark);
        color: var(--el-text-color-primary);
        font-family: 'Courier New', monospace;
        padding: 16px;
        border-radius: 8px;
        overflow-x: auto;

        code {
          background: none;
          color: inherit;
          padding: 0;
          font-size: 14px;
        }
      }

      code {
        background: var(--el-fill-color-dark);
        color: var(--el-color-danger);
        padding: 2px 6px;
        border-radius: 4px;
        font-family: 'Courier New', monospace;
        font-size: 14px;
      }

      blockquote {
        border-left: 4px solid var(--el-color-primary);
        background-color: var(--el-fill-color-lighter);
        padding: 8px 16px;
        margin: 16px 0;
        color: var(--el-text-color-regular);
        font-style: italic;
        border-radius: 0 4px 4px 0;
      }

      ul, ol {
        padding-left: 24px;

        li {
          margin: 4px 0;
        }
      }

      p.is-editor-empty:first-child::before {
        color: var(--el-text-color-placeholder);
        content: attr(data-placeholder);
        float: left;
        height: 0;
        pointer-events: none;
      }
    }
  }
}

.image-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    padding: 12px 24px;
    cursor: pointer;
    transition: border-color 0.2s;

    &:hover {
      border-color: var(--el-color-primary);
    }
  }
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
