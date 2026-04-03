<template>
  <div class="note-edit-view">
    <div class="editor-header">
      <div class="header-left">
        <el-breadcrumb separator="/" class="note-breadcrumb">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="spaceName" :to="{ path: '/spaces/' + currentNote?.spaceId }">{{ spaceName }}</el-breadcrumb-item>
          <el-breadcrumb-item v-if="folderName" :to="{ path: '/notes', query: { folderId: String(currentNote?.folderId) } }">{{ folderName }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ noteTitle || '未命名笔记' }}</el-breadcrumb-item>
        </el-breadcrumb>
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
        <el-button @click="showVersionPanel = true">
          <el-icon><Clock /></el-icon>
          版本历史
        </el-button>
        <el-button @click="showCommentPanel = true">
          <el-icon><ChatDotRound /></el-icon>
          评论 ({{ commentCount }})
        </el-button>
        <ExportMenu :note-id="noteId" :note-title="noteTitle" />
        <el-button type="primary" @click="showAIAssistant = true">
          <el-icon><ChatDotRound /></el-icon>
          AI助手
        </el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </div>
    </div>

    <div class="editor-toolbar">
      <CollabIndicator />
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
      <div class="editor-mode-switch">
        <el-radio-group v-model="editorMode" @change="handleEditorModeChange">
          <el-radio-button label="markdown">Markdown</el-radio-button>
          <el-radio-button label="richtext">富文本</el-radio-button>
        </el-radio-group>
      </div>

      <MarkdownEditor
        v-if="editorMode === 'markdown'"
        v-model="noteContent"
        :height="editorHeight"
        @save="handleAutoSave"
      />

      <RichTextEditor
        v-else
        v-model="noteContent"
        :height="editorHeight"
      />
    </div>

    <!-- AI助手侧边面板 -->
    <AIAssistant
      v-model:visible="showAIAssistant"
      :note-id="noteId"
      @insert="handleInsertContent"
    />

    <!-- 版本历史面板 -->
    <VersionPanel
      v-model="showVersionPanel"
      :note-id="noteId"
      @restore="handleVersionRestore"
    />

    <!-- 评论面板 -->
    <el-drawer
      v-model="showCommentPanel"
      title="评论"
      direction="btt"
      :size="400"
    >
      <CommentPanel v-if="noteId" :note-id="noteId" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useNoteStore } from '@/stores/note';
import { useShortcuts } from '@/composables/useShortcuts';
import MarkdownEditor from '@/components/editor/MarkdownEditor.vue';
import RichTextEditor from '@/components/editor/RichTextEditor.vue';
import AIAssistant from '@/components/ai/AIAssistant.vue';
import VersionPanel from '@/components/editor/VersionPanel.vue';
import ExportMenu from '@/components/editor/ExportMenu.vue';
import CommentPanel from '@/components/note/CommentPanel.vue';
import CollabIndicator from '@/components/editor/CollabIndicator.vue';
import wsClient, { type WSMessage } from '@/utils/websocket';
import type { NoteVersion } from '@/types';
import { getSpaceDetail } from '@/api/space';
import { getFolderTree } from '@/api/folder';

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
const showVersionPanel = ref(false);
const showCommentPanel = ref(false);
const commentCount = ref(0);
const editorMode = ref<'markdown' | 'richtext'>('markdown');
const spaceName = ref('');
const folderName = ref('');

// ===== 自动保存草稿（localStorage）=====
let autoSaveTimer: ReturnType<typeof setInterval> | null = null;
const lastSavedDraftContent = ref('');

function getDraftKey(id: number | string) {
  return `note-draft-${id}`;
}

function startAutoSaveDraft() {
  stopAutoSaveDraft();
  autoSaveTimer = setInterval(() => {
    const current = noteContent.value;
    if (current && current !== lastSavedDraftContent.value) {
      const key = getDraftKey(noteId.value || 'new');
      localStorage.setItem(key, current);
      localStorage.setItem(key + '-time', new Date().toISOString());
      lastSavedDraftContent.value = current;
    }
  }, 5000);
}

function stopAutoSaveDraft() {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer);
    autoSaveTimer = null;
  }
}

function clearDraft() {
  const key = getDraftKey(noteId.value || 'new');
  localStorage.removeItem(key);
  localStorage.removeItem(key + '-time');
}

