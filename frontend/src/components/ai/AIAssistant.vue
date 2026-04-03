<template>
  <el-drawer
    v-model="visible"
    title="AI助手"
    direction="rtl"
    size="600px"
    :close-on-click-modal="false"
  >
    <template #header>
      <div class="assistant-header">
        <span class="header-title">AI助手</span>
        <el-button text @click="handleClose">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </template>

    <div class="assistant-content">
      <!-- 对话列表侧边栏 -->
      <div class="conversation-sidebar">
        <el-button type="primary" size="small" style="width:100%" @click="handleNewConversation">
          + 新对话
        </el-button>
        <div class="conversation-list">
          <div
            v-for="conv in aiStore.conversations"
            :key="conv.id"
            :class="['conversation-item', { active: aiStore.currentConversation?.id === conv.id }]"
            @click="handleSelectConversation(conv)"
            @contextmenu.prevent="showContextMenu($event, conv)"
          >
            <span class="conv-title">{{ conv.title || '新对话' }}</span>
            <el-icon class="conv-delete" @click.stop="handleDeleteConversation(conv.id)"><Delete /></el-icon>
          </div>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="main-area">
        <!-- 提供商和模型选择 -->
        <div class="provider-section">
          <AIProviderSelect
            v-model:provider="aiStore.config.provider"
            v-model:model="aiStore.config.model"
          />
        </div>

        <!-- 快捷操作按钮 -->
        <div class="quick-actions">
          <el-button
            v-for="action in quickActions"
            :key="action.type"
            size="small"
            :loading="action.loading"
            @click="handleQuickAction(action.type)"
          >
            {{ action.label }}
          </el-button>
        </div>

        <!-- 对话区域 -->
        <div class="chat-area">
          <div ref="chatContainer" class="chat-messages">
            <el-empty v-if="displayMessages.length === 0 && !aiStore.isStreaming" description="开始新对话" :image-size="60" />
            <div
              v-for="(msg, index) in displayMessages"
              :key="index"
              :class="['message', msg.role]"
            >
              <div class="message-content">
                <div class="message-role">{{ msg.role === 'user' ? '我' : 'AI助手' }}</div>
                <div class="message-text">{{ msg.content }}</div>
              </div>
            </div>

            <!-- 流式输出 -->
            <div v-if="aiStore.isStreaming" class="message assistant streaming">
              <div class="message-content">
                <div class="message-role">AI助手</div>
                <div class="message-text">
                  {{ aiStore.streamMessage || '思考中...' }}
                  <span class="cursor">|</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="输入问题，按 Enter 发送，Shift + Enter 换行"
            @keydown="handleKeyDown"
          />
          <div class="input-actions">
            <el-button
              v-if="aiStore.isStreaming"
              type="danger"
              @click="handleStopGeneration"
            >
              ⏹ 停止生成
            </el-button>
            <el-button
              type="primary"
              :loading="aiStore.isStreaming"
              @click="handleSend"
              :disabled="!inputMessage.trim()"
            >
              <el-icon v-if="!aiStore.isStreaming"><Promotion /></el-icon>
              {{ aiStore.isStreaming ? '生成中...' : '发送' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右键菜单 -->
    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
    >
      <div class="context-menu-item" @click="handleRenameConversation">重命名</div>
      <div class="context-menu-item danger" @click="handleDeleteFromMenu">删除</div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Close, Promotion, Delete } from '@element-plus/icons-vue';
import { useAIStore } from '@/stores/ai';
import { useNoteStore } from '@/stores/note';
import AIProviderSelect from './AIProviderSelect.vue';
import type { AIConversation, AIChatMessage } from '@/types';

const props = defineProps<{
  visible: boolean;
  noteId?: number;
}>();

const emit = defineEmits<{
  'update:visible': [value: boolean];
  'insert': [content: string];
}>();

const aiStore = useAIStore();
const noteStore = useNoteStore();

const visible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
});

const inputMessage = ref('');
const chatContainer = ref<HTMLElement>();
let abortController: AbortController | null = null;

// 右键菜单
const contextMenuVisible = ref(false);
const contextMenuX = ref(0);
const contextMenuY = ref(0);
const contextMenuTarget = ref<AIConversation | null>(null);

const quickActions = ref([
  { type: 'summarize', label: '📝 摘要', loading: false },
  { type: 'optimize', label: '✨ 优化', loading: false },
  { type: 'expand', label: '📖 续写', loading: false },
  { type: 'rewrite', label: '🔄 改写', loading: false }
]);

const displayMessages = computed(() => {
  return aiStore.currentConversation?.messages || [];
});

const handleNewConversation = () => {
  aiStore.newConversation();
};

const handleSelectConversation = async (conv: AIConversation) => {
  try {
    await aiStore.fetchConversation(conv.id);
    await nextTick();
    scrollToBottom();
  } catch (e) {
    ElMessage.error('加载对话失败');
  }
};

const handleDeleteConversation = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', { type: 'warning' });
    await aiStore.deleteConversation(id);
    ElMessage.success('已删除');
  } catch {}
};

const showContextMenu = (e: MouseEvent, conv: AIConversation) => {
  contextMenuTarget.value = conv;
  contextMenuX.value = e.clientX;
  contextMenuY.value = e.clientY;
  contextMenuVisible.value = true;
};

