<template>
  <el-container class="default-layout">
    <!-- Mobile sidebar overlay -->
    <div v-if="sidebarVisible" class="sidebar-overlay" @click="sidebarVisible = false" />
    <el-aside :width="isCollapse ? '64px' : '240px'" :class="['sidebar', { 'sidebar-visible': sidebarVisible }]">
      <div class="logo" @click="router.push('/')" style="cursor:pointer;">
        <span class="logo-icon">📝</span>
        <span v-if="!isCollapse" class="logo-text">AI Notes</span>
      </div>

      <el-button type="primary" class="new-note-btn" @click="handleNewNote">
        <el-icon><Plus /></el-icon>
        <span v-if="!isCollapse">{{ t('nav.newNote') }}</span>
      </el-button>

      <div class="folder-section">
        <div class="section-title">
          <span>{{ t('nav.folders') }}</span>
          <el-button v-if="!isCollapse" text circle size="small" class="add-folder-btn" @click="handleCreateRootFolder">
            <el-icon><Plus /></el-icon>
          </el-button>
        </div>
        <FolderTree ref="folderTreeRef" :is-collapse="isCollapse" />
      </div>

      <div class="menu-section">
        <div class="menu-group">
          <div class="menu-group-label">导航</div>
          <div
            class="menu-item"
            :class="{ active: isActiveMenu('/notes/recent') }"
            @click="router.push('/notes/recent')"
          >
            <el-icon><Clock /></el-icon>
            <span v-if="!isCollapse">{{ t('nav.recentEdits') }}</span>
          </div>
          <div
            class="menu-item"
            :class="{ active: isActiveMenu('/notes/favorites') }"
            @click="router.push('/notes/favorites')"
          >
            <el-icon><Star /></el-icon>
            <span v-if="!isCollapse">{{ t('nav.favorites') }}</span>
          </div>
          <div
            class="menu-item"
            :class="{ active: isActiveMenu('/spaces') || isActiveMenu('/spaces/') }"
            @click="router.push('/spaces')"
          >
            <el-icon><FolderOpened /></el-icon>
            <span v-if="!isCollapse">{{ t('nav.teamSpaces') }}</span>
          </div>
          <div
            class="sidebar-ai"
            :class="{ active: isActiveMenu('/ai/chat') }"
            @click="router.push('/ai/chat')"
          >
            <div class="sidebar-item">
              <el-icon><ChatDotRound /></el-icon>
              <span v-if="!isCollapse">{{ t('nav.aiAssistant') }}</span>
            </div>
          </div>
          <div
            class="menu-item"
            :class="{ active: isActiveMenu('/graph') }"
            @click="router.push('/graph')"
          >
            <el-icon><Share /></el-icon>
            <span v-if="!isCollapse">{{ t('nav.knowledgeGraph') }}</span>
          </div>
          <div
            class="menu-item"
            :class="{ active: isActiveMenu('/canvas') || isActiveMenu('/canvas/') }"
            @click="router.push('/canvas')"
          >
            <el-icon><Monitor /></el-icon>
            <span v-if="!isCollapse">知识画布</span>
          </div>
        </div>
        <div class="menu-group">
          <div class="menu-group-label">管理</div>
          <div
            class="menu-item"
            :class="{ active: isActiveMenu('/trash') }"
            @click="router.push('/trash')"
          >
            <el-icon><Delete /></el-icon>
            <span v-if="!isCollapse">{{ t('nav.trash') }}</span>
          </div>
          <div
            class="menu-item"
            :class="{ active: isActiveMenu('/audit-logs') }"
            @click="router.push('/audit-logs')"
          >
            <el-icon><Memo /></el-icon>
            <span v-if="!isCollapse">操作日志</span>
          </div>
        </div>
      </div>

      <div class="sidebar-footer">
        <LanguageSwitch />
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
          <!-- Mobile hamburger -->
          <el-button v-if="showMobileMenu" text class="hamburger-btn" @click="sidebarVisible = !sidebarVisible">
            <el-icon :size="20"><Expand /></el-icon>
          </el-button>
          <el-popover
            placement="bottom-start"
            :width="400"
            trigger="focus"
            :visible="searchHistoryVisible && searchHistory.length > 0"
          >
            <template #reference>
              <el-input
                v-model="searchKeyword"
                placeholder="搜索笔记..."
                clearable
                class="search-input"
                @keyup.enter="handleSearch"
                @focus="searchHistoryVisible = true"
                @blur="handleSearchBlur"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </template>
            <div class="search-history">
              <div class="history-header">
                <span>最近搜索</span>
                <el-button text size="small" @click="clearSearchHistory">清空</el-button>
              </div>
              <div class="history-list">
                <div
                  v-for="item in searchHistory"
                  :key="item"
                  class="history-item"
                  @mousedown="handleHistoryClick(item)"
                >
                  <el-icon><Clock /></el-icon>
                  <span class="history-text">{{ item }}</span>
                  <el-icon class="delete-icon" @click.stop="removeHistoryItem(item)"><Close /></el-icon>
                </div>
              </div>
            </div>
          </el-popover>
        </div>

        <div class="header-right">
          <el-tooltip :content="themeStore.mode === 'light' ? '浅色模式' : themeStore.mode === 'dark' ? '深色模式' : '跟随系统'" placement="bottom">
            <el-button text circle @click="cycleThemeMode">
              <el-icon :size="18">
                <Moon v-if="themeStore.getEffectiveTheme() === 'dark'" />
                <Sunny v-else />
              </el-icon>
            </el-button>
          </el-tooltip>
          <div class="header-divider" />
          <NotificationBell />
          <div class="header-divider" />
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

    <!-- Ctrl+K Command Palette -->
    <el-dialog v-model="showCommandPalette" width="560px" :show-close="false" class="command-palette" top="15vh" @opened="focusCommandInput">
      <el-input ref="commandInputRef" v-model="commandKeyword" placeholder="搜索笔记，输入关键词后回车跳转..." size="large" clearable @keyup.enter="handleCommandSearch">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <div v-if="commandResults.length > 0" class="command-results">
        <div v-for="item in commandResults" :key="item.id" class="command-item" @click="handleCommandSelect(item)">
          <div class="command-item-title" v-html="item.titleHighlight || item.title || '无标题'"></div>
          <div class="command-item-preview" v-html="item.contentPreview || (item.content || '').substring(0, 60)"></div>
        </div>
      </div>

      <div v-else-if="commandSearched" class="command-empty">未找到匹配的笔记</div>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ElMessageBox } from 'element-plus';
