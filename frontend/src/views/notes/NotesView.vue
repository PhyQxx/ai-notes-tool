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
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

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

const router = useRouter();
const noteStore = useNoteStore();

const searchKeyword = ref('');
const sortBy = ref('updatedAt');

const notes = computed(() => noteStore.notes);
const loading = computed(() => noteStore.loading);
const total = computed(() => noteStore.total);
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

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    noteStore.search(searchKeyword.value.trim());
  } else {
    noteStore.fetchNotes();
  }
};

const handleSort = () => {
  notes.value.sort((a: any, b: any) => {
    if (sortBy.value === 'title') {
      return a.title.localeCompare(b.title);
    }
    return new Date(b[sortBy.value]).getTime() - new Date(a[sortBy.value]).getTime();
  });
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

onMounted(() => {
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
}
</style>