const handleRenameConversation = async () => {
  if (!contextMenuTarget.value) return;
  contextMenuVisible.value = false;
  try {
    const { value } = await ElMessageBox.prompt('请输入新标题', '重命名对话', {
      inputValue: contextMenuTarget.value.title,
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    });
    if (value) {
      // Call API to rename
      const token = localStorage.getItem('token');
      await fetch(`/api/ai/conversations/${contextMenuTarget.value.id}/rename`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ title: value })
      });
      contextMenuTarget.value.title = value;
      ElMessage.success('已重命名');
    }
  } catch {}
};

const handleDeleteFromMenu = async () => {
  if (!contextMenuTarget.value) return;
  contextMenuVisible.value = false;
  await handleDeleteConversation(contextMenuTarget.value.id);
};

const handleStopGeneration = () => {
  if (abortController) {
    abortController.abort();
    abortController = null;
  }
  aiStore.isStreaming = false;
  aiStore.streamMessage = '';
};

const handleQuickAction = async (type: string) => {
  if (!noteStore.currentNote) {
    ElMessage.warning('请先选择笔记');
    return;
  }
  const action = quickActions.value.find(a => a.type === type);
  if (action) {
    action.loading = true;
    try {
      const content = await aiStore.generateContent(
        type as any,
        noteStore.currentNote.content,
        noteStore.currentNote.id
      );
      if (!aiStore.currentConversation) {
        aiStore.currentConversation = {
          id: Date.now(),
          aiProvider: aiStore.config.provider,
          aiModel: aiStore.config.model,
          title: content.substring(0, 50),
          messages: [{ role: 'assistant', content }],
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
      }
      await nextTick();
      scrollToBottom();
    } catch (error) {
      ElMessage.error('生成失败');
    } finally {
      action.loading = false;
    }
  }
};

const handleSend = async () => {
  if (!inputMessage.value.trim()) return;
  const content = inputMessage.value.trim();
  inputMessage.value = '';
  try {
    await aiStore.sendMessage(content, props.noteId);
    await nextTick();
    scrollToBottom();
  } catch (error) {
    ElMessage.error('发送失败');
  }
};

const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    handleSend();
  }
};

const handleClose = () => { visible.value = false; };

const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

watch(() => aiStore.streamMessage, () => {
  nextTick(() => scrollToBottom());
});

watch(visible, async (val) => {
  if (val) {
    await aiStore.fetchConversations();
  }
  if (!val) {
    contextMenuVisible.value = false;
  }
});

// 点击其他地方关闭右键菜单
if (typeof document !== 'undefined') {
  document.addEventListener('click', () => { contextMenuVisible.value = false; });
}
</script>

<style scoped lang="scss">
.assistant-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  .header-title { font-size: 16px; font-weight: 600; }
}

.assistant-content {
  display: flex;
  height: calc(100vh - 80px);
  gap: 12px;
}

.conversation-sidebar {
  width: 180px;
  min-width: 180px;
  border-right: 1px solid var(--el-border-color);
  padding-right: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow-y: auto;

  .conversation-list {
    flex: 1;
    overflow-y: auto;
  }

  .conversation-item {
    padding: 8px 10px;
    border-radius: 6px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 4px;
    font-size: 13px;
    transition: background 0.2s;

    &:hover { background: var(--el-fill-color-light); }
    &.active { background: var(--el-color-primary-light-9); color: var(--el-color-primary); }

    .conv-title {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      flex: 1;
    }
    .conv-delete { opacity: 0; transition: opacity 0.2s; color: var(--el-text-color-secondary); }
    &:hover .conv-delete { opacity: 1; }
  }
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;

  .provider-section { margin-bottom: 8px; }

  .quick-actions {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
    flex-wrap: wrap;
  }

  .chat-area {
    flex: 1;
    overflow-y: auto;
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 12px;
    background-color: var(--el-bg-color-page);
    min-height: 200px;

    .chat-messages {
      .message {
        margin-bottom: 16px;
        &:last-child { margin-bottom: 0; }
        .message-content {
          display: flex; flex-direction: column; gap: 4px;
          .message-role { font-size: 12px; color: var(--el-text-color-secondary); }
          .message-text {
            padding: 10px 14px; border-radius: 8px; line-height: 1.6;
            white-space: pre-wrap; word-break: break-word;
          }
          .cursor { display: inline-block; animation: blink 1s infinite; }
        }
        &.user .message-text { background-color: var(--el-color-primary-light-9); margin-left: auto; max-width: 80%; }
        &.assistant .message-text { background-color: var(--el-fill-color-light); margin-right: auto; max-width: 80%; }
        &.assistant.streaming .message-text { background-color: var(--el-fill-color); }
      }
    }
  }

  .input-area {
    .input-actions {
      display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px;
    }
  }
}

.context-menu {
  position: fixed;
  z-index: 9999;
  background: var(--el-bg-color-overlay);
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  padding: 4px 0;
  box-shadow: var(--el-box-shadow-light);
  .context-menu-item {
    padding: 6px 16px;
    cursor: pointer;
    font-size: 13px;
    &:hover { background: var(--el-fill-color-light); }
    &.danger { color: var(--el-color-danger); }
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
</style>
