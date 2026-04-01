<template>
  <div class="space-list-view">
    <div class="page-header">
      <h2 class="page-title">团队空间</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        创建空间
      </el-button>
    </div>

    <div v-if="spaceStore.loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="spaceStore.spaces.length === 0" class="empty-container">
      <el-empty description="暂无团队空间">
        <el-button type="primary" @click="showCreateDialog = true">
          创建第一个空间
        </el-button>
      </el-empty>
    </div>

    <div v-else class="space-grid">
      <div
        v-for="space in spaceStore.spaces"
        :key="space.id"
        class="space-card"
        @click="handleEnterSpace(space.id)"
      >
        <div class="space-card-header">
          <div class="space-icon">
            <el-icon :size="32" color="#409eff">
              <FolderOpened />
            </el-icon>
          </div>
          <el-tag :type="getRoleType(space.myRole)" size="small">
            {{ getRoleLabel(space.myRole) }}
          </el-tag>
        </div>

        <div class="space-card-body">
          <h3 class="space-name">{{ space.name }}</h3>
          <p class="space-description">{{ space.description || '暂无描述' }}</p>

          <div class="space-meta">
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>{{ space.memberCount }} 成员</span>
            </div>
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(space.createdAt) }}</span>
            </div>
          </div>
        </div>

        <div class="space-card-footer">
          <el-button type="primary" size="small" @click.stop="handleEnterSpace(space.id)">
            进入空间
          </el-button>
          <el-button-group v-if="canManage(space.myRole)">
            <el-button size="small" @click.stop="handleSettings(space.id)">
              <el-icon><Setting /></el-icon>
            </el-button>
          </el-button-group>
          <el-button
            v-if="space.myRole !== 'owner'"
            text
            type="danger"
            size="small"
            @click.stop="handleLeave(space)"
          >
            退出
          </el-button>
        </div>
      </div>
    </div>

    <!-- 创建空间对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建团队空间"
      width="500px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="80px"
      >
        <el-form-item label="空间名称" prop="name">
          <el-input
            v-model="createForm.name"
            placeholder="请输入空间名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="空间描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入空间描述（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import {
  Plus,
  FolderOpened,
  User,
  Calendar,
  Setting
} from '@element-plus/icons-vue';
import { useSpaceStore } from '@/stores/space';
import { useAuthStore } from '@/stores/auth';
import type { Space } from '@/types';

const router = useRouter();
const spaceStore = useSpaceStore();
const authStore = useAuthStore();

const showCreateDialog = ref(false);
const creating = ref(false);
const createFormRef = ref<FormInstance>();
const createForm = ref({
  name: '',
  description: ''
});

const createRules: FormRules = {
  name: [
    { required: true, message: '请输入空间名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
};

const getRoleType = (role: string) => {
  const typeMap: Record<string, any> = {
    owner: 'danger',
    admin: 'warning',
    editor: 'primary',
    viewer: 'info'
  };
  return typeMap[role] || 'info';
};

const getRoleLabel = (role: string) => {
  const labelMap: Record<string, string> = {
    owner: '所有者',
    admin: '管理员',
    editor: '编辑者',
    viewer: '查看者',
    member: '成员'
  };
  return labelMap[role] || '成员';
};

const canManage = (role: string) => {
  return role === 'owner' || role === 'admin';
};

const formatDate = (date: string) => {
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const handleCreate = async () => {
  if (!createFormRef.value) return;

  try {
    await createFormRef.value.validate();
    creating.value = true;

    await spaceStore.createNewSpace({
      name: createForm.value.name,
      description: createForm.value.description
    });

    ElMessage.success('创建成功');
    showCreateDialog.value = false;
    createForm.value = { name: '', description: '' };
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '创建失败');
    }
  } finally {
    creating.value = false;
  }
};

const handleEnterSpace = (id: number) => {
  router.push(`/spaces/${id}`);
};

const handleSettings = (id: number) => {
  router.push(`/spaces/${id}`);
};

const handleLeave = async (space: Space) => {
  try {
    await ElMessageBox.confirm(
      `确定要退出空间"${space.name}"吗？退出后将无法访问该空间的笔记。`,
      '退出空间',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    await spaceStore.leaveSpaceById(space.id);
    ElMessage.success('已退出空间');
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退出失败');
    }
  }
};

onMounted(async () => {
  await spaceStore.fetchSpaces();
});
</script>

<style scoped lang="scss">
.space-list-view {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;

    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .loading-container {
    padding: 24px;
  }

  .empty-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
  }

  .space-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }

  .space-card {
    background-color: var(--el-bg-color);
    border: 1px solid var(--el-border-color);
    border-radius: 12px;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: var(--el-color-primary);
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }

    .space-card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;

      .space-icon {
        width: 56px;
        height: 56px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: var(--el-color-primary-light-9);
        border-radius: 12px;
      }
    }

    .space-card-body {
      margin-bottom: 16px;

      .space-name {
        margin: 0 0 8px;
        font-size: 18px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .space-description {
        margin: 0 0 12px;
        font-size: 14px;
        color: var(--el-text-color-secondary);
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }

      .space-meta {
        display: flex;
        gap: 16px;
        color: var(--el-text-color-secondary);
        font-size: 13px;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .space-card-footer {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-top: 16px;
      border-top: 1px solid var(--el-border-color-light);
    }
  }
}
</style>
