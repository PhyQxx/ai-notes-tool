<template>
  <div class="ai-chat-view">
    <!-- 左侧对话历史列表 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <el-button type="primary" @click="handleNewConversation">
          <el-icon><Plus /></el-icon>
          新建对话
        </el-button>
      </div>

      <div class="conversation-list">
        <div
          v-for="conv in aiStore.conversations"
          :key="conv.id"
          :class="['conversation-item', { active: aiStore.currentConversation?.id === conv.id }]"
          @click="handleSelectConversation(conv.id)"
        >
          <div class="conversation-title">{{ conv.title }}</div>
          <div class="conversation-meta">
            <span class="conversation-time">{{ formatTime(conv.updatedAt) }}</span>
            <el-button
              text
              size="small"
              type="danger"
              @click.stop="handleDeleteConversation(conv.id)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>

        <el-empty v-if="aiStore.conversations.length === 0" description="暂无对话" />
      </div>
    </div>

    <!-- 右侧对话区域 -->
    <div class="chat-area">
      <!-- 顶部工具栏 -->
      <div class="chat-header">
        <div class="header-left">
          <span v-if="associatedNote" class="associated-note">
            <el-icon><Document /></el-icon>
            关联笔记: {{ associatedNote }}
          </span>
          <span v-else class="chat-title">
            {{ aiStore.currentConversation?.title || '新对话' }}
          </span>
        </div>

        <div class="header-right">
          <AIProviderSelect
            v-model:provider="aiStore.config.provider"
            v-model:model="aiStore.config.model"
          />
        </div>
      </div>

      <!-- 消息列表 -->
      <div ref="chatContainer" class="messages-container">
        <!-- 空状态 -->
        <div v-if="!aiStore.currentConversation" class="empty-state">
          <div class="empty-icon">
            <el-icon :size="80"><ChatDotRound /></el-icon>
          </div>
          <div class="empty-title">开始新的对话</div>
          <div class="empty-desc">选择一个提供商和模型，然后输入问题开始对话</div>

          <!-- 快捷提示 -->
          <div class="quick-tips">
            <div class="tips-title">💡 试试这些功能：</div>
            <div class="tips-list">
              <div
                v-for="tip in quickTips"
                :key="tip.text"
                class="tip-item"
                @click="handleQuickTip(tip.text)"
              >
                <span class="tip-emoji">{{ tip.emoji }}</span>
                <span class="tip-text">{{ tip.text }}</span>
                <span class="tip-desc">- {{ tip.desc }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 消息内容 -->
        <template v-else>
          <div
            v-for="(msg, index) in aiStore.currentConversation.messages"
            :key="index"
            :class="['message', msg.role]"
          >
            <div class="message-avatar">
              <el-avatar v-if="msg.role === 'user'" :size="36" :src="userAvatar">
                {{ user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <el-avatar v-else :size="36" :style="{ backgroundColor: '#67c23a' }">
                <el-icon><ChatDotRound /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="message-name">{{ msg.role === 'user' ? '我' : 'AI助手' }}</span>
                <span v-if="index > 0 && msg.role === 'assistant'" class="message-model">
                  {{ aiStore.currentConversation.aiModel }}
                </span>
              </div>
              <div class="message-text">
                <MarkdownRenderer :content="msg.content" />
              </div>
            </div>
          </div>

          <!-- 流式输出 -->
          <div v-if="aiStore.isStreaming" class="message assistant streaming">
            <div class="message-avatar">
              <el-avatar :size="36" :style="{ backgroundColor: '#67c23a' }">
                <el-icon><ChatDotRound /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="message-name">AI助手</span>
                <span class="message-model">{{ aiStore.currentConversation?.aiModel }}</span>
              </div>
              <div class="message-text">
                {{ aiStore.streamMessage }}
                <span class="cursor">|</span>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- 输入区域 -->
      <div class="input-container">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="4"
          placeholder="输入问题，按 Enter 发送，Shift + Enter 换行"
          @keydown="handleKeyDown"
        />
        <div class="input-footer">
          <span class="hint">Enter 发送 · Shift + Enter 换行</span>
          <el-button
            type="primary"
            :loading="aiStore.isStreaming"
            :disabled="!inputMessage.trim()"
            @click="handleSend"
          >
            <el-icon v-if="!aiStore.isStreaming"><Promotion /></el-icon>
            {{ aiStore.isStreaming ? '生成中...' : '发送' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Plus,
  Delete,
  Document,
  ChatDotRound,
  Promotion
} from '@element-plus/icons-vue';
import { useAIStore } from '@/stores/ai';
import { useNoteStore } from '@/stores/note';
import { useAuthStore } from '@/stores/auth';
import AIProviderSelect from '@/components/ai/AIProviderSelect.vue';
import MarkdownRenderer from '@/components/common/MarkdownRenderer.vue';

const route = useRoute();
const router = useRouter();
const aiStore = useAIStore();
const noteStore = useNoteStore();
const authStore = useAuthStore();

const inputMessage = ref('');
const chatContainer = ref<HTMLElement>();
const user = authStore.user;
const userAvatar = user?.avatar || '';

const quickTips = [
  { emoji: '📝', text: '帮我总结这篇笔记', desc: '一键生成摘要' },
  { emoji: '✨', text: '优化这段文字', desc: '润色优化内容' },
  { emoji: '📖', text: '继续写下去', desc: 'AI智能续写' },
  { emoji: '❓', text: '解释这个概念', desc: 'AI问答' }
];

// 关联的笔记ID
const noteId = computed(() => {
  return parseInt(route.query.noteId as string) || 0;
});

// 关联的笔记标题
const associatedNote = computed(() => {
  if (noteId.value && noteStore.currentNote) {
    return noteStore.currentNote.title;
  }
  return '';
});

const handleNewConversation = () => {
  aiStore.newConversation();
  inputMessage.value = '';
};

const handleSelectConversation = async (id: number) => {
  try {
    await aiStore.fetchConversation(id);
    await nextTick();
    scrollToBottom();
  } catch (error) {
    console.error('加载对话失败:', error);
    ElMessage.error('加载对话失败');
  }
};

const handleDeleteConversation = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await aiStore.deleteConversation(id);
    ElMessage.success('删除成功');

    if (aiStore.currentConversation?.id === id) {
      handleNewConversation();
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除对话失败:', error);
      ElMessage.error('删除失败');
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
    await aiStore.sendMessage(content, noteId.value || undefined);

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

const handleQuickTip = (text: string) => {
  inputMessage.value = text;
};

const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
  }
};

const formatTime = (dateStr: string) => {
  if (!dateStr) return '';
  // Handle MySQL datetime format "2026-04-03 08:10:32" and ISO format
  const date = new Date(dateStr.replace(' ', 'T'));
  if (isNaN(date.getTime())) return '';
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return date.toLocaleDateString();
};

// 监听流式输出，自动滚动
watch(() => aiStore.streamMessage, () => {
  nextTick(() => {
    scrollToBottom();
  });
});

// 监听当前对话变化，自动滚动
watch(() => aiStore.currentConversation?.messages, () => {
  nextTick(() => {
    scrollToBottom();
  });
}, { deep: true });

onMounted(async () => {
  // 加载对话列表
  try {
    await aiStore.fetchConversations();
  } catch (error) {
    console.error('加载对话列表失败:', error);
  }

  // 加载AI配置
  try {
    await aiStore.fetchConfig();
  } catch (error) {
    console.error('加载AI配置失败:', error);
  }

  // 如果有关联的笔记ID，加载笔记信息
  if (noteId.value) {
    try {
      await noteStore.fetchNote(noteId.value);
    } catch (error) {
      console.error('加载笔记信息失败:', error);
    }
  }
});
</script>

<style scoped lang="scss">
.ai-chat-view {
  display: flex;
  height: 100vh;
  background-color: var(--el-bg-color-page);

  .sidebar {
    width: 280px;
    background-color: var(--el-bg-color);
    border-right: 1px solid var(--el-border-color);
    display: flex;
    flex-direction: column;

    .sidebar-header {
      padding: 16px;
      border-bottom: 1px solid var(--el-border-color);

      .el-button {
        width: 100%;
      }
    }

    .conversation-list {
      flex: 1;
      overflow-y: auto;
      padding: 8px;

      .conversation-item {
        padding: 12px;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.2s;
        margin-bottom: 8px;

        &:hover {
          background-color: var(--el-fill-color-light);
        }

        &.active {
          background-color: var(--el-color-primary-light-9);
        }

        .conversation-title {
          font-size: 14px;
          font-weight: 500;
          color: var(--el-text-color-primary);
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .conversation-meta {
          display: flex;
          align-items: center;
          justify-content: space-between;

          .conversation-time {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }
        }
      }

      .el-empty {
        margin-top: 40px;
      }
    }
  }

  .chat-area {
    flex: 1;
    display: flex;
    flex-direction: column;

    .chat-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 24px;
      background-color: var(--el-bg-color);
      border-bottom: 1px solid var(--el-border-color);

      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .associated-note {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 14px;
          color: var(--el-color-primary);
          background-color: var(--el-color-primary-light-9);
          padding: 6px 12px;
          border-radius: 16px;
        }

        .chat-title {
          font-size: 16px;
          font-weight: 600;
        }
      }

      .header-right {
        width: 300px;
      }
    }

    .messages-container {
      flex: 1;
      overflow-y: auto;
      padding: 24px;

      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        text-align: center;

        .empty-icon {
          color: var(--el-color-primary);
          margin-bottom: 24px;
        }

        .empty-title {
          font-size: 20px;
          font-weight: 600;
          color: var(--el-text-color-primary);
          margin-bottom: 8px;
        }

        .empty-desc {
          font-size: 14px;
          color: var(--el-text-color-secondary);
          margin-bottom: 32px;
        }

        .quick-tips {
          background-color: var(--el-bg-color);
          border-radius: 12px;
          padding: 24px;
          box-shadow: var(--el-box-shadow-light);

          .tips-title {
            font-size: 16px;
            font-weight: 600;
            color: var(--el-text-color-primary);
            margin-bottom: 16px;
            text-align: left;
          }

          .tips-list {
            display: grid;
            gap: 12px;
            text-align: left;

            .tip-item {
              padding: 12px;
              border-radius: 8px;
              cursor: pointer;
              transition: all 0.2s;

              &:hover {
                background-color: var(--el-fill-color-light);
              }

              .tip-emoji {
                font-size: 18px;
                margin-right: 8px;
              }

              .tip-text {
                font-size: 14px;
                color: var(--el-text-color-primary);
                margin-right: 8px;
              }

              .tip-desc {
                font-size: 12px;
                color: var(--el-text-color-secondary);
              }
            }
          }
        }
      }

      .message {
        display: flex;
        gap: 12px;
        margin-bottom: 24px;

        &:last-child {
          margin-bottom: 0;
        }

        .message-avatar {
          flex-shrink: 0;
        }

        .message-content {
          flex: 1;
          max-width: 800px;

          .message-header {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 4px;

            .message-name {
              font-size: 14px;
              font-weight: 500;
              color: var(--el-text-color-primary);
            }

            .message-model {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              background-color: var(--el-fill-color);
              padding: 2px 8px;
              border-radius: 4px;
            }
          }

          .message-text {
            padding: 12px 16px;
            border-radius: 12px;
            line-height: 1.6;
            white-space: pre-wrap;
            word-break: break-word;

            .cursor {
              display: inline-block;
              animation: blink 1s infinite;
            }
          }
        }

        &.user {
          flex-direction: row-reverse;

          .message-content {
            .message-text {
              background-color: var(--el-color-primary);
              color: white;
            }
          }
        }

        &.assistant {
          .message-content {
            .message-text {
              background-color: var(--el-fill-color-light);
              color: var(--el-text-color-primary);
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

    .input-container {
      background-color: var(--el-bg-color);
      border-top: 1px solid var(--el-border-color);
      padding: 16px 24px;

      :deep(.el-textarea__inner) {
        resize: none;
        font-size: 14px;
      }

      .input-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-top: 12px;

        .hint {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
</style>