import { Fold, Expand, Search, Plus, Setting, SwitchButton, ChatDotRound, FolderOpened, Clock, Star, Moon, Sunny, Delete, Share, Memo } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';
import { useNoteStore } from '@/stores/note';
import { useNotificationStore } from '@/stores/notification';
import { useThemeStore } from '@/stores/theme';
import LanguageSwitch from '@/components/LanguageSwitch.vue';
import { getToken } from '@/utils/storage';
import wsClient from '@/utils/websocket';
import { fullTextSearch } from '@/api/note';
import FolderTree from '@/components/common/FolderTree.vue';
import NotificationBell from '@/components/common/NotificationBell.vue';

const router = useRouter();
const { t } = useI18n();
const authStore = useAuthStore();
const noteStore = useNoteStore();
const themeStore = useThemeStore();

const isCollapse = ref(false);
const sidebarVisible = ref(false);
const showMobileMenu = ref(false);

const checkMobile = () => {
  showMobileMenu.value = window.innerWidth <= 768;
};

const searchKeyword = ref('');
const searchHistory = ref<string[]>([]);
const searchHistoryVisible = ref(false);
const user = authStore.user;
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const folderTreeRef = ref<any>(null);

const handleCreateRootFolder = () => {
  folderTreeRef.value?.handleCreateRoot();
};

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value;
};

const cycleThemeMode = () => {
  const modes: Array<'light' | 'dark' | 'system'> = ['light', 'dark', 'system'];
  const current = modes.indexOf(themeStore.mode);
  themeStore.setMode(modes[(current + 1) % 3]);
};

const handleNewNote = () => {
  router.push('/notes/new');
};

const handleSearch = () => {
  const kw = searchKeyword.value.trim();
  if (kw) {
    saveSearchHistory(kw);
    searchHistoryVisible.value = false;
    noteStore.search(kw);
    router.push('/notes');
  }
};

const handleSearchBlur = () => {
  setTimeout(() => {
    searchHistoryVisible.value = false;
  }, 200);
};

const saveSearchHistory = (kw: string) => {
  const history = [...searchHistory.value];
  const index = history.indexOf(kw);
  if (index > -1) {
    history.splice(index, 1);
  }
  history.unshift(kw);
  searchHistory.value = history.slice(0, 10);
  localStorage.setItem('search_history', JSON.stringify(searchHistory.value));
};

const loadSearchHistory = () => {
  const history = localStorage.getItem('search_history');
  if (history) {
    try {
      searchHistory.value = JSON.parse(history);
    } catch (e) {
      searchHistory.value = [];
    }
  }
};

const clearSearchHistory = () => {
  searchHistory.value = [];
  localStorage.removeItem('search_history');
};

const removeHistoryItem = (item: string) => {
  searchHistory.value = searchHistory.value.filter(h => h !== item);
  localStorage.setItem('search_history', JSON.stringify(searchHistory.value));
};

const handleHistoryClick = (item: string) => {
  searchKeyword.value = item;
  handleSearch();
};

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

