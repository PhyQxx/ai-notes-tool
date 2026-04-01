<template>
  <div class="home-view">
    <div class="welcome-section">
      <h2>欢迎回来，{{ user?.nickname || '用户' }}！</h2>
      <p>开始记录您的想法和灵感</p>
    </div>

    <el-row :gutter="24" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #ecf5ff;">
              <el-icon color="#409eff" :size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalNotes }}</div>
              <div class="stat-label">笔记总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #fef0f0;">
              <el-icon color="#f56c6c" :size="32"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ favoriteCount }}</div>
              <div class="stat-label">收藏笔记</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #f0f9ff;">
              <el-icon color="#67c23a" :size="32"><FolderOpened /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ folderCount }}</div>
              <div class="stat-label">文件夹</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="recent-notes-section">
      <div class="section-header">
        <h3>最近编辑</h3>
        <el-button type="primary" link @click="router.push('/notes')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      </div>

      <el-empty v-else-if="recentNotes.length === 0" description="暂无笔记" />

      <div v-else class="recent-notes-list">
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
              <el-tag v-if="note.tags.length > 0" size="small">{{ note.tags[0] }}</el-tag>
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

const totalNotes = computed(() => noteStore.notes.length);
const favoriteCount = computed(() => noteStore.notes.filter(n => n.isFavorite).length);
const folderCount = computed(() => noteStore.folders.length);

const recentNotes = computed(() => {
  return noteStore.notes.slice(0, 6);
});

const formatDate = (date: string) => {
  return dayjs(date).fromNow();
};

onMounted(async () => {
  loading.value = true;
  try {
    await noteStore.fetchNotes({ page: 1, size: 10 });
  } catch (error) {
    console.error('加载笔记失败:', error);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="scss">
.home-view {
  max-width: 1200px;
  margin: 0 auto;

  .welcome-section {
    margin-bottom: 32px;

    h2 {
      margin: 0 0 8px 0;
      font-size: 28px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }

    p {
      margin: 0;
      font-size: 16px;
      color: var(--el-text-color-secondary);
    }
  }

  .stats-row {
    margin-bottom: 32px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 64px;
          height: 64px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .stat-info {
          .stat-value {
            font-size: 32px;
            font-weight: 600;
            color: var(--el-text-color-primary);
            line-height: 1;
            margin-bottom: 8px;
          }

          .stat-label {
            font-size: 14px;
            color: var(--el-text-color-secondary);
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
}
</style>
