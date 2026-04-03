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
        <el-button @click="openBacklinks">
          <el-icon><Link /></el-icon>
          反向链接 ({{ backlinks.length }})
        </el-button>
        <ExportMenu :note-id="noteId" :note-title="noteTitle" />
        <el-button @click="openShareDialog">
          <el-icon><Share /></el-icon>
          分享
        </el-button>
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
      <VoiceInput @transcript="handleVoiceTranscript" />
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

    <!-- 反向链接面板 -->
    <el-drawer
      v-model="showBacklinksPanel"
      title="反向链接"
      direction="rtl"
      :size="350"
    >
      <div v-if="backlinks.length === 0" style="text-align:center;color:#999;padding:40px 0;">
        暂无笔记引用此笔记
      </div>
      <div
        v-for="link in backlinks"
        :key="link.id"
        class="backlink-item"
        @click="navigateToNote(link.sourceNoteId)"
      >
        <div class="backlink-title">{{ link.sourceTitle }}</div>
        <div class="backlink-time">{{ formatTime(link.createdAt) }}</div>
      </div>
    </el-drawer>

    <!-- [[ 笔记选择器弹出框 -->
    <div
      v-if="showLinkSelector"
      class="link-selector-popup"
      :style="{ top: linkSelectorPos.top + 'px', left: linkSelectorPos.left + 'px' }"
    >
      <div class="link-selector-input">
        <el-input
          v-model="linkSearchKeyword"
          placeholder="搜索笔记标题..."
          size="small"
          autofocus
          @input="handleLinkSearch"
          @keydown.down.prevent="moveLinkSelection(1)"
          @keydown.up.prevent="moveLinkSelection(-1)"
          @keydown.enter.prevent="selectCurrentLink"
          @keydown.esc.prevent="closeLinkSelector"
        />
      </div>
      <div class="link-selector-list">
        <div
          v-for="(item, idx) in linkSearchResults"
          :key="item.id"
          :class="['link-selector-item', { active: linkSelectedIndex === idx }]"
          @click="insertLink(item)"
          @mouseenter="linkSelectedIndex = idx"
        >
          {{ item.title }}
        </div>
        <div v-if="linkSearchResults.length === 0 && linkSearchKeyword" class="link-selector-empty">
          未找到匹配笔记
        </div>
      </div>
    </div>
  </div>

    <!-- 分享弹窗 -->
    <el-dialog v-model="showShareDialog" title="分享笔记" width="500px">
      <div class="share-section">
        <el-form label-width="80px">
          <el-form-item label="链接Slug">
            <el-input v-model="shareSlug" placeholder="自定义链接后缀，留空自动生成" />
          </el-form-item>
          <el-form-item label="密码保护">
            <el-switch v-model="sharePasswordEnabled" />
            <el-input
              v-if="sharePasswordEnabled"
              v-model="sharePassword"
              placeholder="设置访问密码"
              style="margin-top: 8px"
              show-password
            />
          </el-form-item>
          <el-form-item label="有效期">
            <el-date-picker
              v-model="shareExpireAt"
              type="datetime"
              placeholder="留空为永久有效"
              :disabled-date="(t: Date) => t < new Date()"
            />
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="shareCreating" @click="handleCreateShare" style="width: 100%">
          创建分享链接
        </el-button>
      </div>
      <div v-if="shareList.length > 0" class="share-list">
        <div class="share-list-title">已创建的分享</div>
        <div v-for="s in shareList" :key="s.id" class="share-item">
          <div class="share-info">
            <el-link type="primary" :underline="false" @click="copyShareLink(s.slug)">
              {{ shareBaseUrl }}/{{ s.slug }}
            </el-link>
            <span class="share-meta">浏览 {{ s.viewCount }} 次</span>
            <el-tag v-if="s.password" size="small" type="warning">密码保护</el-tag>
            <el-tag v-if="s.expireAt" size="small" type="info">{{ new Date(s.expireAt).toLocaleString() }} 过期</el-tag>
          </div>
          <el-button text type="danger" size="small" @click="handleDeleteShare(s.id)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </el-dialog>
    <AttachmentList :note-id="noteId" />
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
import { createShare, listShares, deleteShare } from '@/api/noteShare';
import CollabIndicator from '@/components/editor/CollabIndicator.vue';
import VoiceInput from '@/components/voice/VoiceInput.vue';
import AttachmentList from '@/components/attachment/AttachmentList.vue';
import wsClient, { type WSMessage } from '@/utils/websocket';
import type { NoteVersion } from '@/types';
import { getSpaceDetail } from '@/api/space';
import { getFolderTree } from '@/api/folder';
import { getBacklinks, searchTitles, type NoteLink } from '@/api/noteLink';

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
const showBacklinksPanel = ref(false);
const backlinks = ref<NoteLink[]>([]);

// 分享
const showShareDialog = ref(false);
const shareSlug = ref('');
const sharePasswordEnabled = ref(false);
const sharePassword = ref('');
const shareExpireAt = ref('');
const shareCreating = ref(false);
const shareList = ref<any[]>([]);
const shareBaseUrl = `${window.location.origin}/share`;

// [[ 笔记选择器
const showLinkSelector = ref(false);
const linkSearchKeyword = ref('');
const linkSearchResults = ref<any[]>([]);
const linkSelectedIndex = ref(0);
const linkSelectorPos = ref({ top: 0, left: 0 });
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

const handleVoiceTranscript = (text: string) => {
  noteContent.value += text;
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

// 反向链接
const openShareDialog = async () => {
  showShareDialog.value = true;
  shareSlug.value = '';
  sharePassword.value = '';
  sharePasswordEnabled.value = false;
  shareExpireAt.value = '';
  try {
    shareList.value = await listShares(noteId.value);
  } catch (e) { shareList.value = []; }
};

const handleCreateShare = async () => {
  shareCreating.value = true;
  try {
    const s = await createShare(noteId.value, {
      slug: shareSlug.value || undefined,
      password: sharePasswordEnabled.value ? sharePassword.value : undefined,
      expireAt: shareExpireAt.value || undefined
    });
    shareList.value.unshift(s);
    ElMessage.success('分享链接已创建');
    copyShareLink(s.slug);
  } catch (e: any) {
    ElMessage.error(e.message || '创建失败');
  } finally {
    shareCreating.value = false;
  }
};

const copyShareLink = (slug: string) => {
  navigator.clipboard.writeText(`${shareBaseUrl}/${slug}`);
  ElMessage.success('链接已复制');
};

const handleDeleteShare = async (shareId: number) => {
  try {
    await ElMessageBox.confirm('确定取消此分享？', '取消分享', { type: 'warning' });
    await deleteShare(noteId.value, shareId);
    shareList.value = shareList.value.filter(s => s.id !== shareId);
    ElMessage.success('已取消分享');
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败');
  }
};

const openBacklinks = async () => {
  if (!noteId.value) return;
  try {
    backlinks.value = await getBacklinks(noteId.value);
  } catch (e) {
    console.error('获取反向链接失败', e);
  }
  showBacklinksPanel.value = true;
};

const navigateToNote = (id: number) => {
  router.push(`/notes/${id}`);
};

const formatTime = (t: string) => {
  if (!t) return '';
  return new Date(t).toLocaleString('zh-CN');
};

// [[ 笔记选择器
let linkSearchTimer: ReturnType<typeof setTimeout> | null = null;

const handleLinkSearch = () => {
  if (linkSearchTimer) clearTimeout(linkSearchTimer);
  linkSearchTimer = setTimeout(async () => {
    if (!linkSearchKeyword.value.trim()) {
      linkSearchResults.value = [];
      return;
    }
    try {
      linkSearchResults.value = await searchTitles(linkSearchKeyword.value.trim());
      linkSelectedIndex.value = 0;
    } catch (e) {
      linkSearchResults.value = [];
    }
  }, 300);
};

const moveLinkSelection = (dir: number) => {
  const len = linkSearchResults.value.length;
  if (len === 0) return;
  linkSelectedIndex.value = (linkSelectedIndex.value + dir + len) % len;
};

const selectCurrentLink = () => {
  if (linkSearchResults.value.length > 0 && linkSelectedIndex.value >= 0) {
    insertLink(linkSearchResults.value[linkSelectedIndex.value]);
  }
};

const insertLink = (item: any) => {
  const content = noteContent.value;
  // Find the last unclosed [[ in content and replace with [[title]]
  const lastOpenIdx = content.lastIndexOf('[[');
  if (lastOpenIdx >= 0) {
    noteContent.value = content.substring(0, lastOpenIdx) + `[[${item.title}]]` + content.substring(lastOpenIdx + 2);
  }
  closeLinkSelector();
};

const closeLinkSelector = () => {
  showLinkSelector.value = false;
  linkSearchKeyword.value = '';
  linkSearchResults.value = [];
};

// 监听编辑器输入 [[
const onEditorKeydown = (e: KeyboardEvent) => {
  // This is a global listener; the actual [[ detection is handled via content watch
};

// Watch content for [[ to trigger selector
let lastContentLength = 0;
const checkForLinkTrigger = () => {
  const content = noteContent.value || '';
  if (content.length >= 2 && content.endsWith('[[')) {
    showLinkSelector.value = true;
    linkSearchKeyword.value = '';
    linkSearchResults.value = [];
    linkSelectedIndex.value = 0;
    // Position near the editor area
    const editorEl = document.querySelector('.vditor') as HTMLElement;
    if (editorEl) {
      const rect = editorEl.getBoundingClientRect();
      linkSelectorPos.value = { top: rect.top + 60, left: rect.left + rect.width / 2 - 150 };
    } else {
      linkSelectorPos.value = { top: 200, left: 300 };
    }
  }
};

// 监听内容变化
watch([noteTitle, noteContent, noteTags], () => {
  handleAutoSave();
  checkForLinkTrigger();
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

.backlink-item {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
  transition: background-color 0.2s;

  &:hover {
    background-color: var(--el-fill-color-light);
  }

  .backlink-title {
    font-weight: 500;
    margin-bottom: 4px;
  }

  .backlink-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.link-selector-popup {
  position: fixed;
  z-index: 9999;
  width: 300px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;

  .link-selector-input {
    padding: 8px;
    border-bottom: 1px solid var(--el-border-color);
  }

  .link-selector-list {
    max-height: 200px;
    overflow-y: auto;
  }

  .link-selector-item {
    padding: 8px 12px;
    cursor: pointer;
    font-size: 14px;

    &:hover, &.active {
      background-color: var(--el-color-primary-light-9);
    }
  }

  .link-selector-empty {
    padding: 16px;
    text-align: center;
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }
}
</style>
