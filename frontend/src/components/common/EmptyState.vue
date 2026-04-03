<template>
  <div class="empty-state">
    <el-empty :description="description" :image-size="size">
      <template #description>
        <p class="empty-desc">{{ description }}</p>
      </template>
      <template #image>
        <slot name="image">
          <el-empty :image-size="size" />
        </slot>
      </template>
      <template #default>
        <div class="empty-actions">
          <el-button v-if="actionText" type="primary" @click="$emit('action')">
            <el-icon v-if="actionIcon"><component :is="actionIcon" /></el-icon>
            {{ actionText }}
          </el-button>
          <el-button v-if="secondaryText" @click="$emit('secondary')">
            {{ secondaryText }}
          </el-button>
        </div>
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  description?: string
  actionText?: string
  actionIcon?: string
  secondaryText?: string
  size?: number
}>()
defineEmits<{
  action: []
  secondary: []
}>()
</script>

<style scoped lang="scss">
.empty-state {
  padding: 40px 0;

  .empty-desc {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin: 8px 0 0;
  }

  .empty-actions {
    display: flex;
    gap: 12px;
    margin-top: 16px;
  }
}
</style>
