<template>
  <div class="note-edit-view">
    <div class="editor-header">
      <div class="header-left">
        <el-button text @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>

      <div class="header-center">
        <el-input
          v-model="noteTitle"
          placeholder="输入笔记标题"
          size="large"
          style="width: 400px"
        />
        <el-tag v-if="saveStatus" :type="saveStatus.type" size="small">
          {{ saveStatus.text }}
        </el-tag>
      </div>

      <div class="header-right">
        <el-button-group>
          <el-button @click="handleToggleFavorite">
            <el-icon :color="currentNote?.isFavorite ? '#f56c6c' : ''">
              <Star />
            </el-icon>
          </el-button>
          <el-button @click="handleToggleTop">
            <el-icon :color="currentNote?.isTop ? '#409eff' : ''">
              <Top />
            </el-icon>
          </el-button>
        </el-button-group>
        <el-button type="primary" @click="showAIAssistant = true">
          <el-icon><ChatDotRound /></el-icon>
          AI助手
        </el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </div>

    <div class="editor-toolbar">
      <el-tag
        v-for="tag in noteTags"
        :key="tag"
        closable
        @close="handleRemoveTag(tag)"
      >
        {{ tag }}
      </el-tag>
      <el-input
        v-if="inputVisible"
        ref="tagInputRef"
        v-model="inputValue"
        size="small"
        style="width: 100px"
        @keyup.enter="handleAddTag"
        @blur="handleAddTag"
      />
      <el-button v-else size="small" @click="showInput">
        <el-icon><Plus /></el-icon>
        添加标签
      </el-button>
    </div>

    <div class="editor-container">
      <MarkdownEditor
        v-model="noteContent"
        :height="editorHeight"
        @save="handleAutoSave"
      />
    </div>

    <!-- AI助手侧边面板 -->
    <AIAssistant
      v-model:visible="showAIAssistant"
      :note-id="noteId"
      @insert="handleInsertContent"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useNoteStore } from '@/stores/note';
import MarkdownEditor from '@/components/editor/MarkdownEditor.vue';
import AIAssistant from '@/components/ai/AIAssistant.vue';

const route = useRoute();
const router = useRouter();
const noteStore = useNoteStore();

const noteId = computed(() => parseInt(route.params.id as string));
const currentNote = computed(() => noteStore.currentNote);

const noteTitle = ref('');
const noteContent = ref('');
const noteTags = ref<string[]>([]);
const saveStatus = ref<{ type: string; text: string } | null>(null);
const editorHeight = ref('calc(100vh - 180px)');

const inputVisible = ref(false);
const inputValue = ref('');
const tagInputRef = ref();
const showAIAssistant = ref(false);

// 防抖自动保存
let saveTimer: ReturnType<typeof setTimeout> | null = null;

const showSaveStatus = (type: 'success' | 'warning', text: string) => {
  saveStatus.value = { type, text };
  setTimeout(() => {
    saveStatus.value = null;
  }, 2000);
};

const showInput = () => {
  inputVisible.value = true;
  nextTick(() => {
    tagInputRef.value?.focus();
  });
};

const handleAddTag = () => {
  const tag = inputValue.value.trim();
  if (tag && !noteTags.value.includes(tag)) {
    noteTags.value.push(tag);
  }
  inputVisible.value = false;
  inputValue.value = '';
};

const handleRemoveTag = (tag: string) => {
  noteTags.value = noteTags.value.filter(t => t !== tag);
};

const handleToggleFavorite = async () => {
  if (!currentNote.value) return;
  try {
    await noteStore.toggleFavorite(currentNote.value.id);
    showSaveStatus('success', '已更新收藏状态');
  } catch (error) {
    console.error('切换收藏失败:', error);
  }
};

const handleToggleTop = async () => {
  if (!currentNote.value) return;
  try {
    await noteStore.toggleTop(currentNote.value.id);
    showSaveStatus('success', '已更新置顶状态');
  } catch (error) {
    console.error('切换置顶失败:', error);
  }
};

const handleSave = async () => {
  try {
    await saveNote();
    showSaveStatus('success', '保存成功');
  } catch (error) {
    console.error('保存失败:', error);
  }
};

const handleAutoSave = () => {
  // 防抖自动保存
  if (saveTimer) {
    clearTimeout(saveTimer);
  }
  saveTimer = setTimeout(async () => {
    try {
      await saveNote();
      showSaveStatus('success', '自动保存成功');
    } catch (error) {
      console.error('自动保存失败:', error);
    }
  }, 3000);
};

const saveNote = async () => {
  if (!noteId.value) {
    // 新建笔记
    const note = await noteStore.createNote({
      title: noteTitle.value,
      content: noteContent.value,
      contentType: 'markdown'
    });
    // 更新标签
    await noteStore.updateNote(note.id, { tags: noteTags.value });
    router.replace(`/notes/${note.id}`);
  } else {
    // 更新笔记
    await noteStore.updateNote(noteId.value, {
      title: noteTitle.value,
      content: noteContent.value,
      tags: noteTags.value
    });
  }
};

const handleBack = () => {
  router.back();
};

const handleInsertContent = (content: string) => {
  noteContent.value += '\n\n' + content;
  showSaveStatus('success', '内容已插入');
};

// 监听内容变化
watch([noteTitle, noteContent, noteTags], () => {
  handleAutoSave();
}, { deep: true });

// 加载笔记数据
onMounted(async () => {
  if (noteId.value && noteId.value !== 0) {
    try {
      await noteStore.fetchNote(noteId.value);
      if (currentNote.value) {
        noteTitle.value = currentNote.value.title;
        noteContent.value = currentNote.value.content;
        noteTags.value = currentNote.value.tags || [];
      }
    } catch (error) {
      console.error('加载笔记失败:', error);
      ElMessage.error('加载笔记失败');
    }
  }
});

onBeforeUnmount(() => {
  if (saveTimer) {
    clearTimeout(saveTimer);
  }
});
</script>

<style scoped lang="scss">
.note-edit-view {
  height: 100vh;
  display: flex;
  flex-direction: column;

  .editor-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 24px;
    border-bottom: 1px solid var(--el-border-color);
    background-color: var(--el-bg-color);

    .header-left {
      display: flex;
      align-items: center;
    }

    .header-center {
      display: flex;
      align-items: center;
      gap: 16px;

      .el-tag {
        font-size: 12px;
      }
    }

    .header-right {
      display: flex;
      gap: 12px;
    }
  }

  .editor-toolbar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    border-bottom: 1px solid var(--el-border-color);
    background-color: var(--el-bg-color-page);
  }

  .editor-container {
    flex: 1;
    overflow: hidden;
  }
}
</style>
