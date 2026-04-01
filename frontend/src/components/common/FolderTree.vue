<template>
  <el-tree
    ref="treeRef"
    :data="folders"
    :props="treeProps"
    node-key="id"
    :expand-on-click-node="false"
    :highlight-current="true"
    @node-click="handleNodeClick"
  >
    <template #default="{ node, data }">
      <div class="custom-tree-node">
        <span class="node-label">{{ node.label }}</span>
        <span class="node-actions" @click.stop>
          <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, data)">
            <el-icon size="16">
              <MoreFilled />
            </el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="add">
                  <el-icon><Plus /></el-icon>
                  新建子文件夹
                </el-dropdown-item>
                <el-dropdown-item command="rename">
                  <el-icon><Edit /></el-icon>
                  重命名
                </el-dropdown-item>
                <el-dropdown-item command="delete" divided>
                  <el-icon><Delete /></el-icon>
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </span>
      </div>
    </template>
  </el-tree>

  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="400px"
  >
    <el-form>
      <el-form-item label="文件夹名称">
        <el-input
          v-model="folderName"
          placeholder="请输入文件夹名称"
          @keyup.enter="handleConfirm"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useNoteStore } from '@/stores/note';
import type { Folder } from '@/types';

defineProps<{
  isCollapse?: boolean;
}>();

const router = useRouter();
const noteStore = useNoteStore();

const dialogVisible = ref(false);
const dialogTitle = ref('');
const folderName = ref('');
const currentFolder = ref<Folder | null>(null);
const dialogType = ref<'add' | 'rename'>('add');

const folders = computed(() => noteStore.folders);

const treeProps = {
  children: 'children',
  label: 'name'
};

const handleNodeClick = (data: Folder) => {
  router.push(`/notes?folderId=${data.id}`);
};

const handleCommand = (command: string, data: Folder) => {
  currentFolder.value = data;

  switch (command) {
    case 'add':
      dialogTitle.value = '新建文件夹';
      dialogType.value = 'add';
      folderName.value = '';
      dialogVisible.value = true;
      break;
    case 'rename':
      dialogTitle.value = '重命名文件夹';
      dialogType.value = 'rename';
      folderName.value = data.name;
      dialogVisible.value = true;
      break;
    case 'delete':
      handleDeleteFolder(data);
      break;
  }
};

const handleConfirm = async () => {
  if (!folderName.value.trim()) {
    ElMessage.warning('请输入文件夹名称');
    return;
  }

  try {
    if (dialogType.value === 'add') {
      await noteStore.createFolder({
        name: folderName.value,
        parentId: currentFolder.value?.id
      });
    } else if (dialogType.value === 'rename' && currentFolder.value) {
      await noteStore.updateFolder(currentFolder.value.id, {
        name: folderName.value
      });
    }
    await noteStore.fetchFolders();
    dialogVisible.value = false;
    ElMessage.success('操作成功');
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
  }
};

const handleDeleteFolder = async (folder: Folder) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件夹"${folder.name}"吗？删除后文件夹中的笔记将不会被删除。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    await noteStore.deleteFolder(folder.id);
    await noteStore.fetchFolders();
    ElMessage.success('删除成功');
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};
</script>

<style scoped lang="scss">
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;

  .node-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .node-actions {
    opacity: 0;
    transition: opacity 0.2s;

    .el-icon {
      cursor: pointer;
      padding: 4px;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  &:hover {
    .node-actions {
      opacity: 1;
    }
  }
}
</style>
