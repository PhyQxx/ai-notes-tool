<template>
  <div class="notes-view">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索笔记..."
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
          @clear="handleClearSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-button @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>

        <el-select v-model="folderFilter" placeholder="文件夹" style="width: 150px" clearable @change="handleFilter">
          <el-option label="全部" :value="0" />
          <el-option
            v-for="folder in flatFolders"
            :key="folder.id"
            :label="folder.name"
            :value="folder.id"
          />
        </el-select>

        <el-select v-model="timeFilter" placeholder="时间" style="width: 120px" clearable @change="handleFilter">
          <el-option label="全部" value="" />
          <el-option label="今天" value="today" />
          <el-option label="本周" value="week" />
          <el-option label="本月" value="month" />
        </el-select>

        <el-select v-model="sortBy" placeholder="排序" style="width: 120px" @change="handleSort">
          <el-option label="更新时间" value="updatedAt" />
          <el-option label="创建时间" value="createdAt" />
          <el-option label="标题" value="title" />
        </el-select>

        <el-button v-if="recentSearches.length > 0" text @click="showRecentSearches = true">
          <el-icon><Clock /></el-icon>
          最近搜索
        </el-button>
      </div>

      <div class="toolbar-right">
        <el-button type="primary" @click="handleNewNote">
          <el-icon><Plus /></el-icon>
          新建笔记
        </el-button>
      </div>
    </div>

    <!-- 最近搜索建议 -->
    <el-drawer
      v-model="showRecentSearches"
      title="最近搜索"
      size="300px"
      direction="ltr"
    >
      <div v-if="recentSearches.length === 0" class="empty-search">
        <el-empty description="暂无搜索历史" />
      </div>
      <div v-else class="recent-searches">
        <el-tag
          v-for="(keyword, index) in recentSearches"
          :key="index"
          closable
          style="margin: 4px"
          @click="handleQuickSearch(keyword)"
          @close="handleRemoveRecentSearch(index)"
        >
          <el-icon><Clock /></el-icon>
          {{ keyword }}
        </el-tag>
      </div>
    </el-drawer>

    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <el-empty v-else-if="notes.length === 0" description="暂无笔记" />

    <div v-else class="notes-list">
      <el-card
        v-for="note in notes"
        :key="note.id"
        class="note-card"
        shadow="hover"
        @click="handleOpenNote(note.id)"
      >
        <NoteCard :note="note" @toggle-favorite="handleToggleFavorite(note.id)" />
      </el-card>
    </div>

    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useNoteStore } from '@/stores/note';
import NoteCard from '@/components/common/NoteCard.vue';
import type { Folder } from '@/types';

const router = useRouter();
const noteStore = useNoteStore();

const searchKeyword = ref('');
const sortBy = ref('updatedAt');
const folderFilter = ref(0);
const timeFilter = ref('');
const showRecentSearches = ref(false);

// 从localStorage加载最近搜索
const recentSearches = ref<string[]>(JSON.parse(localStorage.getItem('recent-searches') || '[]'));

const notes = computed(() => noteStore.notes);
const loading = computed(() => noteStore.loading);
const total = computed(() => noteStore.total);
const folders = computed(() => noteStore.folders);
const currentPage = computed({
  get: () => noteStore.currentPage,
  set: (value) => {
    noteStore.currentPage = value;
  }
});
const pageSize = computed({
  get: () => noteStore.pageSize,
  set: (value) => {
    noteStore.pageSize = value;
  }
});

// 扁平化文件夹列表
const flatFolders = computed<Folder[]>(() => {
  const result: Folder[] = [];
  const flatten = (folder: Folder, level = 0) => {
    result.push({
      ...folder,
      name: '　'.repeat(level) + folder.name
    });
    if (folder.children) {
      folder.children.forEach(child => flatten(child, level + 1));
    }
  };
  folders.value.forEach(folder => flatten(folder));
  return result;
});

