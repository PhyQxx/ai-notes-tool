<template>
  <el-drawer
    v-model="visible"
    title="AI助手"
    direction="rtl"
    size="450px"
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
      <!-- 快捷操作按钮 -->
      <div class="quick-actions">
        <el-button
          v-for="action in quickActions"
          :key="action.type"
          :icon="action.icon"
          :loading="action.loading"
          @click="handleQuickAction(action.type)"
        >
          {{ action.label }}
        </el-button>
      </div>

      <!-- 提供商和模型选择 -->
      <div class="provider-section">
        <AIProviderSelect
          v-model:provider="aiStore.config.provider"
          v-model:model="aiStore.config.model"
        />
      </div>

      <!-- 对话区域 -->
      <div class="chat-area">
        <div ref="chatContainer" class="chat-messages">
          <div
            v-for="(msg, index) in messages"
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
                {{ aiStore.streamMessage }}
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
        <el-button
          type="primary"
          :loading="aiStore.isStreaming"
          @click="handleSend"
          style="margin-top: 12px; width: 100%"
        >
          <el-icon v-if="!aiStore.isStreaming"><Promotion /></el-icon>
          {{ aiStore.isStreaming ? '生成中...' : '发送' }}
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { Close, Promotion, Document, Magic, DocumentCopy, RefreshRight } from '@element-plus/icons-vue';
import { useAIStore } from '@/stores/ai';
import { useNoteStore } from '@/stores/note';
import AIProviderSelect from './AIProviderSelect.vue';
import type { AIChatMessage } from '@/types';

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
const messages = ref<AIChatMessage[]>([]);
const chatContainer = ref<HTMLElement>();

const quickActions = ref([
  { type: 'summarize', label: '📝 摘要', icon: Document, loading: false },
  { type: 'optimize', label: '✨ 优化', icon: Magic, loading: false },
  { type: 'expand', label: '📖 续写', icon: DocumentCopy, loading: false },
  { type: 'rewrite', label: '🔄 改写', icon: RefreshRight, loading: false }
]);

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

      // 添加到对话
      messages.value.push({
        role: 'assistant',
        content
      });

      // 滚动到底部
      await nextTick();
      scrollToBottom();

      ElMessage.success('生成成功');
    } catch (error) {
      console.error('AI生成失败:', error);
      ElMessage.error('生成失败，请稍后重试');
    } finally {
      action.loading = false;
    }
  }
};

const handleSend = async () => {
  if (!inputMessage.value.trim()) {
    return;
  }

  const content = inputMessage.value.trim();
  inputMessage.value = '';

  try {
    await aiStore.sendMessage(content, props.noteId);

    // 更新消息列表
    if (aiStore.currentConversation) {
      messages.value = [...aiStore.currentConversation.messages];
    }

    // 滚动到底部
    await nextTick();
    scrollToBottom();
  } catch (error) {
    console.error('发送消息失败:', error);
    ElMessage.error('发送失败，请稍后重试');
  }
};

const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    handleSend();
  }
};

const handleClose = () => {
  visible.value = false;
};

const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

// 监听流式输出，自动滚动
watch(() => aiStore.streamMessage, () => {
  nextTick(() => {
    scrollToBottom();
  });
});

// 关闭时清空对话
watch(visible, (val) => {
  if (!val) {
    messages.value = [];
  }
});
</script>

<style scoped lang="scss">
.assistant-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;

  .header-title {
    font-size: 16px;
    font-weight: 600;
  }
}

.assistant-content {
  display: flex;
  flex-direction: column;
  height: 100%;

  .quick-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
    margin-bottom: 16px;

    .el-button {
      justify-content: flex-start;
      height: 40px;
    }
  }

  .provider-section {
    margin-bottom: 16px;
  }

  .chat-area {
    flex: 1;
    overflow-y: auto;
    border: 1px solid var(--el-border-color);
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 16px;
    background-color: var(--el-bg-color-page);
    min-height: 300px;
    max-height: 400px;

    .chat-messages {
      .message {
        margin-bottom: 16px;

        &:last-child {
          margin-bottom: 0;
        }

        .message-content {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .message-role {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }

          .message-text {
            padding: 10px 14px;
            border-radius: 8px;
            line-height: 1.6;
            white-space: pre-wrap;
            word-break: break-word;
          }

          .cursor {
            display: inline-block;
            animation: blink 1s infinite;
          }
        }

        &.user {
          .message-text {
            background-color: var(--el-color-primary-light-9);
            margin-left: auto;
            max-width: 80%;
          }
        }

        &.assistant {
          .message-text {
            background-color: var(--el-fill-color-light);
            margin-right: auto;
            max-width: 80%;
          }

          &.streaming {
            .message-text {
              background-color: var(--el-fill-color);
            }
          }
        }
      }
    }
  }

  .input-area {
    :deep(.el-textarea__inner) {
      resize: none;
    }
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
</style>
