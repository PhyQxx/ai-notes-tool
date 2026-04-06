<template>
  <div class="comment-panel">
    <div class="panel-header">
      <div class="header-title">
        <el-icon><ChatDotRound /></el-icon>
        <span>评论 ({{ commentCount }})</span>
      </div>
      <el-button
        v-if="!showInput"
        type="primary"
        size="small"
        @click="showInput = true"
      >
        <el-icon><EditPen /></el-icon>
        写评论
      </el-button>
    </div>

    <div v-if="showInput" class="comment-input-wrapper">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        @keydown.ctrl.enter="handleAddComment"
      />
      <div class="input-actions">
        <span class="tip">按 Ctrl+Enter 发送</span>
        <el-button-group>
          <el-button @click="showInput = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleAddComment">
            发送
          </el-button>
        </el-button-group>
      </div>
    </div>

    <div class="comment-list">
      <el-skeleton v-if="loading" :rows="3" animated />

      <el-empty
        v-else-if="comments.length === 0"
        description="暂无评论，快来抢沙发吧"
        :image-size="80"
      />

      <div v-else class="comments">
        <div
          v-for="comment in flatComments"
          :key="comment.id"
          class="comment-item"
          :class="{ 'is-reply': comment.parentId !== 0 }"
        >
          <div class="comment-avatar">
            <el-avatar :size="36" :src="comment.avatar || defaultAvatar">
              {{ comment.nickname?.charAt(0) || comment.username?.charAt(0) || 'U' }}
            </el-avatar>
          </div>

          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-user">{{ comment.nickname || comment.username }}</span>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>

            <div class="comment-text">
              <div v-if="comment.positionText" class="position-text">> {{ comment.positionText }}</div>
              {{ comment.content }}
            </div>

            <div class="comment-actions">
              <el-button text size="small" @click="handleReply(comment)">
                <el-icon><ChatLineRound /></el-icon>
                回复
              </el-button>
              <el-button
                text
                size="small"
                :type="comment.resolveStatus === 'resolved' ? 'success' : 'warning'"
                @click="handleToggleStatus(comment)"
              >
                {{ comment.resolveStatus === 'resolved' ? '✓ 已解决' : '○ 未解决' }}
              </el-button>
              <el-button
                v-if="comment.userId === currentUserId"
                text
                size="small"
                type="danger"
                @click="handleDelete(comment)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>

            <div v-if="replyTo?.id === comment.id" class="reply-input-wrapper">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                :placeholder="`回复 ${comment.nickname || comment.username}...`"
                @keydown.ctrl.enter="handleSubmitReply"
              />
              <div class="input-actions">
                <span class="tip">按 Ctrl+Enter 发送</span>
                <el-button-group>
                  <el-button size="small" @click="replyTo = null">取消</el-button>
                  <el-button
                    type="primary"
                    size="small"
                    :loading="submitting"
                    @click="handleSubmitReply"
                  >
                    回复
                  </el-button>
                </el-button-group>
              </div>
            </div>

            <div v-if="comment.replies && comment.replies.length > 0" class="replies">
              <div
                v-for="reply in comment.replies"
                :key="reply.id"
                class="comment-item is-reply"
              >
                <div class="comment-avatar">
                  <el-avatar :size="28" :src="reply.avatar || defaultAvatar">
                    {{ reply.nickname?.charAt(0) || reply.username?.charAt(0) || 'U' }}
                  </el-avatar>
                </div>

                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-user">{{ reply.nickname || reply.username }}</span>
                    <span class="comment-time">{{ formatTime(reply.createdAt) }}</span>
                  </div>

                  <div class="comment-text">{{ reply.content }}</div>

                  <div class="comment-actions">
                    <el-button text size="small" @click="handleReply(reply)">
                      <el-icon><ChatLineRound /></el-icon>
                      回复
                    </el-button>
                    <el-button
                      v-if="reply.userId === currentUserId"
                      text
                      size="small"
                      type="danger"
                      @click="handleDelete(reply)"
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ChatDotRound,
  EditPen,
  ChatLineRound,
  Delete
} from '@element-plus/icons-vue';
import { listComments, createComment, deleteComment as deleteCommentApi, toggleCommentStatus } from '@/api/comment';
import { useAuthStore } from '@/stores/auth';
import type { Comment } from '@/types';

interface Props {
  noteId: number;
}

const props = defineProps<Props>();

const authStore = useAuthStore();
const currentUserId = authStore.user?.id;
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const comments = ref<Comment[]>([]);
const loading = ref(false);
const showInput = ref(false);
const newComment = ref('');
const submitting = ref(false);
const replyTo = ref<Comment | null>(null);
const replyContent = ref('');

// 展平评论列表（一级评论）
const flatComments = computed(() => {
  return comments.value.filter(c => c.parentId === 0);
});

// 评论数量（包括回复）
const commentCount = computed(() => {
  const count = comments.value.length;
  return count;
});

const formatTime = (time: string) => {
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚';
  }

  // 小于1小时
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000);
    return `${minutes}分钟前`;
  }

  // 小于24小时
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000);
    return `${hours}小时前`;
  }

  // 小于7天
  if (diff < 604800000) {
    const days = Math.floor(diff / 86400000);
    return `${days}天前`;
  }

  // 显示完整日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const extractMentions = (text: string): string => {
  const matches = text.match(/@(\S+)/g);
  return matches ? matches.map(m => m.substring(1)).join(',') : '';
};

