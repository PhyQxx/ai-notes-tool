<template>
  <div class="space-detail-view">
    <div class="page-header">
      <div class="header-left">
        <el-button text @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>

      <div class="header-center">
        <h2 class="space-title">{{ spaceStore.currentSpace?.name }}</h2>
        <p class="space-description">{{ spaceStore.currentSpace?.description || '暂无描述' }}</p>
      </div>

      <div class="header-right">
        <el-button-group>
          <el-button type="primary" @click="showInviteDialog = true">
            <el-icon><Plus /></el-icon>
            邀请成员
          </el-button>
          <el-button v-if="canManage" @click="showSettingsDialog = true">
            <el-icon><Setting /></el-icon>
            设置
          </el-button>
          <el-button
            v-if="isMember"
            type="danger"
            @click="handleLeave"
          >
            <el-icon><SwitchButton /></el-icon>
            退出空间
          </el-button>
        </el-button-group>
      </div>
    </div>

    <div class="content-container">
      <el-card class="members-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>空间成员</span>
            <el-tag type="info">{{ spaceStore.members.length }} 人</el-tag>
          </div>
        </template>

        <el-skeleton v-if="spaceStore.loading" :rows="3" animated />

        <el-empty v-else-if="spaceStore.members.length === 0" description="暂无成员" />

        <div v-else class="members-list">
          <div
            v-for="member in spaceStore.members"
            :key="member.id"
            class="member-item"
          >
            <div class="member-info">
              <el-avatar :size="40" :src="member.avatar || defaultAvatar">
                {{ member.nickname?.charAt(0) || member.username?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="member-details">
                <div class="member-name">{{ member.nickname || member.username }}</div>
                <div class="member-email">{{ member.username }}</div>
              </div>
            </div>

            <div class="member-actions">
              <el-tag :type="getRoleType(member.role)" size="small">
                {{ getRoleLabel(member.role) }}
              </el-tag>

              <el-dropdown v-if="canManage && member.userId !== currentUserId" trigger="click">
                <el-button text>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleShowRoleDialog(member)">
                      <el-icon><Edit /></el-icon>
                      修改角色
                    </el-dropdown-item>
                    <el-dropdown-item
                      divided
                      @click="handleRemoveMember(member)"
                    >
                      <el-icon><Delete /></el-icon>
                      移除成员
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </el-card>

      <el-card class="notes-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>空间笔记</span>
            <el-button text @click="handleNewNote">
              <el-icon><Plus /></el-icon>
              新建
            </el-button>
          </div>
        </template>

        <el-empty description="暂无笔记" />
      </el-card>
    </div>

    <!-- 邀请成员对话框 -->
    <el-dialog
      v-model="showInviteDialog"
      title="邀请成员"
      width="500px"
    >
      <el-form
        ref="inviteFormRef"
        :model="inviteForm"
        :rules="inviteRules"
        label-width="80px"
      >
        <el-form-item label="邮箱地址" prop="email">
          <el-input
            v-model="inviteForm.email"
            placeholder="请输入要邀请的邮箱"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="inviteForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="编辑者" value="editor" />
            <el-option label="查看者" value="viewer" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showInviteDialog = false">取消</el-button>
        <el-button type="primary" :loading="inviting" @click="handleInvite">
          邀请
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改角色对话框 -->
    <el-dialog
      v-model="showRoleDialog"
      title="修改角色"
      width="400px"
    >
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="成员">
          <span>{{ selectedMember?.nickname || selectedMember?.username }}</span>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="roleForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="编辑者" value="editor" />
            <el-option label="查看者" value="viewer" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRoleDialog = false">取消</el-button>
        <el-button type="primary" :loading="updatingRole" @click="handleUpdateRole">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import {
  ArrowLeft,
  Plus,
  Setting,
  SwitchButton,
  MoreFilled,
  Edit,
  Delete
} from '@element-plus/icons-vue';
import { useSpaceStore } from '@/stores/space';
import { useAuthStore } from '@/stores/auth';
import { updateMemberRole } from '@/api/space';
import type { SpaceMember } from '@/types';

const route = useRoute();
const router = useRouter();
const spaceStore = useSpaceStore();
const authStore = useAuthStore();

const spaceId = computed(() => parseInt(route.params.id as string));
const currentUserId = authStore.user?.id;
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const showInviteDialog = ref(false);
const showRoleDialog = ref(false);
const showSettingsDialog = ref(false);
const inviting = ref(false);
const updatingRole = ref(false);
const selectedMember = ref<SpaceMember | null>(null);

const inviteFormRef = ref<FormInstance>();
const inviteForm = ref({
  email: '',
  role: 'editor'
});

const roleForm = ref({
  role: ''
});

const inviteRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
};

const canManage = computed(() => {
  const role = spaceStore.currentSpace?.myRole;
  return role === 'owner' || role === 'admin';
});

const isMember = computed(() => {
  const role = spaceStore.currentSpace?.myRole;
  return role !== 'owner';
});

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

const handleBack = () => {
  router.push('/spaces');
};

const handleInvite = async () => {
  if (!inviteFormRef.value) return;

  try {
    await inviteFormRef.value.validate();
    inviting.value = true;

    await spaceStore.inviteNewMember(spaceId.value, inviteForm.value);

    ElMessage.success('邀请成功');
    showInviteDialog.value = false;
    inviteForm.value = { email: '', role: 'editor' };
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '邀请失败');
    }
  } finally {
    inviting.value = false;
  }
};

