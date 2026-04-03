import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'

export const useThemeStore = defineStore('theme', () => {
  const mode = ref<ThemeMode>((localStorage.getItem('theme-mode') as ThemeMode) || 'system')

  function getEffectiveTheme(): 'light' | 'dark' {
    if (mode.value === 'system') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    return mode.value
  }

  function applyTheme() {
    const theme = getEffectiveTheme()
    document.documentElement.classList.toggle('dark', theme === 'dark')
    // 更新 Vditor 主题
    updateVditorTheme(theme)
  }

  function setMode(newMode: ThemeMode) {
    mode.value = newMode
    localStorage.setItem('theme-mode', newMode)
    applyTheme()
  }

  function updateVditorTheme(theme: 'light' | 'dark') {
    // 动态更新 Vditor 主题 CSS
    const contentThemeLink = document.querySelector('link[data-vditor-content-theme]')
    if (contentThemeLink) {
      contentThemeLink.setAttribute('href', `/vditor/dist/css/content-theme/${theme === 'dark' ? 'dark' : 'light'}.css`)
    }
  }

  function initTheme() {
    applyTheme()
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
      if (mode.value === 'system') {
        applyTheme()
      }
    })
  }

  return { mode, setMode, initTheme, getEffectiveTheme }
})
