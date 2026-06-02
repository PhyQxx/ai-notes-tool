<template>
  <div class="canvas-view">
    <div class="canvas-toolbar">
      <div class="toolbar-left">
        <el-button :icon="ArrowLeft" circle @click="router.push('/canvas')" />
        <h3 class="canvas-title">{{ canvasTitle }}</h3>
        <el-tag v-if="saving" type="info" size="small" class="save-status">保存中...</el-tag>
        <el-tag v-else type="success" size="small" class="save-status">已保存</el-tag>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" size="small" @click="handleSave">手动保存</el-button>
        <el-button size="small" @click="handleAddNote">添加笔记卡片</el-button>
      </div>
    </div>

    <div class="canvas-container">
      <VueFlow
        v-model="elements"
        :default-viewport="{ x: 0, y: 0, zoom: 1.5 }"
        :min-zoom="0.2"
        :max-zoom="4"
        @connect="onConnect"
      >
        <Background />
        <Controls />
        
        <!-- Custom Note Node -->
        <template #node-note="nodeProps">
          <div class="note-node" :style="{ borderColor: nodeProps.data.color || 'var(--el-border-color)' }">
            <div class="node-header">
              <span class="node-title">{{ nodeProps.data.title }}</span>
              <el-icon class="node-link" @click="router.push(`/notes/${nodeProps.data.noteId}`)"><Link /></el-icon>
            </div>
            <div class="node-content">
              {{ nodeProps.data.preview }}
            </div>
            <Handle type="target" :position="Position.Top" />
            <Handle type="source" :position="Position.Bottom" />
          </div>
        </template>
      </VueFlow>
    </div>

    <!-- Note Selection Dialog -->
    <el-dialog v-model="noteDialogVisible" title="选择笔记添加到画布" width="500px">
      <el-input v-model="noteSearch" placeholder="搜索笔记..." :prefix-icon="Search" class="mb-4" />
      <div class="note-selection-list">
        <div 
          v-for="note in filteredNotes" 
          :key="note.id" 
          class="selection-item"
          @click="addNoteToCanvas(note)"
        >
          <span class="title">{{ note.title || '无标题' }}</span>
          <span class="date">{{ new Date(note.updatedAt).toLocaleDateString() }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Link, Search } from '@element-plus/icons-vue';
import { VueFlow, useVueFlow, Position, Handle, type Elements, type Connection, type Edge, type Node } from '@vue-flow/core';
import { Background } from '@vue-flow/background';
import { Controls } from '@vue-flow/controls';
import { getCanvas, updateCanvas } from '@/api/canvas';
import { listNotes } from '@/api/note';

import '@vue-flow/core/dist/style.css';
import '@vue-flow/core/dist/theme-default.css';
import '@vue-flow/controls/dist/style.css';

const route = useRoute();
const router = useRouter();
const canvasId = Number(route.params.id);

const canvasTitle = ref('加载中...');
const elements = ref<Elements>([]);
const saving = ref(false);
const loading = ref(true);

const noteDialogVisible = ref(false);
const noteSearch = ref('');
const allNotes = ref<any[]>([]);

const { onPaneReady, addNodes, addEdges, toObject } = useVueFlow();

const filteredNotes = computed(() => {
  const kw = noteSearch.value.toLowerCase();
  return allNotes.value.filter(n => 
    n.title?.toLowerCase().includes(kw) || 
    n.content?.toLowerCase().includes(kw)
  ).slice(0, 20);
});

const loadCanvasData = async () => {
  try {
    const res = await getCanvas(canvasId);
    canvasTitle.value = res.title;
    if (res.data) {
      const parsed = JSON.parse(res.data);
      elements.value = [...(parsed.nodes || []), ...(parsed.edges || [])];
    }
  } catch (error) {
    ElMessage.error('加载画布数据失败');
  } finally {
    loading.value = false;
  }
};

const handleSave = async () => {
  saving.value = true;
  try {
    const data = JSON.stringify(toObject());
    await updateCanvas(canvasId, data);
  } catch (error) {
    console.error('保存失败', error);
  } finally {
    saving.value = false;
  }
};

const handleAddNote = async () => {
  noteDialogVisible.value = true;
  if (allNotes.value.length === 0) {
    const res = await listNotes({ page: 1, size: 100 });
    allNotes.value = (res as any).records || res as any;
  }
};

const addNoteToCanvas = (note: any) => {
  const id = `note-${note.id}-${Date.now()}`;
  const newNode: Node = {
    id,
    type: 'note',
    position: { x: Math.random() * 400, y: Math.random() * 400 },
    data: {
      noteId: note.id,
      title: note.title || '无标题',
      preview: (note.content || '').substring(0, 100) + '...',
      color: note.color
    }
  };
  elements.value.push(newNode);
  noteDialogVisible.value = false;
  handleSave();
};

const onConnect = (params: Connection) => {
  const newEdge: Edge = {
    ...params,
    id: `e-${params.source}-${params.target}`,
    animated: true,
    style: { stroke: '#5B7FFF', strokeWidth: 2 }
  };
  elements.value.push(newEdge);
  handleSave();
};

// Auto-save logic
let autoSaveTimer: any = null;
watch(elements, () => {
  if (loading.value) return;
  if (autoSaveTimer) clearTimeout(autoSaveTimer);
  autoSaveTimer = setTimeout(handleSave, 2000);
}, { deep: true });

onMounted(loadCanvasData);
</script>

<style scoped lang="scss">
.canvas-view {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  margin: -24px; // Offset layout padding

  .canvas-toolbar {
    height: 50px;
    background-color: var(--el-bg-color);
    border-bottom: 1px solid var(--el-border-color-lighter);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 16px;
    z-index: 10;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;
      .canvas-title { margin: 0; font-size: 16px; font-weight: 600; }
      .save-status { font-size: 10px; }
    }
  }

  .canvas-container {
    flex: 1;
    position: relative;
    background-color: var(--el-fill-color-extra-light);

    :deep(.vue-flow__node-note) {
      width: 200px;
    }
  }

  .note-node {
    background: var(--el-bg-color);
    border: 2px solid var(--el-border-color);
    border-radius: 8px;
    padding: 10px;
    box-shadow: var(--el-box-shadow-light);
    font-size: 12px;
    
    .node-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;
      padding-bottom: 6px;
      border-bottom: 1px solid var(--el-border-color-extra-light);
      
      .node-title {
        font-weight: 600;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        flex: 1;
      }
      .node-link {
        cursor: pointer;
        color: var(--el-color-primary);
        &:hover { opacity: 0.8; }
      }
    }
    
    .node-content {
      color: var(--el-text-color-secondary);
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
      line-height: 1.4;
    }
  }

  .note-selection-list {
    max-height: 400px;
    overflow-y: auto;
    
    .selection-item {
      padding: 10px 12px;
      border-bottom: 1px solid var(--el-border-color-extra-light);
      cursor: pointer;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      &:hover { background-color: var(--el-fill-color-light); }
      &:last-child { border-bottom: none; }
      
      .title { font-weight: 500; }
      .date { font-size: 12px; color: var(--el-text-color-placeholder); }
    }
  }
}

.mb-4 { margin-bottom: 16px; }
</style>
