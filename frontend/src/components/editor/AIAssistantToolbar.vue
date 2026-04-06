<template>
  <div class="ai-assistant-toolbar" v-if="visible" :style="toolbarStyle" ref="toolbarRef">
    <div class="ai-toolbar-arrow" :style="arrowStyle"></div>
    <div class="ai-toolbar-loading" v-if="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>{{ loadingText }}</span>
    </div>
    <template v-else>
      <div class="ai-toolbar-item" @click="handleAction('summarize')" title="AI总结笔记">
        <span class="ai-toolbar-icon">📝</span>
        <span>总结</span>
      </div>
      <div class="ai-toolbar-item" @click="handleAction('outline')" title="AI生成大纲">
        <span class="ai-toolbar-icon">📋</span>
        <span>大纲</span>
      </div>
      <div class="ai-toolbar-item" @click="handleAction('continue')" title="AI续写">
        <span class="ai-toolbar-icon">✍️</span>
        <span>续写</span>
      </div>
      <div class="ai-toolbar-divider"></div>
      <div class="ai-toolbar-item" @click="handleAction('translate')" title="AI翻译">
        <span class="ai-toolbar-icon">🌐</span>
        <span>翻译</span>
      </div>
      <div class="ai-toolbar-item" @click="handleAction('polish')" title="AI润色">
        <span class="ai-toolbar-icon">✨</span>
        <span>润色</span>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { aiSummarize, aiOutline, aiContinue, aiTranslate, aiPolish } from '@/api/ai'

const props = defineProps<{
  visible: boolean
  x: number
  y: number
  selectedText: string
  fullContent: string
  noteId?: number | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'replace', text: string): void
  (e: 'insert', text: string): void
  (e: 'result', text: string, action: string): void
}>()

const loading = ref(false)
const loadingText = ref('')
const toolbarRef = ref<HTMLElement>()

const toolbarStyle = computed(() => ({
  left: `${props.x}px`,
  top: `${props.y - 48}px`,
}))

const arrowStyle = computed(() => ({
  left: '50%',
}))

const actionLabels: Record<string, string> = {
  summarize: '正在总结...',
  outline: '正在生成大纲...',
  continue: '正在续写...',
  translate: '正在翻译...',
  polish: '正在润色...',
}

function detectLang(text: string): 'zh' | 'en' {
  const zhChars = (text.match(/[\u4e00-\u9fff]/g) || []).length
  return zhChars > text.length * 0.3 ? 'en' : 'zh'
}

async function handleAction(action: string) {
  loading.value = true
  loadingText.value = actionLabels[action]

  try {
    let result = ''
    switch (action) {
      case 'summarize':
        result = (await aiSummarize(props.noteId, props.fullContent)).data
        break
      case 'outline':
        result = (await aiOutline(props.noteId, props.fullContent)).data
        break
      case 'continue': {
        const ctx = props.selectedText || props.fullContent
        result = (await aiContinue(ctx)).data
        emit('insert', result)
        break
      }
      case 'translate': {
        const targetLang = detectLang(props.selectedText)
        result = (await aiTranslate(props.selectedText, targetLang)).data
        emit('replace', result)
        break
      }
      case 'polish': {
        result = (await aiPolish(props.selectedText)).data
        emit('replace', result)
        break
      }
    }

    if (action === 'summarize' || action === 'outline') {
      emit('result', result, action)
    }

    ElMessage.success({ message: 'AI 处理完成', grouping: true, duration: 2000 })
  } catch (err: any) {
    ElMessage.error(err.message || 'AI 处理失败')
  } finally {
    loading.value = false
    emit('close')
  }
}

function handleClickOutside(e: MouseEvent) {
  if (toolbarRef.value && !toolbarRef.value.contains(e.target as Node)) {
    emit('close')
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('mousedown', handleClickOutside)
})
</script>

<style scoped>
.ai-assistant-toolbar {
  position: fixed;
  z-index: 3000;
  display: flex;
  align-items: center;
  gap: 2px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 4px 6px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateX(-50%);
  white-space: nowrap;
}

.ai-toolbar-arrow {
  position: absolute;
  bottom: -6px;
  width: 12px;
  height: 12px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  transform: translateX(-50%) rotate(45deg);
}

.ai-toolbar-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #303133;
  transition: all 0.2s;
  user-select: none;
}

.ai-toolbar-item:hover {
  background: #EDE9FE;
  color: #8B5CF6;
}

.ai-toolbar-icon {
  font-size: 14px;
}

.ai-toolbar-divider {
  width: 1px;
  height: 20px;
  background: #e4e7ed;
  margin: 0 2px;
}

.ai-toolbar-loading {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  color: #909399;
  font-size: 13px;
}
</style>
