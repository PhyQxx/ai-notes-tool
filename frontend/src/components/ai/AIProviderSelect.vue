<template>
  <div class="ai-provider-select">
    <el-radio-group v-model="selectedProvider" size="default" @change="handleProviderChange">
      <el-radio-button
        v-for="provider in providers"
        :key="provider.name"
        :value="provider.name"
      >
        {{ provider.label }}
      </el-radio-button>
    </el-radio-group>

    <el-select
      v-model="selectedModel"
      placeholder="选择模型"
      size="default"
      style="width: 100%; margin-top: 12px"
      @change="handleModelChange"
    >
      <el-option
        v-for="model in currentModels"
        :key="model"
        :label="getModelDisplayName(model)"
        :value="model"
      />
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useAIStore } from '@/stores/ai';
import type { AIProviderInfo } from '@/types';

const props = defineProps<{
  provider?: string;
  model?: string;
}>();

const emit = defineEmits<{
  'update:provider': [value: string];
  'update:model': [value: string];
}>();

const aiStore = useAIStore();

// 模型名称映射
const MODEL_DISPLAY_NAMES: Record<string, string> = {
  'deepseek-chat': 'DeepSeek Chat',
  'deepseek-coder': 'DeepSeek Coder',
  'deepseek-reasoner': 'DeepSeek R1 (推理)',
  'glm-4': 'GLM-4',
  'glm-4-flash': 'GLM-4 Flash',
  'glm-3-turbo': 'GLM-3 Turbo',
  'gpt-4': 'GPT-4',
  'gpt-3.5-turbo': 'GPT-3.5',
};

const getModelDisplayName = (model: string): string => {
  return MODEL_DISPLAY_NAMES[model] || model;
};

const providers = ref<AIProviderInfo[]>([
  {
    name: 'deepseek',
    label: 'DeepSeek',
    models: ['deepseek-chat', 'deepseek-coder']
  },
  {
    name: 'glm',
    label: 'GLM',
    models: ['glm-4', 'glm-4-flash', 'glm-3-turbo']
  }
]);

const selectedProvider = ref(props.provider || 'deepseek');
const selectedModel = ref(props.model || 'deepseek-chat');

const currentModels = computed(() => {
  const provider = providers.value.find(p => p.name === selectedProvider.value);
  return provider?.models || [];
});

const handleProviderChange = (value: string) => {
  // 切换提供商时，选择该提供商的第一个模型
  const provider = providers.value.find(p => p.name === value);
  if (provider && provider.models.length > 0) {
    selectedModel.value = provider.models[0];
  }
  emit('update:provider', value);
  emit('update:model', selectedModel.value);
};

const handleModelChange = (value: string) => {
  emit('update:model', value);
};

// 监听props变化
watch(() => props.provider, (val) => {
  if (val) selectedProvider.value = val;
});

watch(() => props.model, (val) => {
  if (val) selectedModel.value = val;
});

onMounted(async () => {
  try {
    await aiStore.fetchProviders();
    if (aiStore.providers.length > 0) {
      providers.value = aiStore.providers;
    }
  } catch (error) {
    console.error('获取AI提供商失败:', error);
  }
});
</script>

<style scoped lang="scss">
.ai-provider-select {
  width: 100%;

  .el-radio-group {
    width: 100%;
    display: flex;

    :deep(.el-radio-button) {
      flex: 1;

      .el-radio-button__inner {
        width: 100%;
      }
    }
  }
}
</style>