const loadComments = async () => {
  loading.value = true;
  try {
    const data = await listComments(props.noteId);
    comments.value = data || [];
  } catch (error) {
    console.error('加载评论失败:', error);
  } finally {
    loading.value = false;
  }
};

const handleAddComment = async () => {
  if (!newComment.value.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }

  try {
    submitting.value = true;
    const comment = await createComment({
      noteId: props.noteId,
      content: newComment.value.trim(),
      mentionUserIds: extractMentions(newComment.value) || undefined
    });

    // 添加到评论列表
    comments.value.unshift(comment);
    newComment.value = '';
    showInput.value = false;
    ElMessage.success('评论成功');
  } catch (error: any) {
    ElMessage.error(error.message || '评论失败');
  } finally {
    submitting.value = false;
  }
};

const handleReply = (comment: Comment) => {
  replyTo.value = comment;
  replyContent.value = '';
};

const handleSubmitReply = async () => {
  if (!replyTo.value) return;

  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容');
    return;
  }

  try {
    submitting.value = true;
    const reply = await createComment({
      noteId: props.noteId,
      parentId: replyTo.value.id,
      content: replyContent.value.trim(),
      mentionUserIds: extractMentions(replyContent.value) || undefined
    });

    // 找到父评论并添加回复
    const parentComment = comments.value.find(c => c.id === replyTo.value?.id);
    if (parentComment) {
      if (!parentComment.replies) {
        parentComment.replies = [];
      }
      parentComment.replies.push({
        id: reply as number,
        noteId: props.noteId,
        userId: 0,
        username: '',
        nickname: '我',
        avatar: '',
        parentId: replyTo.value?.id || 0,
        content: replyContent.value.trim(),
        createdAt: new Date().toISOString(),
        replies: []
      });
    }

    replyContent.value = '';
    replyTo.value = null;
    ElMessage.success('回复成功');
  } catch (error: any) {
    ElMessage.error(error.message || '回复失败');
  } finally {
    submitting.value = false;
  }
};

const handleToggleStatus = async (comment: any) => {
  const newStatus = comment.resolveStatus === 'resolved' ? 'open' : 'resolved';
  try {
    await toggleCommentStatus(comment.id, newStatus);
    comment.resolveStatus = newStatus;
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败');
  }
};

const handleDelete = async (comment: Comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '删除评论', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await deleteCommentApi(comment.id);

    // 如果是父评论，从列表中移除
    if (comment.parentId === 0) {
      comments.value = comments.value.filter(c => c.id !== comment.id);
    } else {
      // 如果是回复，从父评论的回复列表中移除
      const parentComment = comments.value.find(c => c.id === comment.parentId);
      if (parentComment?.replies) {
        parentComment.replies = parentComment.replies.filter(r => r.id !== comment.id);
      }
    }

    ElMessage.success('删除成功');
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};

// 监听noteId变化，重新加载评论
watch(() => props.noteId, () => {
  if (props.noteId) {
    loadComments();
  }
}, { immediate: true });
</script>

<style scoped lang="scss">
.comment-panel {
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 0;
    border-bottom: 1px solid var(--el-border-color);
    margin-bottom: 16px;

    .header-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .comment-input-wrapper,
  .reply-input-wrapper {
    margin-bottom: 20px;
    padding: 16px;
    background-color: var(--el-fill-color-light);
    border-radius: 8px;

    .input-actions {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 12px;

      .tip {
        font-size: 12px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .comment-list {
    .comments {
      .comment-item {
        display: flex;
        gap: var(--nt-spacing-md);
        padding: var(--nt-spacing-md) 0;
        border-bottom: 1px solid var(--nt-border-lighter);

        &:last-child {
          border-bottom: none;
        }

        &.is-reply {
          padding: 12px 0;
          margin-left: 48px;

          .comment-avatar .el-avatar {
            width: 28px;
            height: 28px;
          }
        }

        .comment-avatar {
          flex-shrink: 0;
        }

        .comment-content {
          flex: 1;
          min-width: 0;

          .comment-header {
            display: flex;
            align-items: center;
            gap: 8px;
            margin-bottom: 4px;

            .comment-user {
              font-weight: 500;
              color: var(--el-text-color-primary);
            }

            .comment-time {
              font-size: var(--nt-font-size-caption);
              color: var(--nt-text-quaternary);
            }
          }

          .comment-text {
            color: var(--el-text-color-primary);
            line-height: 1.6;
            margin-bottom: 8px;
            word-break: break-word;

            .position-text {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              background: var(--el-fill-color);
              padding: 4px 8px;
              border-radius: 4px;
              margin-bottom: 4px;
              font-style: italic;
            }
          }

          .comment-actions {
            display: flex;
            gap: 8px;

            .el-button {
              padding: 4px 8px;
              font-size: 13px;
            }
          }
        }
      }

      .replies {
        margin-top: 12px;
        padding-top: 12px;
        border-top: 1px solid var(--el-border-color-lighter);
      }
    }
  }
}
</style>
