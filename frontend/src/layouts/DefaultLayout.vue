<template>
  <el-container class="default-layout">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapse">AI Notes</span>
        <span v-else>AI</span>
      </div>

      <el-button type="primary" class="new-note-btn" @click="handleNewNote">
        <el-icon><Plus /></el-icon>
        <span v-if="!isCollapse">新建笔记</span>
      </el-button>

      <div class="folder-section">
        <div class="section-title">文件夹</div>
        <FolderTree :is-collapse="isCollapse" />
      </div>

      <div class="sidebar-footer">
        <el-button text @click="toggleCollapse">
          <el-icon>
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </el-button>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索笔记..."
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" :src="user?.avatar || defaultAvatar">
                {{ user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ user?.nickname || '未登录' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/settings')">
                  <el-icon><Setting /></el-icon>
                  <span>设置</span>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { useNoteStore } from '@/stores/note';
import FolderTree from '@/components/common/FolderTree.vue';

const router = useRouter();
const authStore = useAuthStore();
const noteStore = useNoteStore();

const isCollapse = ref(false);
const searchKeyword = ref('');
const user = authStore.user;
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

// 切换侧边栏
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
};

// 新建笔记
const handleNewNote = () => {
  router.push('/notes/new');
};

// 搜索笔记
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    noteStore.search(searchKeyword.value.trim());
    router.push('/notes');
  }
};

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    authStore.logout();
    router.push('/login');
  } catch (error) {
    // 用户取消
  }
};

onMounted(async () => {
  // 初始化用户信息
  await authStore.initAuth();
  // 加载文件夹树
  await noteStore.fetchFolders();
});
</script>

<style scoped lang="scss">
.default-layout {
  height: 100vh;

  .sidebar {
    background-color: var(--el-bg-color);
    border-right: 1px solid var(--el-border-color);
    display: flex;
    flex-direction: column;
    transition: width 0.3s;

    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      font-weight: bold;
      color: var(--el-color-primary);
      border-bottom: 1px solid var(--el-border-color);
    }

    .new-note-btn {
      margin: 16px;
      width: calc(100% - 32px);
      justify-content: center;
    }

    .folder-section {
      flex: 1;
      overflow-y: auto;
      padding: 0 16px;

      .section-title {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-bottom: 8px;
      }
    }

    .sidebar-footer {
      height: 50px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-top: 1px solid var(--el-border-color);
    }
  }

  .main-container {
    .header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 24px;
      border-bottom: 1px solid var(--el-border-color);
      background-color: var(--el-bg-color);

      .header-left {
        width: 300px;

        .el-input {
          width: 100%;
        }
      }

      .header-right {
        .user-info {
          display: flex;
          align-items: center;
          gap: 12px;
          cursor: pointer;

          .username {
            font-size: 14px;
            color: var(--el-text-color-primary);
          }
        }
      }
    }

    .main-content {
      background-color: var(--el-bg-color-page);
      padding: 24px;
      overflow-y: auto;
    }
  }
}
</style>
