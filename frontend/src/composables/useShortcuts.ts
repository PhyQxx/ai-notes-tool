import { onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'

export function useShortcuts() {
  const router = useRouter()

  function handleKeydown(e: KeyboardEvent) {
    // Ctrl+S 保存
    if (e.ctrlKey && !e.shiftKey && e.key === 's') {
      e.preventDefault()
      window.dispatchEvent(new CustomEvent('note:save'))
    }
    // Ctrl+Shift+N 新建
    if (e.ctrlKey && e.shiftKey && e.key === 'N') {
      e.preventDefault()
      router.push('/notes/new')
    }
    // Ctrl+Shift+F 全局搜索
    if (e.ctrlKey && e.shiftKey && e.key === 'F') {
      e.preventDefault()
      router.push('/?focusSearch=true')
    }
    // Ctrl+/ 快捷键帮助
    if (e.ctrlKey && e.key === '/') {
      e.preventDefault()
      ElMessageBox.alert(
        `<div style="text-align:left;line-height:2">
          <b>Ctrl+S</b> — 保存笔记<br/>
          <b>Ctrl+Shift+N</b> — 新建笔记<br/>
          <b>Ctrl+Shift+F</b> — 全局搜索<br/>
          <b>Ctrl+/</b> — 显示此帮助
        </div>`,
        '快捷键',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '知道了',
          customStyle: { maxWidth: '360px' }
        }
      )
    }
  }

  onMounted(() => document.addEventListener('keydown', handleKeydown))
  onBeforeUnmount(() => document.removeEventListener('keydown', handleKeydown))
}
