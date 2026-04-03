<template>
  <div class="error-state">
    <el-result :icon="icon" :title="title" :sub-title="message">
      <template #extra>
        <el-button type="primary" @click="$emit('retry')">
          <el-icon><RefreshRight /></el-icon>
          重试
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RefreshRight } from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  title?: string
  message?: string
  type?: 'network' | 'permission' | 'data'
}>(), {
  type: 'network'
})

defineEmits<{
  retry: []
}>()

const icon = computed(() => {
  switch (props.type) {
    case 'permission': return 'warning'
    case 'data': return 'info'
    default: return 'error'
  }
})

const title = computed(() => {
  if (props.title) return props.title
  switch (props.type) {
    case 'permission': return '权限不足'
    case 'data': return '数据异常'
    default: return '网络错误'
  }
})

const message = computed(() => {
  if (props.message) return props.message
  switch (props.type) {
    case 'permission': return '您没有权限执行此操作，请联系管理员'
    case 'data': return '数据加载异常，请稍后重试'
    default: return '无法连接到服务器，请检查网络后重试'
  }
})
</script>

<style scoped lang="scss">
.error-state {
  padding: 40px 0;
}
</style>
