<template>
  <div class="canvas-list-view">
    <div class="view-header">
      <div class="header-left">
        <h2>知识画布</h2>
        <p class="subtitle">自由排列笔记，构建知识网络</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleCreateCanvas">
        创建画布
      </el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="canvas in canvases" :key="canvas.id">
        <el-card class="canvas-card" shadow="hover" @click="router.push(`/canvas/${canvas.id}`)">
          <div class="canvas-preview">
            <el-icon :size="48"><Monitor /></el-icon>
          </div>
          <div class="canvas-info">
            <h4 class="canvas-title">{{ canvas.title }}</h4>
            <div class="canvas-meta">
              <span>更新于 {{ formatTime(canvas.updatedAt) }}</span>
              <el-dropdown trigger="click" @click.stop>
                <el-icon class="more-icon"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :icon="Edit">重命名</el-dropdown-item>
                    <el-dropdown-item :icon="Delete" type="danger" @click="handleDeleteCanvas(canvas.id)">
                      删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col v-if="!loading && canvases.length === 0" :span="24">
        <el-empty description="暂无画布，点击右上角创建一个吧" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Monitor, MoreFilled, Edit, Delete } from '@element-plus/icons-vue';
import { listCanvases, createCanvas, deleteCanvas, type Canvas } from '@/api/canvas';

const router = useRouter();
const canvases = ref<Canvas[]>([]);
const loading = ref(false);

const loadCanvases = async () => {
  loading.value = true;
  try {
    const res = await listCanvases();
    canvases.value = res as any;
  } catch (error) {
    ElMessage.error('加载画布失败');
  } finally {
    loading.value = false;
  }
};

const handleCreateCanvas = async () => {
  try {
    const { value: title } = await ElMessageBox.prompt('请输入画布名称', '创建画布', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '未命名画布',
      inputValue: '我的新画布'
    });
    
    if (title) {
      const res = await createCanvas(title);
      ElMessage.success('创建成功');
      router.push(`/canvas/${(res as any).id}`);
    }
  } catch (error) {
    // Cancelled
  }
};

const handleDeleteCanvas = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个画布吗？', '提示', {
      type: 'warning'
    });
    await deleteCanvas(id);
    ElMessage.success('已删除');
    loadCanvases();
  } catch (error) {
    // Cancelled
  }
};

const formatTime = (time: string) => {
  return new Date(time).toLocaleString();
};

onMounted(loadCanvases);
</script>

<style scoped lang="scss">
.canvas-list-view {
  .view-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h2 { margin: 0; font-size: 24px; font-weight: 600; }
    .subtitle { margin: 4px 0 0 0; color: var(--el-text-color-secondary); font-size: 14px; }
  }

  .canvas-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: transform 0.2s;
    border-radius: 12px;
    
    &:hover {
      transform: translateY(-4px);
    }

    .canvas-preview {
      height: 140px;
      background-color: var(--el-fill-color-light);
      display: flex;
      align-items: center;
      justify-content: center;
      color: var(--el-text-color-placeholder);
      border-radius: 8px 8px 0 0;
    }

    .canvas-info {
      padding: 12px;

      .canvas-title {
        margin: 0 0 8px 0;
        font-size: 16px;
        font-weight: 500;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .canvas-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 12px;
        color: var(--el-text-color-secondary);

        .more-icon {
          cursor: pointer;
          padding: 4px;
          border-radius: 4px;
          &:hover { background-color: var(--el-fill-color); }
        }
      }
    }
  }
}
</style>
