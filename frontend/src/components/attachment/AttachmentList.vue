<template>
  <div class="attachment-panel">
    <div class="panel-header">
      <span>📎 {{ $t('attachment.title') || '附件' }}</span>
      <el-upload
        :action="''"
        :http-request="handleUpload"
        :show-file-list="false"
        :multiple="true"
      >
        <el-button size="small" type="primary" :icon="Upload">上传</el-button>
      </el-upload>
    </div>

    <div v-if="uploading" class="upload-progress">
      <el-progress :percentage="progress" :stroke-width="6" />
    </div>

    <div v-if="attachments.length === 0" class="empty-tip">
      {{ $t('attachment.empty') || '暂无附件' }}
    </div>

    <el-scrollbar v-else max-height="240px">
      <div v-for="att in attachments" :key="att.id" class="att-item">
        <el-icon class="att-icon"><Document /></el-icon>
        <div class="att-info" @click="handleDownload(att)">
          <span class="att-name" :title="att.fileName">{{ att.fileName }}</span>
          <span class="att-size">{{ formatSize(att.fileSize) }}</span>
        </div>
        <el-button type="danger" link size="small" @click="handleDelete(att)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Upload, Document, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAttachments, uploadAttachment, deleteAttachment, getAttachmentDownloadUrl, type NoteAttachment } from '@/api/attachment'

const props = defineProps<{ noteId: number }>()
const attachments = ref<NoteAttachment[]>([])
const uploading = ref(false)
const progress = ref(0)

async function loadList() {
  try {
    const res = await listAttachments(props.noteId)
    attachments.value = res || []
  } catch { /* ignore */ }
}

async function handleUpload({ file }: { file: File }) {
  uploading.value = true
  progress.value = 0
  try {
    await uploadAttachment(props.noteId, file, p => { progress.value = p })
    ElMessage.success('上传成功')
    await loadList()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

function handleDownload(att: NoteAttachment) {
  const url = getAttachmentDownloadUrl(props.noteId, att.id)
  const token = localStorage.getItem('token') || ''
  const a = document.createElement('a')
  a.href = url + (url.includes('?') ? '&' : '?') + 'token=' + token
  a.download = att.fileName
  a.click()
}

async function handleDelete(att: NoteAttachment) {
  await ElMessageBox.confirm(`确定删除附件 "${att.fileName}" ?`, '提示', { type: 'warning' })
  try {
    await deleteAttachment(props.noteId, att.id)
    ElMessage.success('已删除')
    await loadList()
  } catch { /* cancel */ }
}

function formatSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

onMounted(loadList)
</script>

<style scoped>
.attachment-panel { border-top: 1px solid var(--el-border-color-lighter); padding: 12px; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-weight: 500; font-size: 13px; }
.upload-progress { margin-bottom: 8px; }
.empty-tip { color: var(--el-text-color-placeholder); font-size: 12px; text-align: center; padding: 16px 0; }
.att-item { display: flex; align-items: center; gap: 8px; padding: 6px 4px; border-radius: 4px; }
.att-item:hover { background: var(--el-fill-color-light); }
.att-icon { color: var(--el-color-primary); font-size: 16px; flex-shrink: 0; }
.att-info { flex: 1; min-width: 0; cursor: pointer; }
.att-name { display: block; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.att-size { font-size: 11px; color: var(--el-text-color-secondary); }
</style>