async function checkDraftRecovery() {
  const key = getDraftKey(noteId.value || 'new');
  const draft = localStorage.getItem(key);
  if (!draft || draft.length === 0) return;
  // 仅在服务端内容为空或草稿比服务端内容新时提示
  if (noteContent.value && noteContent.value === draft) return;
  try {
    await ElMessageBox.confirm(
      '检测到未保存的草稿，是否恢复？',
      '草稿恢复',
      {
        confirmButtonText: '恢复',
        cancelButtonText: '丢弃',
        type: 'info',
        distinguishCancelAndClose: true,
      }
    );
    noteContent.value = draft;
    lastSavedDraftContent.value = draft;
  } catch {
    clearDraft();
  }
}

// 注册全局快捷键
useShortcuts();

// 监听 Ctrl+S 事件
function onNoteSave() {
  handleSave();
}

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
    clearDraft();
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

const handleEditorModeChange = async (mode: 'markdown' | 'richtext') => {
  // Just switch mode, auto-save will handle content preservation
  try {
    if (noteId.value) {
      await saveNote();
    }
  } catch (error) {
    console.error('切换编辑器模式失败:', error);
  }
};

const handleVersionRestore = (version: NoteVersion) => {
  noteTitle.value = version.title;
  noteContent.value = version.content;
  showSaveStatus('success', '已恢复到版本 #' + version.versionNo);
};

// 监听内容变化
watch([noteTitle, noteContent, noteTags], () => {
  handleAutoSave();
}, { deep: true });

// WebSocket 实时协作
let contentChangeTimer: ReturnType<typeof setTimeout> | null = null;

const handleContentChange = (msg: WSMessage) => {
  if (msg.noteId !== noteId.value) return;
  // 最后写入胜出策略：如果当前用户没有在编辑，则更新内容
  if (msg.data?.content && msg.userId) {
    noteContent.value = msg.data.content;
    showSaveStatus('success', '其他用户更新了内容');
  }
};

wsClient.on('content_change', handleContentChange);

// 加载笔记数据
onMounted(async () => {
  // 注册 Ctrl+S 监听
  window.addEventListener('note:save', onNoteSave);

  if (noteId.value && noteId.value !== 0) {
    try {
      await noteStore.fetchNote(noteId.value);
      if (currentNote.value) {
        noteTitle.value = currentNote.value.title;
        noteContent.value = currentNote.value.content;
        noteTags.value = currentNote.value.tags || [];
        editorMode.value = (currentNote.value.contentType as 'markdown' | 'richtext') || 'markdown';
        // Resolve breadcrumb
        if (currentNote.value.spaceId) {
          try { const s = await getSpaceDetail(currentNote.value.spaceId); spaceName.value = s.name || ''; } catch(e) {}
        }
        if (currentNote.value.folderId) {
          try {
            const tree = await getFolderTree();
            const find = (list: any[], id: number): any => { for (const f of list) { if (f.id === id) return f; if (f.children) { const r = find(f.children, id); if (r) return r; } } return null; };
            const folder = find(tree, currentNote.value.folderId);
            if (folder) folderName.value = folder.name;
          } catch(e) {}
        }
        lastSavedDraftContent.value = noteContent.value;
      }
    } catch (error) {
      console.error('加载笔记失败:', error);
      ElMessage.error('加载笔记失败');
    }
    // 检查草稿恢复
    await checkDraftRecovery();
    // 加入 WebSocket 笔记协作房间
    wsClient.joinNote(noteId.value);
  } else {
    // 新建笔记
    editorMode.value = 'markdown';
    await checkDraftRecovery();
  }
  // 启动草稿自动保存
  startAutoSaveDraft();
});

onBeforeUnmount(() => {
  if (saveTimer) {
    clearTimeout(saveTimer);
  }
  stopAutoSaveDraft();
  window.removeEventListener('note:save', onNoteSave);
  if (noteId.value && noteId.value !== 0) {
    wsClient.leaveNote(noteId.value);
  }
  wsClient.off('content_change', handleContentChange);
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
      min-width: 200px;

      .note-breadcrumb {
        font-size: 14px;
      }
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
    display: flex;
    flex-direction: column;

    .editor-mode-switch {
      display: flex;
      justify-content: center;
      padding: 8px;
      background-color: var(--el-bg-color-page);
      border-bottom: 1px solid var(--el-border-color);
    }
  }
}
</style>