const isActiveMenu = (path: string) => {
  return router.currentRoute.value.path === path;
};

onMounted(async () => {
  checkMobile();
  window.addEventListener('resize', checkMobile);
  await authStore.initAuth();
  await noteStore.fetchFolders();
  loadSearchHistory();
  const token = getToken();
  if (token) {
    wsClient.connect(token);
  }
  const notificationStore = useNotificationStore();
  notificationStore.initWSListener();
  notificationStore.fetchUnreadCount();
  document.addEventListener('keydown', handleKeyDown);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', checkMobile);
  document.removeEventListener('keydown', handleKeyDown);
});

const showCommandPalette = ref(false);
const commandKeyword = ref('');
const commandResults = ref<any[]>([]);
const commandSearched = ref(false);
const commandInputRef = ref();

const handleKeyDown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault();
    showCommandPalette.value = true;
    commandKeyword.value = '';
    commandResults.value = [];
    commandSearched.value = false;
  }
};

const focusCommandInput = () => {
  nextTick(() => commandInputRef.value?.focus());
};

const handleCommandSearch = async () => {
  const kw = commandKeyword.value.trim();
  if (!kw) return;
  commandSearched.value = true;
  try {
    const result = await fullTextSearch(kw, 'all');
    commandResults.value = (result.records || []).slice(0, 10);
  } catch (e) {
    commandResults.value = [];
  }
};

const handleCommandSelect = (item: any) => {
  showCommandPalette.value = false;
  router.push(`/notes/${item.id}`);
};
</script>

