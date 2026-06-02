<template>
  <div class="note-property-panel">
    <div class="panel-header">
      <span class="title">笔记属性</span>
      <el-button type="primary" link @click="handleAddProperty">
        <el-icon><Plus /></el-icon> 添加属性
      </el-button>
    </div>

    <div class="property-list">
      <div v-if="Object.keys(properties).length === 0" class="empty-hint">
        暂无属性，点击上方添加
      </div>
      <div v-for="(value, key) in properties" :key="key" class="property-item">
        <div class="property-key">
          <el-input v-model="tempKeys[key]" size="small" @blur="handleRenameKey(key)" placeholder="属性名" />
        </div>
        <div class="property-value">
          <el-select v-if="isStatusProperty(key)" v-model="properties[key]" size="small" @change="handleChange" placeholder="选择状态">
            <el-option label="进行中" value="In Progress" />
            <el-option label="已完成" value="Completed" />
            <el-option label="未开始" value="Not Started" />
          </el-select>
          <el-date-picker
            v-else-if="isDateProperty(key)"
            v-model="properties[key]"
            type="date"
            size="small"
            value-format="YYYY-MM-DD"
            @change="handleChange"
            placeholder="选择日期"
          />
          <el-input v-else v-model="properties[key]" size="small" @change="handleChange" placeholder="属性值" />
        </div>
        <el-button type="danger" link @click="handleRemoveProperty(key)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import { Plus, Delete } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const props = defineProps<{
  modelValue: Record<string, any>;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>];
  'change': [value: Record<string, any>];
}>();

const properties = reactive<Record<string, any>>({ ...props.modelValue });
const tempKeys = reactive<Record<string, string>>({});

// Initialize tempKeys
Object.keys(properties).forEach(key => {
  tempKeys[key] = key;
});

watch(() => props.modelValue, (newVal) => {
  Object.keys(properties).forEach(k => delete properties[k]);
  Object.assign(properties, newVal);
  Object.keys(newVal).forEach(key => {
    if (!tempKeys[key]) tempKeys[key] = key;
  });
}, { deep: true });

const isStatusProperty = (key: string) => key.toLowerCase().includes('status') || key.toLowerCase().includes('状态');
const isDateProperty = (key: string) => key.toLowerCase().includes('date') || key.toLowerCase().includes('日期') || key.toLowerCase().includes('时间');

const handleAddProperty = () => {
  const newKey = `属性${Object.keys(properties).length + 1}`;
  properties[newKey] = '';
  tempKeys[newKey] = newKey;
  handleChange();
};

const handleRemoveProperty = (key: string) => {
  delete properties[key];
  delete tempKeys[key];
  handleChange();
};

const handleRenameKey = (oldKey: string) => {
  const newKey = tempKeys[oldKey]?.trim();
  if (!newKey || newKey === oldKey) {
    tempKeys[oldKey] = oldKey;
    return;
  }
  if (properties[newKey] !== undefined) {
    ElMessage.warning('属性名已存在');
    tempKeys[oldKey] = oldKey;
    return;
  }
  
  const val = properties[oldKey];
  delete properties[oldKey];
  properties[newKey] = val;
  delete tempKeys[oldKey];
  tempKeys[newKey] = newKey;
  handleChange();
};

const handleChange = () => {
  emit('update:modelValue', { ...properties });
  emit('change', { ...properties });
};
</script>

<style scoped lang="scss">
.note-property-panel {
  padding: 16px;
  background-color: var(--el-fill-color-blank);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  margin-bottom: 16px;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .title {
      font-size: 14px;
      font-weight: 600;
      color: var(--el-text-color-primary);
    }
  }

  .property-list {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .empty-hint {
      text-align: center;
      color: var(--el-text-color-placeholder);
      font-size: 13px;
      padding: 12px 0;
    }

    .property-item {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .property-key {
        width: 120px;
        flex-shrink: 0;
      }
      
      .property-value {
        flex: 1;
        min-width: 0;
        
        :deep(.el-input), :deep(.el-select), :deep(.el-date-editor) {
          width: 100%;
        }
      }
    }
  }
}
</style>
