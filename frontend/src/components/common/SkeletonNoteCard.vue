<template>
  <div class="skeleton-card">
    <div class="skeleton-header">
      <el-skeleton-item variant="h3" style="width: 60%" />
      <el-skeleton-item variant="text" style="width: 20%" />
    </div>
    <el-skeleton :rows="3" animated />
    <div class="skeleton-footer">
      <el-skeleton-item variant="text" style="width: 30%" />
      <el-skeleton-item variant="text" style="width: 15%" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.skeleton-card {
  padding: 20px;
  border-radius: 8px;
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);

  .skeleton-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .skeleton-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16px;
  }
}
</style>
