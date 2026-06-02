<template>
  <div class="voice-input-wrapper">
    <el-tooltip :content="isRecording ? t('voice.stop') : t('voice.start')" placement="bottom">
      <el-button
        :type="isRecording ? 'danger' : 'default'"
        :icon="Microphone"
        circle
        size="small"
        @click="toggleRecording"
      />
    </el-tooltip>
    <div v-if="isRecording || isTranscribing" class="recording-indicator">
      <span v-if="isRecording" class="pulse" />
      <el-icon v-else class="is-loading"><Loading /></el-icon>
      <span class="recording-text">{{ isRecording ? t('voice.listening') : '正在转写...' }}</span>
      <span class="transcript-text">{{ transcript || '...' }}</span>
    </div>
    <el-alert
      v-if="notSupported"
      :title="t('voice.notSupported')"
      type="warning"
      :closable="false"
      show-icon
      style="margin-top: 4px; padding: 4px 8px;"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { Microphone, Loading } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const emit = defineEmits<{
  transcript: [text: string]
}>()

const isRecording = ref(false)
const isTranscribing = ref(false)
const transcript = ref('')
const notSupported = ref(false)

let mediaRecorder: MediaRecorder | null = null
let audioChunks: Blob[] = []

if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
  notSupported.value = true
}

async function toggleRecording() {
  if (isRecording.value) {
    stopRecording()
  } else {
    await startRecording()
  }
}

async function startRecording() {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder = new MediaRecorder(stream)
    audioChunks = []

    mediaRecorder.ondataavailable = (event) => {
      audioChunks.push(event.data)
    }

    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm' })
      await transcribeAudio(audioBlob)
    }

    mediaRecorder.start()
    isRecording.value = true
    transcript.value = '正在录音...'
  } catch (err) {
    console.error('无法开启麦克风', err)
    ElMessage.error('无法开启麦克风，请检查权限')
  }
}

function stopRecording() {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop()
    mediaRecorder.stream.getTracks().forEach(track => track.stop())
    isRecording.value = false
  }
}

async function transcribeAudio(blob: Blob) {
  isTranscribing.value = true
  transcript.value = '正在转换文字...'
  
  try {
    const formData = new FormData()
    formData.append('file', blob, 'recording.webm')
    
    const token = localStorage.getItem('token')
    const response = await fetch('/api/ai/voice/transcribe', {
      method: 'POST',
      headers: { 'Authorization': token ? `Bearer ${token}` : '' },
      body: formData
    })
    
    const result = await response.json()
    if (result.code === 200 && result.data) {
      transcript.value = result.data
      handleInsert()
    } else {
      ElMessage.error(result.message || '转写失败')
      transcript.value = ''
    }
  } catch (error) {
    console.error('转写失败', error)
    ElMessage.error('语音转写失败')
    transcript.value = ''
  } finally {
    isTranscribing.value = false
  }
}

function handleInsert() {
  if (transcript.value) {
    emit('transcript', transcript.value)
    // ElMessage.success('已插入转写内容')
    setTimeout(() => { transcript.value = '' }, 2000)
  }
}

onUnmounted(() => {
  stopRecording()
})
</script>

<style scoped>
.voice-input-wrapper {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;
}
.recording-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef0f0;
  border-radius: 8px;
  font-size: 13px;
  max-width: 400px;
}
.pulse {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  animation: pulse-anim 1.2s ease-in-out infinite;
}
@keyframes pulse-anim {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.3); }
}
.recording-text {
  color: #f56c6c;
  font-weight: 500;
  white-space: nowrap;
}
.transcript-text {
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
