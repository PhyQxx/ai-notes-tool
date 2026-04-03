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
        <el-button @click="templateDialogRef?.open()">
          <el-icon><Document /></el-icon>
          从模板创建
        </el-button>
        <TemplateSelectDialog ref="templateDialogRef" @apply="handleTemplateApply" />
      </div>
    </div>

    <!-- 标签筛选 -->
    <TagFilterPanel
      v-if="tagCloudData.length > 0"
      :tags="tagCloudData"
      :custom-colors="tagColors"
      v-model="selectedTags"
      style="margin-bottom: 16px;"
    />

    <!-- 批量操作栏 -->
    <div v-if="selectedNoteIds.length > 0" class="batch-bar">
      <span>已选 {{ selectedNoteIds.length }} 篇笔记</span>
      <el-button type="primary" size="small" @click="batchTagDialogVisible = true">
        <el-icon><PriceTag /></el-icon> 批量打标签
      </el-button>
      <el-button type="danger" size="small" @click="handleBatchDelete">移入回收站</el-button>
      <el-button size="small" @click="selectedNoteIds = []">取消选择</el-button>
    </div>

    <!-- 批量打标签对话框 -->
    <el-dialog v-model="batchTagDialogVisible" title="批量打标签" width="480px">
      <div style="margin-bottom: 12px;">
        <TagCloud :tags="tagCloudData" :custom-colors="tagColors" @select="toggleBatchTag" />
      </div>
      <div style="margin-bottom: 12px;">
        <span style="font-weight: 600; font-size: 14px;">选择标签：</span>
        <div style="margin-top: 8px; display: flex; flex-wrap: wrap; gap: 6px;">
          <el-tag
            v-for="t in batchSelectedTags"
            :key="t"
            closable
            @close="batchSelectedTags = batchSelectedTags.filter(x => x !== t)"
          >{{ t }}</el-tag>
          <el-input
            v-if="showNewTagInput"
            ref="newTagInputRef"
            v-model="newTagInput"
            size="small"
            style="width: 120px;"
            placeholder="新标签"
            @keyup.enter="addNewTag"
            @blur="showNewTagInput = false"
          />
          <el-button v-else size="small" @click="showNewTagInput = true">+ 新标签</el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="batchTagDialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="batchSelectedTags.length === 0" @click="handleBatchAddTags">确定</el-button>
      </template>
    </el-dialog>

    <div v-if="initialLoading" class="loading-container">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>

    <el-empty v-else-if="tagFilteredNotes.length === 0" description="暂无笔记" />

    <div v-else class="notes-list">
      <el-card
        v-for="note in tagFilteredNotes"
        :key="note.id"
        class="note-card"
        shadow="hover"
        @click="handleOpenNote(note.id)"
      >
        <el-checkbox
          class="note-checkbox"
          :model-value="selectedNoteIds.includes(note.id)"
          @change="(val: boolean) => toggleNoteSelect(note.id, val)"
          @click.stop
        />
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
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useNoteStore } from '@/stores/note';
import NoteCard from '@/components/common/NoteCard.vue';
import TemplateSelectDialog from '@/components/template/TemplateSelectDialog.vue';
import TagCloud from '@/components/tag/TagCloud.vue';
import TagFilterPanel from '@/components/tag/TagFilterPanel.vue';
import { fullTextSearch } from '@/api/note';
import { getTagCloud, getTagColors, batchAddTags } from '@/api/tag';
import type { TagCloudItem, TagColor } from '@/api/tag';
import { ElMessage, ElMessageBox } from 'element-plus';
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

// Tag management
const tagCloudData = ref<TagCloudItem[]>([]);
const tagColors = ref<TagColor[]>([]);
const selectedTags = ref<string[]>([]);
const selectedNoteIds = ref<number[]>([]);
const batchTagDialogVisible = ref(false);
const batchSelectedTags = ref<string[]>([]);
const showNewTagInput = ref(false);
const newTagInput = ref('');
const newTagInputRef = ref<any>(null);

const loadTagCloud = async () => {
  try {
    tagCloudData.value = await getTagCloud();
  } catch (e) { console.error(e); }
};

const loadTagColors = async () => {
  try {
    tagColors.value = await getTagColors();
  } catch (e) { console.error(e); }
};

watch(selectedTags, () => {
  // Client-side filter by tags
  if (selectedTags.value.length === 0) {
    fetchInitialNotes();
  } else {
    // Filter current notes client-side (tags are arrays in displayNotes)
    // For server-side we'd need backend support, but client-side is simpler
  }
});

const toggleNoteSelect = (id: number, val: boolean) => {
  if (val) selectedNoteIds.value.push(id);
  else selectedNoteIds.value = selectedNoteIds.value.filter(x => x !== id);
};

const toggleBatchTag = (name: string) => {
  const idx = batchSelectedTags.value.indexOf(name);
  if (idx >= 0) batchSelectedTags.value.splice(idx, 1);
  else batchSelectedTags.value.push(name);
};

const addNewTag = () => {
  const t = newTagInput.value.trim();
  if (t && !batchSelectedTags.value.includes(t)) {
    batchSelectedTags.value.push(t);
  }
  newTagInput.value = '';
  showNewTagInput.value = false;
};

const handleBatchAddTags = async () => {
  try {
    await batchAddTags(selectedNoteIds.value, batchSelectedTags.value);
    ElMessage.success('标签添加成功');
    batchTagDialogVisible.value = false;
    batchSelectedTags.value = [];
    selectedNoteIds.value = [];
    fetchInitialNotes();
    loadTagCloud();
  } catch (e) { console.error(e); }
};

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定将选中的 ${selectedNoteIds.value.length} 篇笔记移入回收站？`, '移入回收站', { type: 'warning' });
    for (const id of selectedNoteIds.value) {
      await noteStore.deleteNote(id);
    }
    ElMessage.success('已移入回收站');
    selectedNoteIds.value = [];
    fetchInitialNotes();
    loadTagCloud();
  } catch (e) { /* cancelled */ }
};

// Filter displayNotes by selected tags
const tagFilteredNotes = computed(() => {
  if (selectedTags.value.length === 0) return displayNotes.value;
  return displayNotes.value.filter(note => {
    const noteTags = Array.isArray(note.tags) ? note.tags : (note.tags ? note.tags.split(',').map((s: string) => s.trim()) : []);
    return selectedTags.value.every(t => noteTags.includes(t));
  });
});

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

const templateDialogRef = ref<InstanceType<typeof TemplateSelectDialog> | null>(null);

const handleTemplateApply = async (content: string) => {
  // Extract title from first # heading
  const titleMatch = content.match(/^#\s+(.+)$/m);
  const title = titleMatch ? titleMatch[1].replace(/\{\{[^}]+\}\}/g, '').trim() : '无标题';
  try {
    await noteStore.createNote({
      title,
      content,
      contentType: 'markdown',
    });
    // Reload and open the note
    await fetchInitialNotes();
    if (notes.value.length > 0) {
      router.push(`/notes/${notes.value[0].id}`);
    }
  } catch (error) {
    console.error('从模板创建失败:', error);
  }
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
  loadTagCloud();
  loadTagColors();
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

  .batch-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 16px;
    background: var(--el-color-primary-light-9);
    border-radius: 8px;
    margin-bottom: 16px;
    font-size: 14px;
  }

  .note-card {
    position: relative;

    .note-checkbox {
      position: absolute;
      top: 12px;
      right: 12px;
      z-index: 2;
    }
  }
}
</style>
