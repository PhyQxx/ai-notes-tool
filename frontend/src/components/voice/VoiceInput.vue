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
    <div v-if="isRecording" class="recording-indicator">
      <span class="pulse" />
      <span class="recording-text">{{ t('voice.listening') }}</span>
      <span class="transcript-text">{{ transcript || '...' }}</span>
      <el-button v-if="finalTranscript" type="primary" size="small" @click="handleInsert">
        {{ t('note.insertToEditor') }}
      </el-button>
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
import { Microphone } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const emit = defineEmits<{
  transcript: [text: string]
}>()

const isRecording = ref(false)
const transcript = ref('')
const finalTranscript = ref('')
const notSupported = ref(false)

// eslint-disable-next-line @typescript-eslint/no-explicit-any
let recognition: any = null

const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition

if (!SpeechRecognition) {
  notSupported.value = true
}

function toggleRecording() {
  if (!SpeechRecognition) {
    notSupported.value = true
    return
  }

  if (isRecording.value) {
    stopRecording()
  } else {
    startRecording()
  }
}

function startRecording() {
  recognition = new SpeechRecognition()
  recognition.continuous = true
  recognition.interimResults = true
  recognition.lang = localStorage.getItem('locale') === 'en-US' ? 'en-US' : 'zh-CN'

  transcript.value = ''
  finalTranscript.value = ''
  isRecording.value = true

  recognition.onresult = (event: { results: { isFinal: boolean; transcript: string }[][] }) => {
    let interim = ''
    let final = ''
    for (let i = 0; i < event.results.length; i++) {
      const result = event.results[i]
      if (result.isFinal) {
        final += result.transcript
      } else {
        interim += result.transcript
      }
    }
    transcript.value = interim
    if (final) {
      finalTranscript.value += final
    }
  }

  recognition.onerror = () => {
    isRecording.value = false
  }

  recognition.onend = () => {
    isRecording.value = false
  }

  recognition.start()
}

function stopRecording() {
  if (recognition) {
    recognition.stop()
  }
  isRecording.value = false
}

function handleInsert() {
  if (finalTranscript.value) {
    emit('transcript', finalTranscript.value)
    finalTranscript.value = ''
    transcript.value = ''
  }
}

onUnmounted(() => {
  if (recognition) {
    recognition.stop()
  }
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
