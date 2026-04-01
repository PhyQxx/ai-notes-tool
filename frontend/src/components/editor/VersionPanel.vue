<template>
  <el-drawer
    v-model="visible"
    title="版本历史"
    size="400px"
    direction="rtl"
    :before-close="handleClose"
  >
    <template #header>
      <div class="version-header">
        <span>版本历史</span>
        <el-button
          type="primary"
          size="small"
          @click="handleCreateSnapshot"
        >
          <el-icon><Camera /></el-icon>
          创建快照
        </el-button>
      </div>
    </template>

    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <el-empty v-else-if="versions.length === 0" description="暂无历史版本" />

    <div v-else class="version-list">
      <div
        v-for="version in versions"
        :key="version.id"
        :class="['version-item', { 'current': isCurrentVersion(version) }]"
      >
        <div class="version-info">
          <div class="version-title">
            <span class="version-no">版本 #{{ version.versionNo }}</span>
            <el-tag v-if="isCurrentVersion(version)" type="success" size="small">当前</el-tag>
          </div>
          <div class="version-time">{{ formatTime(version.createdAt) }}</div>
          <div v-if="version.remark" class="version-remark">{{ version.remark }}</div>
        </div>

        <div class="version-actions">
          <el-button
            text
            size="small"
            @click="handleViewVersion(version)"
          >
            查看
          </el-button>
          <el-button
            v-if="!isCurrentVersion(version)"
            text
            type="primary"
            size="small"
            @click="handleRestoreVersion(version)"
          >
            恢复
          </el-button>
          <el-popconfirm
            title="确定删除此版本吗？"
            @confirm="handleDeleteVersion(version)"
          >
            <template #reference>
              <el-button
                v-if="!isCurrentVersion(version)"
                text
                type="danger"
                size="small"
              >
                删除
              </el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <!-- 查看版本对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      :title="`版本 #${viewVersion?.versionNo}`"
      width="80%"
      top="5vh"
    >
      <div class="version-view-dialog">
        <div class="view-header">
          <span class="view-title">{{ viewVersion?.title }}</span>
          <span class="view-time">{{ viewVersion ? formatTime(viewVersion.createdAt) : '' }}</span>
        </div>
        <div v-if="viewVersion?.remark" class="view-remark">{{ viewVersion.remark }}</div>
        <div class="view-content">
          <MarkdownRenderer :content="viewVersion?.content || ''" />
        </div>
      </div>
    </el-dialog>

    <!-- 创建快照对话框 -->
    <el-dialog
      v-model="snapshotDialogVisible"
      title="创建版本快照"
      width="500px"
    >
      <el-form :model="snapshotForm" label-width="80px">
        <el-form-item label="版本备注">
          <el-input
            v-model="snapshotForm.remark"
            type="textarea"
            :rows="4"
            placeholder="输入版本备注（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="snapshotDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmSnapshot" :loading="saving">确定</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useNoteStore } from '@/stores/note';
import MarkdownRenderer from '@/components/common/MarkdownRenderer.vue';
import type { NoteVersion } from '@/types';

const props = defineProps<{
  modelValue: boolean;
  noteId: number;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'restore': [version: NoteVersion];
}>();

const noteStore = useNoteStore();

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

const loading = ref(false);
const saving = ref(false);
const versions = computed(() => noteStore.versions);
const currentNote = computed(() => noteStore.currentNote);

const viewDialogVisible = ref(false);
const viewVersion = ref<NoteVersion | null>(null);

const snapshotDialogVisible = ref(false);
const snapshotForm = ref({
  remark: ''
});

const isCurrentVersion = (version: NoteVersion) => {
  return currentNote.value?.title === version.title &&
         currentNote.value?.content === version.content;
};

const formatTime = (time: string) => {
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days === 1) return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  if (days === 2) return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  if (days < 7) return `${days}天前`;

  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const handleViewVersion = (version: NoteVersion) => {
  viewVersion.value = version;
  viewDialogVisible.value = true;
};

const handleRestoreVersion = async (version: NoteVersion) => {
  try {
    await ElMessageBox.confirm(
      '恢复到此版本将覆盖当前内容，确定要继续吗？',
      '确认恢复',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    );

    await noteStore.restoreVersion(props.noteId, version.id);
    ElMessage.success('版本已恢复');
    emit('restore', version);
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('恢复版本失败:', error);
      ElMessage.error(error.message || '恢复版本失败');
    }
  }
};

const handleDeleteVersion = async (version: NoteVersion) => {
  try {
    await noteStore.deleteVersion(props.noteId, version.id);
    ElMessage.success('版本已删除');
  } catch (error) {
    console.error('删除版本失败:', error);
    ElMessage.error('删除版本失败');
  }
};

const handleCreateSnapshot = () => {
  snapshotForm.value.remark = '';
  snapshotDialogVisible.value = true;
};

const handleConfirmSnapshot = async () => {
  saving.value = true;
  try {
    await noteStore.createVersion(props.noteId, snapshotForm.value.remark || undefined);
    ElMessage.success('快照已创建');
    snapshotDialogVisible.value = false;
  } catch (error) {
    console.error('创建快照失败:', error);
    ElMessage.error('创建快照失败');
  } finally {
    saving.value = false;
  }
};

const handleClose = () => {
  visible.value = false;
};

const loadVersions = async () => {
  if (!props.noteId) return;
  loading.value = true;
  try {
    await noteStore.fetchVersions(props.noteId);
  } catch (error) {
    console.error('加载版本列表失败:', error);
  } finally {
    loading.value = false;
  }
};

watch(() => props.modelValue, (value) => {
  if (value) {
    loadVersions();
  }
});
</script>

<style scoped lang="scss">
.version-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  font-size: 16px;
  font-weight: 600;
}

.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: var(--el-text-color-secondary);
}

.version-list {
  .version-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    margin-bottom: 12px;
    background-color: var(--el-bg-color-page);
    border-radius: 8px;
    border-left: 3px solid transparent;
    transition: all 0.2s;

    &:hover {
      background-color: var(--el-fill-color-light);
    }

    &.current {
      border-left-color: var(--el-color-success);
      background-color: var(--el-color-success-light-9);
    }

    .version-info {
      flex: 1;
      min-width: 0;

      .version-title {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 4px;

        .version-no {
          font-weight: 600;
          color: var(--el-text-color-primary);
        }
      }

      .version-time {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-bottom: 4px;
      }

      .version-remark {
        font-size: 13px;
        color: var(--el-text-color-regular);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .version-actions {
      display: flex;
      gap: 4px;
      flex-shrink: 0;
      margin-left: 12px;
    }
  }
}

.version-view-dialog {
  .view-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--el-border-color);

    .view-title {
      font-size: 18px;
      font-weight: 600;
    }

    .view-time {
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }

  .view-remark {
    padding: 12px;
    margin-bottom: 16px;
    background-color: var(--el-fill-color-light);
    border-radius: 6px;
    color: var(--el-text-color-secondary);
  }

  .view-content {
    max-height: 60vh;
    overflow-y: auto;
    padding: 16px;
    background-color: var(--el-bg-color-page);
    border-radius: 8px;
  }
}
</style>
