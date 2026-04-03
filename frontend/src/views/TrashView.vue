<template>
  <div class="trash-view">
    <div class="trash-header">
      <h2>🗑️ 回收站</h2>
      <el-button type="danger" plain :disabled="notes.length === 0" @click="handleEmptyTrash">
        <el-icon><Delete /></el-icon>
        清空回收站
      </el-button>
    </div>

    <div class="trash-tip">
      <el-alert type="info" :closable="false" show-icon>
        回收站中的笔记将在 30 天后自动清理。你可以随时恢复或彻底删除。
      </el-alert>
    </div>

    <el-table v-loading="loading" :data="notes" stripe empty-text="回收站为空">
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="{ row }">
          <span class="note-title">{{ row.title || '无标题' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="tags" label="标签" width="150">
        <template #default="{ row }">
          <el-tag v-for="tag in (row.tags || '').split(',').filter(Boolean)" :key="tag" size="small" class="tag-item">
            {{ tag }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deletedAt" label="删除时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.deletedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleRestore(row)">
            恢复
          </el-button>
          <el-button type="danger" link size="small" @click="handlePermanentDelete(row)">
            彻底删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="total > pageSize" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="fetchTrash"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Delete } from '@element-plus/icons-vue';
import { listTrash, restoreNote, permanentDeleteNote, emptyTrash } from '@/api/trash';

const notes = ref<any[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = 20;
const total = ref(0);

const formatTime = (t: string) => {
  if (!t) return '-';
  return new Date(t).toLocaleString('zh-CN');
};

const fetchTrash = async () => {
  loading.value = true;
  try {
    const res = await listTrash(currentPage.value, pageSize);
    const data = (res as any).data || res;
    notes.value = data.records || [];
    total.value = data.total || 0;
  } catch (e: any) {
    ElMessage.error(e.message || '加载回收站失败');
  } finally {
    loading.value = false;
  }
};

const handleRestore = async (row: any) => {
  try {
    await restoreNote(row.id);
    ElMessage.success('恢复成功');
    fetchTrash();
  } catch (e: any) {
    ElMessage.error(e.message || '恢复失败');
  }
};

const handlePermanentDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(`确定要彻底删除「${row.title || '无标题'}」吗？此操作不可恢复。`, '彻底删除', {
      confirmButtonText: '彻底删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await permanentDeleteNote(row.id);
    ElMessage.success('已彻底删除');
    fetchTrash();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败');
  }
};

const handleEmptyTrash = async () => {
  try {
    await ElMessageBox.confirm('确定要清空回收站吗？所有笔记将被彻底删除，此操作不可恢复。', '清空回收站', {
      confirmButtonText: '清空',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await emptyTrash();
    ElMessage.success('回收站已清空');
    fetchTrash();
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '清空失败');
  }
};

onMounted(() => {
  fetchTrash();
});
</script>

<style scoped lang="scss">
.trash-view {
  .trash-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h2 {
      margin: 0;
      font-size: 20px;
    }
  }

  .trash-tip {
    margin-bottom: 16px;
  }

  .note-title {
    font-weight: 500;
  }

  .tag-item {
    margin-right: 4px;
    margin-bottom: 4px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 16px;
  }
}
</style>
