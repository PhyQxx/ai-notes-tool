<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

onMounted(() => {
  // 初始化认证状态
  authStore.initAuth();
});
</script>

<style lang="scss">
@import '@/styles/global.scss';
</style>
