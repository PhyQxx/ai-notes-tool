<template>
  <div class="shared-note-view">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="8" animated />
    </div>
    <div v-else-if="needPassword && !verified" class="password-gate">
      <div class="password-card">
        <h2>🔒 受密码保护的笔记</h2>
        <p>请输入访问密码</p>
        <el-input v-model="password" placeholder="密码" show-password @keydown.enter="verifyPassword" />
        <el-button type="primary" style="width:100%;margin-top:16px" @click="verifyPassword">验证</el-button>
      </div>
    </div>
    <div v-else-if="error" class="error-state">
      <el-empty :description="error" />
    </div>
    <div v-else class="note-content">
      <div class="note-header">
        <h1>{{ noteData.title }}</h1>
        <span class="note-date">{{ formatDate(noteData.createdAt) }}</span>
      </div>
      <div class="note-body" v-html="renderedContent"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getSharedNote } from '@/api/noteShare';

const route = useRoute();
const slug = route.params.slug as string;

const loading = ref(true);
const error = ref('');
const password = ref('');
const needPassword = ref(false);
const verified = ref(false);
const noteData = ref({ title: '', content: '', createdAt: '' });

const formatDate = (date: string) => {
  if (!date) return '';
  return new Date(date).toLocaleString('zh-CN');
};

const renderedContent = computed(() => {
  if (!noteData.value.content) return '';
  // Simple markdown to HTML
  let html = noteData.value.content
    .replace(/^### (.*$)/gm, '<h3>$1</h3>')
    .replace(/^## (.*$)/gm, '<h2>$1</h2>')
    .replace(/^# (.*$)/gm, '<h1>$1</h1>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\[\[(.+?)\]\]/g, '<span class="wikilink">[[$1]]</span>')
    .replace(/\n/g, '<br>');
  return html;
});

const verifyPassword = async () => {
  await loadNote(password.value);
};

const loadNote = async (pwd?: string) => {
  loading.value = true;
  error.value = '';
  try {
    const data = await getSharedNote(slug, pwd);
    noteData.value = data;
    verified.value = true;
  } catch (e: any) {
    if (e?.response?.data?.message?.includes('密码') || e?.message?.includes('密码')) {
      needPassword.value = true;
    } else {
      error.value = e?.response?.data?.message || e?.message || '笔记不存在或已过期';
    }
  } finally {
    loading.value = false;
  }
};

onMounted(() => loadNote());
</script>

<style scoped lang="scss">
.shared-note-view {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;
  min-height: 100vh;
  background: #fff;
}

.loading, .error-state, .password-gate {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.password-card {
  text-align: center;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  max-width: 360px;

  h2 { margin-bottom: 8px; }
  p { color: #999; margin-bottom: 24px; }
}

.note-header {
  border-bottom: 1px solid #eee;
  padding-bottom: 16px;
  margin-bottom: 32px;

  h1 { margin: 0 0 8px; font-size: 28px; }
  .note-date { color: #999; font-size: 14px; }
}

.note-body {
  line-height: 1.8;
  font-size: 16px;
  color: #333;

  :deep(h1), :deep(h2), :deep(h3) { margin: 24px 0 12px; }
  :deep(code) { background: #f5f5f5; padding: 2px 6px; border-radius: 4px; font-size: 14px; }
  :deep(.wikilink) { color: #409eff; cursor: pointer; }
}
</style>