const handleShowRoleDialog = (member: SpaceMember) => {
  selectedMember.value = member;
  roleForm.value.role = member.role;
  showRoleDialog.value = true;
};

const handleUpdateRole = async () => {
  if (!selectedMember.value) return;

  try {
    updatingRole.value = true;
    await updateMemberRole(spaceId.value, selectedMember.value.userId, roleForm.value.role);

    // 更新本地数据
    const member = spaceStore.members.find(m => m.id === selectedMember.value?.id);
    if (member) {
      member.role = roleForm.value.role;
    }

    ElMessage.success('角色修改成功');
    showRoleDialog.value = false;
  } catch (error: any) {
    ElMessage.error(error.message || '修改角色失败');
  } finally {
    updatingRole.value = false;
  }
};

const handleRemoveMember = async (member: SpaceMember) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除成员"${member.nickname || member.username}"吗？`,
      '移除成员',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    await spaceStore.removeMemberById(spaceId.value, member.userId);
    ElMessage.success('已移除成员');
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '移除成员失败');
    }
  }
};

const handleLeave = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要退出空间"${spaceStore.currentSpace?.name}"吗？`,
      '退出空间',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );

    await spaceStore.leaveSpaceById(spaceId.value);
    ElMessage.success('已退出空间');
    router.push('/spaces');
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退出失败');
    }
  }
};

const handleNewNote = () => {
  router.push(`/notes/new?spaceId=${spaceId.value}`);
};

onMounted(async () => {
  try {
    // 加载空间详情
    const space = await spaceStore.fetchSpaces().then(() => {
      return spaceStore.spaces.find(s => s.id === spaceId.value);
    });

    if (space) {
      spaceStore.setCurrentSpace(space);
    }

    // 加载成员列表
    await spaceStore.fetchMembers(spaceId.value);
  } catch (error) {
    console.error('加载空间详情失败:', error);
    router.push('/spaces');
  }
});
</script>

<style scoped lang="scss">
.space-detail-view {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24px 0;
    border-bottom: 1px solid var(--el-border-color);
    margin-bottom: 24px;

    .header-left {
      flex-shrink: 0;
    }

    .header-center {
      flex: 1;
      text-align: center;

      .space-title {
        margin: 0 0 8px;
        font-size: 24px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .space-description {
        margin: 0;
        font-size: 14px;
        color: var(--el-text-color-secondary);
      }
    }

    .header-right {
      flex-shrink: 0;
    }
  }

  .content-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
  }

  .members-card,
  .notes-card {
    :deep(.el-card__header) {
      padding: 16px 20px;
    }

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      span {
        font-weight: 600;
        font-size: 16px;
      }
    }
  }

  .members-list {
    .member-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid var(--el-border-color-light);

      &:last-child {
        border-bottom: none;
      }

      .member-info {
        display: flex;
        align-items: center;
        gap: 12px;

        .member-details {
          .member-name {
            font-weight: 500;
            color: var(--el-text-color-primary);
            margin-bottom: 4px;
          }

          .member-email {
            font-size: 12px;
            color: var(--el-text-color-secondary);
          }
        }
      }

      .member-actions {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }
  }
}
</style>
