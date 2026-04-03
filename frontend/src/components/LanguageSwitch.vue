<template>
  <el-dropdown trigger="click" @command="handleSwitch">
    <el-button text size="small">
      <el-icon><Connection /></el-icon>
      {{ currentLabel }}
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh-CN" :class="{ 'is-active': locale === 'zh-CN' }">
          🇨🇳 {{ t('i18n.zhCN') }}
        </el-dropdown-item>
        <el-dropdown-item command="en-US" :class="{ 'is-active': locale === 'en-US' }">
          🇺🇸 {{ t('i18n.enUS') }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Connection } from '@element-plus/icons-vue'

const { t, locale } = useI18n()

const currentLabel = computed(() => {
  return locale.value === 'en-US' ? 'EN' : '中'
})

function handleSwitch(lang: string) {
  locale.value = lang
  localStorage.setItem('locale', lang)
  // Reload to apply Element Plus locale
  window.location.reload()
}
</script>
