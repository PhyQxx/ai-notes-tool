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
            :type="isActive({ textAlign: 'left' }) ? 'primary' : ''"
            @click="editor?.chain().focus().setTextAlign('left').run()"
          >
            <el-icon><DArrowLeft /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="居中" placement="top">
          <el-button
            :type="isActive({ textAlign: 'center' }) ? 'primary' : ''"
            @click="editor?.chain().focus().setTextAlign('center').run()"
          >
            <el-icon><Minus /></el-icon>
          </el-button>
        </el-tooltip>
        <el-tooltip content="右对齐" placement="top">
          <el-button
            :type="isActive({ textAlign: 'right' }) ? 'primary' : ''"
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

    <div class="editor-content">
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
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue';
import { ElMessage } from 'element-plus';
import { useEditor, EditorContent } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';
import Image from '@tiptap/extension-image';
import Link from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';
import { http } from '@/utils/request';

const props = defineProps<{
  modelValue: string;
  height?: string;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit,
    Underline,
    TextAlign.configure({
      types: ['heading', 'paragraph'],
    }),
    Image,
    Link.configure({
      openOnClick: false,
    }),
    Placeholder.configure({
      placeholder: '开始编写您的笔记...',
    }),
  ],
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML());
  },
  editorProps: {
    attributes: {
      style: `height: ${props.height || 'calc(100vh - 180px)'}; overflow-y: auto;`,
    },
  },
});

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
      const response = await http.post('/upload/image', formData, {
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

      hr {
        border: none;
        border-top: 2px solid var(--el-border-color);
        margin: 24px 0;
      }

      pre {
        background: var(--el-fill-color-light);
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
        background: var(--el-fill-color-light);
        color: var(--el-color-danger);
        padding: 2px 6px;
        border-radius: 4px;
        font-family: 'Courier New', monospace;
        font-size: 14px;
      }

      blockquote {
        border-left: 4px solid var(--el-color-primary);
        padding-left: 16px;
        margin: 16px 0;
        color: var(--el-text-color-secondary);
        font-style: italic;
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
</style>
