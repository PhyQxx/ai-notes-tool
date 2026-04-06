<template>
  <div class="home-view">
    <div class="welcome-section">
      <h2>欢迎回来，{{ user?.nickname || '用户' }}！</h2>
      <p>开始记录您的想法和灵感</p>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-icon stat-icon--blue">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ totalNotes }}</div>
            <div class="stat-label">笔记总数</div>
          </div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-icon stat-icon--red">
            <el-icon :size="32"><Star /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ favoriteCount }}</div>
            <div class="stat-label">收藏笔记</div>
          </div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-icon stat-icon--green">
            <el-icon :size="32"><FolderOpened /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ folderCount }}</div>
            <div class="stat-label">文件夹</div>
          </div>
        </div>
      </div>
    </div>

    <div class="recent-notes-section">
      <div class="section-header">
        <h3>最近编辑</h3>
        <el-button type="primary" link @click="router.push('/notes')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-if="loading" class="skeleton-list">
        <SkeletonNoteCard v-for="i in 5" :key="i" />
      </div>

      <EmptyState
        v-else-if="error"
        :type="errorType"
        :message="errorMsg"
        @retry="loadNotes"
      />

      <EmptyState
        v-else-if="recentNotes.length === 0"
        description="还没有笔记，开始记录你的第一个想法吧！"
        secondary-text="查看模板"
        @secondary="router.push('/notes/new')"
      />

      <div v-else class="recent-notes-list" ref="homeScrollContainer" @scroll="handleHomeScroll">
        <el-card
          v-for="note in recentNotes"
          :key="note.id"
          class="note-card"
          shadow="hover"
          @click="router.push(`/notes/${note.id}`)"
        >
          <div class="note-card-content">
            <div class="note-header">
              <h4 class="note-title">{{ note.title || '无标题' }}</h4>
              <div class="note-actions">
                <el-icon v-if="note.isFavorite" color="#f56c6c"><Star /></el-icon>
                <el-icon v-if="note.isTop" color="#409eff"><Top /></el-icon>
              </div>
            </div>
            <p class="note-preview">{{ note.content.substring(0, 100) }}...</p>
            <div class="note-meta">
              <span class="note-time">{{ formatDate(note.updatedAt) }}</span>
              <div class="note-tags" v-if="note.tags && note.tags.length > 0">
                <span v-for="tag in note.tags.slice(0, 3)" :key="tag" class="note-tag">{{ tag }}</span>
              </div>
            </div>
          </div>
        </el-card>
        <div v-if="homeLoadingMore" class="load-more">
          <el-icon class="is-loading" :size="20" /><span>加载中...</span>
        </div>
        <div v-else-if="!homeHasMore && recentNotes.length > 0" class="load-more no-more">
          没有更多了
        </div>
      </div>
    </div>

    <!-- 推荐笔记 -->
    <div v-if="recommendNotes.length > 0" class="recommend-section">
      <div class="section-header">
        <h3>猜你喜欢</h3>
      </div>
      <div class="recommend-list">
        <el-card
          v-for="note in recommendNotes"
          :key="note.id"
          class="note-card"
          shadow="hover"
          @click="router.push(`/notes/${note.id}`)"
        >
          <div class="note-card-content">
            <div class="note-header">
              <h4 class="note-title">{{ note.title || '无标题' }}</h4>
            </div>
            <p class="note-preview">{{ note.content.substring(0, 100) }}...</p>
            <div class="note-meta">
              <span class="note-time">{{ formatDate(note.updatedAt) }}</span>
              <div class="note-tags" v-if="note.tags && note.tags.length > 0">
                <span v-for="tag in note.tags.slice(0, 3)" :key="tag" class="note-tag">{{ tag }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

import { useAuthStore } from '@/stores/auth';
import { useNoteStore } from '@/stores/note';
import { getRecommendNotes } from '@/api/note';
import EmptyState from '@/components/common/EmptyState.vue';
import ErrorState from '@/components/common/ErrorState.vue';
import SkeletonNoteCard from '@/components/common/SkeletonNoteCard.vue';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

const router = useRouter();
const authStore = useAuthStore();
const noteStore = useNoteStore();

const user = authStore.user;
const loading = ref(false);
const error = ref(false);
const errorType = ref<'network' | 'permission' | 'data'>('network');
const errorMsg = ref('');
const homeScrollContainer = ref<HTMLElement | null>(null);
const homeLoadingMore = ref(false);
const homeHasMore = ref(true);
const recommendNotes = ref<any[]>([]);

const totalNotes = computed(() => noteStore.total);
const favoriteCount = computed(() => noteStore.notes.filter(n => n.isFavorite).length);
const folderCount = computed(() => noteStore.folders.length);

const recentNotes = computed(() => {
  return noteStore.notes;
});

const formatDate = (date: string) => {
  return dayjs(date).fromNow();
};

// Retry handler for ErrorState - must be defined at setup level (not inside onMounted)
const loadNotes = async () => {
  error.value = false;
  errorMsg.value = '';
  loading.value = true;
  try {
    await noteStore.fetchNotes({ page: 1, size: 20, sortBy: 'updatedAt' });
    homeHasMore.value = noteStore.notes.length < noteStore.total;
  } catch (err: any) {
    error.value = true;
    errorMsg.value = err?.message || '加载失败';
    errorType.value = 'network';
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  await loadNotes();
  // 加载推荐笔记
  getRecommendNotes().then((data: any) => {
    recommendNotes.value = data || [];
  }).catch(() => {});
});

const handleHomeScroll = () => {
  const el = homeScrollContainer.value;
  if (!el) return;
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 50 && !homeLoadingMore.value && homeHasMore.value) {
    homeLoadingMore.value = true;
    const nextPage = noteStore.currentPage + 1;
    noteStore.fetchNotes({ page: nextPage, size: 20, sortBy: 'updatedAt', append: true })
      .then(() => { homeHasMore.value = noteStore.notes.length < noteStore.total; })
      .catch(() => {})
      .finally(() => { homeLoadingMore.value = false; });
  }
};
</script>