// 高亮搜索关键词
const highlightKeyword = (text: string, keyword: string) => {
  if (!keyword.trim()) return text;
  const regex = new RegExp(`(${keyword})`, 'gi');
  return text.replace(regex, '<span style="background-color: var(--el-color-warning-light-7); padding: 0 2px; border-radius: 2px;">$1</span>');
};

const handleSearch = () => {
  const keyword = searchKeyword.value.trim();
  if (keyword) {
    // 保存到最近搜索
    saveRecentSearch(keyword);
    noteStore.search(keyword);
  } else {
    handleFilter();
  }
};

const handleClearSearch = () => {
  searchKeyword.value = '';
  handleFilter();
};

const handleFilter = () => {
  let filteredNotes = [...notes.value];

  // 文件夹筛选
  if (folderFilter.value !== 0) {
    filteredNotes = filteredNotes.filter(note => note.folderId === folderFilter.value);
  }

  // 时间筛选
  if (timeFilter.value) {
    const now = new Date();
    filteredNotes = filteredNotes.filter(note => {
      const noteDate = new Date(note.updatedAt);
      switch (timeFilter.value) {
        case 'today':
          return noteDate.toDateString() === now.toDateString();
        case 'week':
          const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
          return noteDate >= weekAgo;
        case 'month':
          const monthAgo = new Date(now.getFullYear(), now.getMonth(), 1);
          return noteDate >= monthAgo;
        default:
          return true;
      }
    });
  }

  // 关键词高亮
  if (searchKeyword.value.trim()) {
    filteredNotes = filteredNotes.map(note => ({
      ...note,
      title: highlightKeyword(note.title, searchKeyword.value)
    }));
  }

  // 排序
  handleSort();

  noteStore.notes = filteredNotes;
};

const handleSort = () => {
  notes.value.sort((a: any, b: any) => {
    if (sortBy.value === 'title') {
      return a.title.replace(/<[^>]*>/g, '').localeCompare(b.title.replace(/<[^>]*>/g, ''));
    }
    return new Date(b[sortBy.value]).getTime() - new Date(a[sortBy.value]).getTime();
  });
};

const handleQuickSearch = (keyword: string) => {
  searchKeyword.value = keyword;
  showRecentSearches.value = false;
  handleSearch();
};

const saveRecentSearch = (keyword: string) => {
  const index = recentSearches.value.indexOf(keyword);
  if (index !== -1) {
    recentSearches.value.splice(index, 1);
  }
  recentSearches.value.unshift(keyword);
  // 最多保存10条
  if (recentSearches.value.length > 10) {
    recentSearches.value.pop();
  }
  localStorage.setItem('recent-searches', JSON.stringify(recentSearches.value));
};

const handleRemoveRecentSearch = (index: number) => {
  recentSearches.value.splice(index, 1);
  localStorage.setItem('recent-searches', JSON.stringify(recentSearches.value));
};

const handleNewNote = () => {
  router.push('/notes/new');
};

const handleOpenNote = (id: number) => {
  router.push(`/notes/${id}`);
};

const handleToggleFavorite = async (id: number) => {
  try {
    await noteStore.toggleFavorite(id);
  } catch (error) {
    console.error('切换收藏失败:', error);
  }
};

const handleSizeChange = (size: number) => {
  noteStore.fetchNotes({ size });
};

const handleCurrentChange = (page: number) => {
  noteStore.fetchNotes({ page });
};

onMounted(async () => {
  await noteStore.fetchFolders();
  noteStore.fetchNotes();
});
</script>

<style scoped lang="scss">
.notes-view {
  .toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;

    .toolbar-left {
      display: flex;
      gap: 12px;
    }
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60px 0;
    color: var(--el-text-color-secondary);
  }

  .notes-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;

    .note-card {
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: translateY(-4px);
      }
    }
  }

  .pagination-container {
    margin-top: 32px;
    display: flex;
    justify-content: center;
  }

  .empty-search {
    padding: 40px 0;
  }

  .recent-searches {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .el-tag {
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        transform: translateY(-2px);
      }
    }
  }
}
</style>
