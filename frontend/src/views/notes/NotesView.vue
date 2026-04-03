<template>
  <div class="notes-view" ref="scrollContainer" @scroll="handleScroll">
    <div class="toolbar">
      <div class="toolbar-left">
        <h3 v-if="mode === 'recent'" class="mode-title">最近编辑</h3>
        <h3 v-else-if="mode === 'favorites'" class="mode-title">收藏笔记</h3>

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

        <el-select v-model="searchScope" placeholder="搜索范围" style="width: 120px">
          <el-option label="全部" value="all" />
          <el-option label="标题" value="title" />
          <el-option label="内容" value="content" />
        </el-select>

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

        <el-select v-model="sortBy" placeholder="排序" style="width: 120px" @change="handleSort">
          <el-option label="更新时间" value="updatedAt" />
          <el-option label="创建时间" value="createdAt" />
          <el-option label="标题" value="title" />
        </el-select>
      </div>

      <div class="toolbar-right">
        <el-button type="primary" @click="handleNewNote">
          <el-icon><Plus /></el-icon>
          新建笔记
        </el-button>
      </div>
    </div>

    <div v-if="initialLoading" class="loading-container">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <el-empty v-else-if="displayNotes.length === 0" description="暂无笔记" />

    <div v-else class="notes-list">
      <el-card
        v-for="note in displayNotes"
        :key="note.id"
        class="note-card"
        shadow="hover"
        @click="handleOpenNote(note.id)"
      >
        <NoteCard :note="note" @toggle-favorite="handleToggleFavorite(note.id)" />
      </el-card>
    </div>

    <div v-if="loadingMore" class="load-more">
      <el-icon class="is-loading" :size="20" /><span>加载中...</span>
    </div>
    <div v-else-if="!hasMore && displayNotes.length > 0" class="load-more no-more">
      没有更多了
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useNoteStore } from '@/stores/note';
import NoteCard from '@/components/common/NoteCard.vue';
import { fullTextSearch } from '@/api/note';
import type { Folder } from '@/types';

const props = defineProps<{
  mode?: string;
}>();

const router = useRouter();
const noteStore = useNoteStore();

const searchKeyword = ref('');
const searchScope = ref('all');
const searchResults = ref<any[]>([]);
const sortBy = ref('updatedAt');
const folderFilter = ref(0);
const scrollContainer = ref<HTMLElement | null>(null);
const initialLoading = ref(false);
const loadingMore = ref(false);
const hasMore = ref(true);

const notes = computed(() => noteStore.notes);
const total = computed(() => noteStore.total);
const folders = computed(() => noteStore.folders);

const isSearchMode = computed(() => searchKeyword.value.trim().length > 0);

const displayNotes = computed(() => {
  if (isSearchMode.value && searchResults.value.length > 0) {
    return searchResults.value.map(r => ({
      ...r,
      content: r.contentPreview || '',
      tags: r.tags ? r.tags.split(',').filter(Boolean) : []
    }));
  }
  return notes.value;
});

const flatFolders = computed<Folder[]>(() => {
  const result: Folder[] = [];
  const flatten = (folder: Folder, level = 0) => {
    result.push({ ...folder, name: '\u3000'.repeat(level) + folder.name });
    if (folder.children) {
      folder.children.forEach(child => flatten(child, level + 1));
    }
  };
  folders.value.forEach(folder => flatten(folder));
  return result;
});

const getQueryParams = () => {
  const params: any = {};
  if (props.mode === 'recent') {
    params.sortBy = 'updatedAt';
  } else if (props.mode === 'favorites') {
    params.isFavorite = 1;
  } else if (folderFilter.value !== 0) {
    params.folderId = folderFilter.value;
  }
  if (sortBy.value) params.sortBy = sortBy.value;
  return params;
};

const fetchInitialNotes = async () => {
  initialLoading.value = true;
  hasMore.value = true;
  noteStore.currentPage = 1;
  try {
    await noteStore.fetchNotes({ page: 1, size: noteStore.pageSize, ...getQueryParams() });
    hasMore.value = notes.value.length < total.value;
  } catch (e) {
    console.error(e);
  } finally {
    initialLoading.value = false;
  }
};

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value || isSearchMode.value) return;
  loadingMore.value = true;
  const nextPage = noteStore.currentPage + 1;
  try {
    await noteStore.fetchNotes({ page: nextPage, size: noteStore.pageSize, append: true, ...getQueryParams() });
    hasMore.value = notes.value.length < total.value;
  } catch (e) {
    console.error(e);
  } finally {
    loadingMore.value = false;
  }
};

const handleScroll = () => {
  const el = scrollContainer.value;
  if (!el) return;
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 50) {
    loadMore();
  }
};

const handleSearch = async () => {
  const keyword = searchKeyword.value.trim();
  if (keyword) {
    try {
      const result = await fullTextSearch(keyword, searchScope.value);
      searchResults.value = result.records || [];
    } catch (e) {
      console.error('全文搜索失败:', e);
      searchResults.value = [];
    }
  } else {
    searchResults.value = [];
    fetchInitialNotes();
  }
};

const handleClearSearch = () => {
  searchKeyword.value = '';
  searchResults.value = [];
  fetchInitialNotes();
};

const handleFilter = () => {
  fetchInitialNotes();
};

const handleSort = () => {
  fetchInitialNotes();
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

watch(() => props.mode, () => {
  searchResults.value = [];
  searchKeyword.value = '';
  folderFilter.value = 0;
  fetchInitialNotes();
});

onMounted(async () => {
  await noteStore.fetchFolders();
  fetchInitialNotes();
});
</script>

<style scoped lang="scss">
.notes-view {
  height: 100%;
  overflow-y: auto;

  .toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 12px;

    .toolbar-left {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-wrap: wrap;

      .mode-title {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        margin-right: 8px;
      }
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

  .load-more {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 24px 0;
    color: var(--el-text-color-secondary);
    font-size: 14px;

    &.no-more {
      color: var(--el-text-color-placeholder);
    }
  }
}
</style>