<style scoped lang="scss">
.home-view {
  max-width: 1200px;
  margin: 0 auto;

  .welcome-section {
    margin-bottom: var(--nt-spacing-xl);
    background: linear-gradient(135deg, #5B7FFF 0%, #8B5CF6 100%);
    border-radius: var(--nt-radius-xl);
    padding: 32px 32px 28px;
    position: relative;
    overflow: hidden;

    // Decorative circles
    &::before {
      content: '';
      position: absolute;
      width: 200px;
      height: 200px;
      background: rgba(255, 255, 255, 0.08);
      border-radius: 50%;
      top: -60px;
      right: 40px;
      pointer-events: none;
    }

    &::after {
      content: '';
      position: absolute;
      width: 120px;
      height: 120px;
      background: rgba(255, 255, 255, 0.06);
      border-radius: 50%;
      bottom: -30px;
      right: 120px;
      pointer-events: none;
    }

    h2 {
      margin: 0 0 8px 0;
      font-size: 28px;
      font-weight: 700;
      color: #ffffff;
      position: relative;
      z-index: 1;
    }

    p {
      margin: 0;
      font-size: var(--nt-font-size-body);
      color: rgba(255, 255, 255, 0.85);
      position: relative;
      z-index: 1;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: var(--nt-spacing-lg);
    margin-bottom: var(--nt-spacing-xl);

    .stat-card {
      background: var(--nt-bg-elevated);
      border: 1px solid var(--nt-border-default);
      border-radius: var(--nt-radius-lg);
      padding: var(--nt-spacing-lg);
      transition: transform 0.2s, box-shadow 0.2s;
      cursor: default;

      &:hover {
        transform: translateY(-4px);
        box-shadow: var(--shadow-brand-hover);
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: var(--nt-spacing-md);

        .stat-icon {
          width: 56px;
          height: 56px;
          border-radius: var(--nt-radius-md);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;

          &--blue { background-color: var(--nt-primary-bg); color: var(--nt-primary); }
          &--red { background-color: var(--nt-danger-bg); color: var(--nt-danger); }
          &--green { background-color: var(--nt-success-bg); color: var(--nt-success); }
        }

        .stat-info {
          .stat-value {
            font-size: 28px;
            font-weight: 700;
            color: var(--nt-text-primary);
            line-height: 1;
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: var(--nt-font-size-body);
            color: var(--nt-text-tertiary);
          }
        }
      }
    }
  }

  .recent-notes-section {
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20px;

      h3 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }

    .loading-container {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 60px 0;
      color: var(--el-text-color-secondary);
    }

    .recent-notes-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;

      .note-card {
        cursor: pointer;
        transition: transform 0.2s;

        &:hover {
          transform: translateY(-4px);
        }

        .note-card-content {
          .note-header {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            margin-bottom: 12px;

            .note-title {
              margin: 0;
              font-size: 16px;
              font-weight: 600;
              color: var(--el-text-color-primary);
              flex: 1;
            }

            .note-actions {
              display: flex;
              gap: 4px;
            }
          }

          .note-preview {
            margin: 0 0 12px 0;
            font-size: 14px;
            color: var(--el-text-color-secondary);
            line-height: 1.6;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }

          .note-meta {
            display: flex;
            align-items: center;
            justify-content: space-between;

            .note-time {
              font-size: 12px;
              color: var(--el-text-color-placeholder);
            }
          }
        }
      }
    }
  }

  .load-more {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 24px 0;
    color: var(--el-text-color-secondary);
    font-size: 14px;
    grid-column: 1 / -1;

    &.no-more {
      color: var(--el-text-color-placeholder);
    }
  }

  .recommend-section {
    margin-top: 32px;
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20px;
      h3 { margin: 0; font-size: 20px; font-weight: 600; color: var(--el-text-color-primary); }
    }
    .recommend-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 20px;
      .note-card { cursor: pointer; transition: transform 0.2s; &:hover { transform: translateY(-4px); }
        .note-card-content {
          .note-header { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 12px;
            .note-title { margin: 0; font-size: 16px; font-weight: 600; color: var(--el-text-color-primary); flex: 1; }
          }
          .note-preview { margin: 0 0 12px 0; font-size: 14px; color: var(--el-text-color-secondary); line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
          .note-meta { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 8px;
            .note-time { font-size: 12px; color: var(--el-text-color-placeholder); }
            .note-tags { display: flex; flex-wrap: wrap; gap: 4px;
              .note-tag { padding: 1px 6px; background: var(--brand-primary-light-5); color: var(--brand-primary); border-radius: 4px; font-size: 11px; font-weight: 500; }
            }
          }
        }
      }
    }
  }
}
</style>