<style scoped lang="scss">
.default-layout {
  height: 100vh;

  .sidebar-overlay {
    display: none;
  }

  .sidebar {
    background-color: var(--bg-sidebar);
    border-right: 1px solid var(--el-border-color);
    display: flex;
    flex-direction: column;
    transition: width var(--duration-normal) var(--ease-default);
    overflow: hidden;

    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      gap: var(--space-2);
      padding: 0 var(--space-4);
      border-bottom: 1px solid var(--el-border-color);
      flex-shrink: 0;

      .logo-icon {
        font-size: 24px;
        flex-shrink: 0;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--brand-primary-bg);
        border-radius: var(--radius-md);
      }

      .logo-text {
        font-size: var(--font-size-h4);
        font-weight: var(--font-weight-bold);
        color: var(--brand-primary);
        white-space: nowrap;
        overflow: hidden;
      }
    }

    .new-note-btn {
      margin: var(--space-4);
      width: calc(100% - var(--space-8));
      justify-content: center;
    }

    .folder-section {
      flex: 1;
      overflow-y: auto;
      padding: 0 var(--space-4);

      .section-title {
        font-size: var(--font-size-caption);
        color: var(--text-secondary);
        margin-bottom: var(--space-2);
        text-transform: uppercase;
        letter-spacing: 0.5px;
        font-weight: var(--font-weight-medium);
        display: flex;
        justify-content: space-between;
        align-items: center;

        .add-folder-btn {
          padding: 2px;
          height: auto;
          color: var(--text-secondary);

          &:hover {
            color: var(--brand-primary);
            background-color: var(--brand-primary-bg);
          }
        }
      }
    }

    .menu-section {
      padding: var(--space-2) var(--space-4);
      border-top: 1px solid var(--el-border-color);

      .menu-group {
        margin-bottom: var(--space-2);

        &:last-child {
          margin-bottom: 0;
        }
      }

      .menu-group-label {
        font-size: var(--font-size-caption);
        color: var(--text-secondary);
        padding: var(--space-1) var(--space-3);
        text-transform: uppercase;
        letter-spacing: 0.5px;
        font-weight: var(--font-weight-medium);
      }

      .sidebar-ai {
        // 与普通菜单项样式一致，移除特殊背景高亮
        display: flex;
        align-items: center;
        gap: var(--space-3);
        padding: var(--space-2) var(--space-3);
        border-radius: var(--radius-md);
        cursor: pointer;
        transition: all var(--duration-fast) var(--ease-default);
        font-size: var(--font-size-body);

        .sidebar-item {
          color: inherit;
          display: flex;
          align-items: center;
          gap: var(--space-3);
        }

        &:hover {
          background: var(--hover-bg);
          color: var(--text-primary);
        }

        &.active {
          background: var(--active-bg);
          color: var(--primary-color);
        }
      }

      .menu-item {
        display: flex;
        align-items: center;
        gap: var(--space-3);
        padding: var(--space-2) var(--space-3);
        border-radius: var(--radius-md);
        cursor: pointer;
        transition: all var(--duration-fast) var(--ease-default);
        color: var(--text-regular);
        font-size: var(--font-size-body);
        position: relative;

        &:hover {
          background-color: var(--bg-hover);
          color: var(--text-primary);
        }

        &.active {
          background-color: var(--brand-primary-bg);
          color: var(--brand-primary);
          font-weight: var(--font-weight-medium);

          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 50%;
            transform: translateY(-50%);
            width: 3px;
            height: 20px;
            background-color: var(--brand-primary);
            border-radius: var(--radius-full);
          }
        }
      }
    }

    .sidebar-footer {
      height: 50px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-top: 1px solid var(--el-border-color);
      flex-shrink: 0;
    }
  }

  .main-container {
    .header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 var(--space-6);
      border-bottom: 1px solid var(--el-border-color);
      background-color: var(--bg-header);
      height: 60px;

      .header-left {
        display: flex;
        align-items: center;
        gap: var(--space-2);
        flex: 1;

        .hamburger-btn {
          display: none;
        }

        .search-input {
          max-width: 400px;
          width: 100%;

          :deep(.el-input__wrapper) {
            border-radius: var(--radius-full) !important;
          }

          :deep(.el-input__inner) {
            &::placeholder {
              color: var(--text-placeholder) !important;
            }
          }
        }
      }

      .header-right {
        display: flex;
        align-items: center;
        gap: var(--space-1);

        .header-divider {
          width: 1px;
          height: 20px;
          background-color: var(--el-border-color);
          margin: 0 var(--space-1);
        }

        .user-info {
          display: flex;
          align-items: center;
          gap: var(--space-3);
          cursor: pointer;
          padding: var(--space-1) var(--space-2);
          border-radius: var(--radius-md);
          transition: background-color var(--duration-fast);

          &:hover {
            background-color: var(--bg-hover);
          }

          .username {
            font-size: var(--font-size-body);
            color: var(--text-primary);
          }
        }
      }
    }

    .main-content {
      background-color: var(--bg-page);
      padding: var(--space-6);
      overflow-y: auto;
    }
  }

  .command-results {
    margin-top: var(--space-3);
    max-height: 400px;
    overflow-y: auto;

    .command-item {
      padding: var(--space-3);
      border-radius: var(--radius-md);
      cursor: pointer;
      transition: background-color var(--duration-fast);

      &:hover {
        background-color: var(--bg-hover);
      }

      .command-item-title {
        font-size: var(--font-size-body);
        font-weight: var(--font-weight-medium);
        color: var(--text-primary);
        margin-bottom: var(--space-1);
      }

      .command-item-preview {
        font-size: var(--font-size-caption);
        color: var(--text-secondary);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      :deep(em) {
        color: var(--brand-primary);
        font-weight: bold;
        background: var(--brand-primary-light-5);
        padding: 0 2px;
        border-radius: 2px;
        font-style: normal;
      }
    }
  }

  .command-empty {
    text-align: center;
    padding: var(--space-6);
    color: var(--text-placeholder);
    font-size: var(--font-size-body);
  }

  .search-history {
    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 8px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      margin-bottom: 8px;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }

    .history-list {
      .history-item {
        display: flex;
        align-items: center;
        padding: 8px;
        border-radius: 4px;
        cursor: pointer;
        transition: background-color 0.2s;
        gap: 8px;

        &:hover {
          background-color: var(--el-fill-color-light);

          .delete-icon {
            opacity: 1;
          }
        }

        .history-text {
          flex: 1;
          font-size: 14px;
          color: var(--el-text-color-primary);
        }

        .delete-icon {
          opacity: 0;
          color: var(--el-text-color-placeholder);
          transition: opacity 0.2s;

          &:hover {
            color: var(--el-color-danger);
          }
        }
      }
    }
  }
}

// Responsive: lg - sidebar collapses
@media (max-width: 1024px) {
  .default-layout .sidebar {
    width: 64px !important;

    .logo .logo-text,
    .folder-section .section-title,
    .menu-group-label,
    .menu-item span {
      display: none;
    }

    .new-note-btn span {
      display: none;
    }
  }
}

// Responsive: md - sidebar hidden, hamburger
@media (max-width: 768px) {
  .default-layout {
    .sidebar-overlay {
      display: block;
      position: fixed;
      inset: 0;
      background: var(--bg-overlay);
      z-index: 1000;
    }

    .sidebar {
      position: fixed !important;
      z-index: 1001;
      height: 100vh;
      left: 0;
      top: 0;
      transform: translateX(-100%);
      transition: transform var(--duration-normal) var(--ease-default);

      &.sidebar-visible {
        transform: translateX(0);
      }
    }

    .main-container .header {
      .header-left .hamburger-btn {
        display: flex;
      }
    }
  }
}

// Responsive: sm - command palette fullscreen
@media (max-width: 640px) {
  .default-layout .command-palette {
    width: 100% !important;
    margin: 0;
    top: 0 !important;
  }
}
</style>
