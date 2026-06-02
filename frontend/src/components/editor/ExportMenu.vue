<template>
  <el-dropdown trigger="click" @command="handleExport">
    <el-button>
      <el-icon><Download /></el-icon>
      导出
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="md" :loading="loading">
          <span style="margin-right: 8px">📝</span>
          导出为 Markdown (.md)
        </el-dropdown-item>
        <el-dropdown-item command="pdf" :loading="loading">
          <span style="margin-right: 8px">📄</span>
          导出为 PDF (.pdf)
        </el-dropdown-item>
        <el-dropdown-item command="word" :loading="loading">
          <span style="margin-right: 8px">📑</span>
          导出为 Word (.docx)
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { exportMarkdown, exportPDF, exportWord, downloadFile } from '@/api/export';

const props = defineProps<{
  noteId: number;
  noteTitle: string;
}>();

const loading = ref(false);

const handleExport = async (type: string) => {
  if (loading.value) return;

  let password = '';
  if (type === 'pdf' || type === 'word') {
    try {
      const { value } = await ElMessageBox.prompt('如需加密，请输入导出文件的访问密码（留空则不加密）', '加密导出', {
        confirmButtonText: '导出',
        cancelButtonText: '取消',
        inputPlaceholder: '请输入密码',
        inputType: 'password'
      });
      password = value || '';
    } catch {
      return; // User cancelled
    }
  }

  loading.value = true;
  try {
    let blob: Blob;
    let filename: string;

    switch (type) {
      case 'md':
        blob = await exportMarkdown(props.noteId);
        filename = `${props.noteTitle}.md`;
        break;
      case 'pdf':
        blob = await exportPDF(props.noteId, password);
        filename = `${props.noteTitle}.pdf`;
        break;
      case 'word':
        blob = await exportWord(props.noteId, password);
        filename = `${props.noteTitle}.docx`;
        break;
      default:
        throw new Error('不支持的导出格式');
    }

    downloadFile(blob, filename);
    ElMessage.success('导出成功' + (password ? '（已加密）' : ''));
  } catch (error) {
    console.error('导出失败:', error);
    ElMessage.error('导出失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped lang="scss">
.el-dropdown-item {
  span {
    font-size: 16px;
  }
}
</style>
