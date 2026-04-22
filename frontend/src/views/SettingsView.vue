<template>
  <div class="settings-view">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <h3>设置</h3>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane name="profile">
          <template #label><el-icon><User /></el-icon> 个人信息</template>
          <el-form :model="profileForm" label-width="100px" style="max-width: 600px">
            <el-form-item label="头像">
              <div class="avatar-upload">
                <el-avatar :size="80" :src="profileForm.avatar || defaultAvatar">
                  {{ profileForm.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <el-button type="primary" link style="margin-left: 16px">
                  更换头像
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>

            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" disabled />
            </el-form-item>

            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveProfile">
                保存
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="password">
          <template #label><el-icon><Lock /></el-icon> 修改密码</template>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            style="max-width: 600px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
                @keyup.enter="handleChangePassword"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="changing" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="appearance">
          <template #label><el-icon><Brush /></el-icon> 外观设置</template>
          <el-form label-width="100px" style="max-width: 600px">
            <el-form-item label="主题">
              <el-radio-group v-model="theme">
                <el-radio-button value="light">浅色</el-radio-button>
                <el-radio-button value="dark">深色</el-radio-button>
                <el-radio-button value="auto">跟随系统</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="字体大小">
              <el-radio-group v-model="fontSize">
                <el-radio-button value="small">小</el-radio-button>
                <el-radio-button value="default">默认</el-radio-button>
                <el-radio-button value="large">大</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane name="ai">
          <template #label><el-icon><Monitor /></el-icon> AI设置</template>
          <el-form :model="aiConfig" label-width="120px" style="max-width: 600px">
            <el-form-item label="AI提供商">
              <el-radio-group v-model="aiConfig.provider">
                <el-radio value="deepseek">DeepSeek</el-radio>
                <el-radio value="glm">GLM</el-radio>
                <el-radio value="minimax">MiniMax</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item v-if="aiConfig.provider === 'deepseek'" label="DeepSeek API Key">
              <el-input
                v-model="aiConfig.deepseekApiKey"
                type="password"
                placeholder="请输入DeepSeek API Key"
                show-password
              />
              <div class="ai-hint">
                免费API Key，前往 <a href="https://platform.deepseek.com" target="_blank">platform.deepseek.com</a> 注册申请
              </div>
            </el-form-item>

            <el-form-item v-if="aiConfig.provider === 'glm'" label="GLM API Key">
              <el-input
                v-model="aiConfig.glmApiKey"
                type="password"
                placeholder="请输入GLM API Key"
                show-password
              />
              <div class="ai-hint">
                免费API Key，前往 <a href="https://open.bigmodel.cn" target="_blank">open.bigmodel.cn</a> 注册申请
              </div>
            </el-form-item>

            <el-form-item v-if="aiConfig.provider === 'minimax'" label="MiniMax API Key">
              <el-input
                v-model="aiConfig.minimaxApiKey"
                type="password"
                placeholder="请输入MiniMax API Key"
                show-password
              />
              <div class="ai-hint">
                免费API Key，前往 <a href="https://platform.minimax.chat" target="_blank">platform.minimax.chat</a> 注册申请
              </div>
            </el-form-item>

            <el-form-item label="默认模型">
              <el-select
                v-model="aiConfig.model"
                placeholder="选择或输入模型名称"
                filterable
                allow-create
                default-first-option
                style="width: 100%"
              >
                <el-option
                  v-for="model in currentModels"
                  :key="model"
                  :label="model"
                  :value="model"
                />
              </el-select>
            </el-form-item>

            <el-form-item>
              <div class="api-key-status" :class="aiConfig.hasApiKey ? 'configured' : 'missing'">
                <el-icon v-if="aiConfig.hasApiKey"><CircleCheckFilled /></el-icon>
                <el-icon v-else><CircleCloseFilled /></el-icon>
                <span v-if="aiConfig.hasApiKey">已配置 API Key</span>
                <span v-else>未配置 API Key，请先填写上方表单</span>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveAIConfig">
                保存配置
              </el-button>
              <el-button :loading="testing" @click="handleTestAIConfig">
                <el-icon v-if="aiConnected && !testing"><CircleCheckFilled style="color: var(--nt-success)" /></el-icon>
                <el-icon v-else-if="!aiConnected && !testing"><CircleCloseFilled style="color: var(--nt-danger)" /></el-icon>
                测试连接
              </el-button>
              <span v-if="aiConnected" class="status-text status-text--success">已连接</span>
              <span v-else-if="tested" class="status-text status-text--error">连接失败</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { User, Lock, Brush, Monitor, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';
import { useAIStore } from '@/stores/ai';
import { updateProfile } from '@/api/auth';

const authStore = useAuthStore();
const aiStore = useAIStore();
const route = useRoute();

const activeTab = ref('profile');
const saving = ref(false);
const changing = ref(false);
const testing = ref(false);
const aiConnected = ref(false);
const tested = ref(false);

const aiConfig = reactive({
  provider: 'deepseek',
  model: 'deepseek-chat',
  deepseekApiKey: '',
  glmApiKey: '',
  minimaxApiKey: '',
  hasApiKey: false
});

const currentModels = computed(() => {
  if (aiConfig.provider === 'deepseek') {
    return ['deepseek-chat', 'deepseek-coder', 'deepseek-reasoner'];
  } else if (aiConfig.provider === 'minimax') {
    return ['MiniMax-M2.7'];
  } else {
    return ['glm-4', 'glm-4-flash', 'glm-3-turbo'];
  }
});

const passwordFormRef = ref<FormInstance>();

const profileForm = reactive({
  username: '',
  email: '',
  nickname: '',
  avatar: ''
});

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const theme = ref('light');
const fontSize = ref('default');

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const validateNewPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入新密码'));
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'));
  } else {
    callback();
  }
};

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'));
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

const handleSaveProfile = async () => {
  saving.value = true;
  try {
    await updateProfile({
      nickname: profileForm.nickname,
      avatar: profileForm.avatar
    });
    await authStore.fetchProfile();
    ElMessage.success('保存成功');
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败');
  } finally {
    saving.value = false;
  }
};

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return;

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return;

    changing.value = true;
    try {
      // TODO: 调用修改密码API
      ElMessage.success('密码修改成功');
      passwordForm.oldPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
    } catch (error: any) {
      ElMessage.error(error.message || '修改密码失败');
    } finally {
      changing.value = false;
    }
  });
};

const handleSaveAIConfig = async () => {
  saving.value = true;
  try {
    await aiStore.updateConfig({
      provider: aiConfig.provider,
      model: aiConfig.model,
      deepseekApiKey: aiConfig.provider === 'deepseek' ? aiConfig.deepseekApiKey : undefined,
      glmApiKey: aiConfig.provider === 'glm' ? aiConfig.glmApiKey : undefined,
      minimaxApiKey: aiConfig.provider === 'minimax' ? aiConfig.minimaxApiKey : undefined
    });
    ElMessage.success('AI配置保存成功');
  } catch (error: any) {
    ElMessage.error(error.message || '保存配置失败');
  } finally {
    saving.value = false;
  }
};

const handleTestAIConfig = async () => {
  testing.value = true;
  aiConnected.value = false;
  tested.value = false;
  try {
    const testProvider = aiConfig.provider;
    let testApiKey = '';
    if (testProvider === 'deepseek') testApiKey = aiConfig.deepseekApiKey;
    else if (testProvider === 'glm') testApiKey = aiConfig.glmApiKey;
    else if (testProvider === 'minimax') testApiKey = aiConfig.minimaxApiKey;
    const res = await fetch('/api/ai/config/test', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        provider: testProvider,
        apiKey: testApiKey,
        model: aiConfig.model
      })
    });
    const data = await res.json();
    aiConnected.value = data.success && data.data?.success;
    tested.value = true;
    if (aiConnected.value) {
      ElMessage.success('API连接测试成功 ✓');
    } else {
      ElMessage.error('API连接测试失败，请检查Key和网络');
    }
  } catch (error: any) {
    aiConnected.value = false;
    tested.value = true;
    ElMessage.error(error.message || 'API连接测试失败');
  } finally {
    testing.value = false;
  }
};

onMounted(async () => {
  if (authStore.user) {
    profileForm.username = authStore.user.username;
    profileForm.email = authStore.user.email;
    profileForm.nickname = authStore.user.nickname;
    profileForm.avatar = authStore.user.avatar;
  }

  // 加载AI配置
  try {
    await aiStore.fetchConfig();
    aiConfig.provider = aiStore.config.provider;
    aiConfig.model = aiStore.config.model;
    aiConfig.deepseekApiKey = aiStore.config.deepseekApiKey || '';
    aiConfig.glmApiKey = aiStore.config.glmApiKey || '';
    aiConfig.minimaxApiKey = aiStore.config.minimaxApiKey || '';
    aiConfig.hasApiKey = aiStore.config.hasApiKey || false;
  } catch (error) {
    console.error('加载AI配置失败:', error);
  }

  // If navigated from chat with ?tab=ai, switch to AI tab
  const tab = route.query.tab as string;
  if (tab === 'ai') {
    activeTab.value = 'ai';
  }
});
</script>

<style scoped lang="scss">
.settings-view {
  max-width: 960px;
  margin: 0 auto;

  .settings-card {
    border-radius: var(--nt-radius-xl);
    box-shadow: var(--nt-shadow-sm);

    .card-header {
      h3 {
        margin: 0;
        font-size: 20px;
        font-weight: 700;
      }
    }

    .avatar-upload {
      display: flex;
      align-items: center;
    }
  }

  .status-text {
    font-size: var(--nt-font-size-caption);
    margin-left: 8px;
    &--success { color: var(--nt-success); }
    &--error { color: var(--nt-danger); }
  }

  .ai-hint {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 6px;
    a {
      color: var(--el-color-primary);
      text-decoration: none;
      &:hover { text-decoration: underline; }
    }
  }

  .api-key-status {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    padding: 8px 12px;
    border-radius: 6px;
    width: fit-content;
    &.configured {
      color: var(--nt-success);
      background-color: rgba(103, 194, 58, 0.1);
    }
    &.missing {
      color: var(--el-color-warning);
      background-color: rgba(230, 162, 60, 0.1);
    }
  }
}
</style>
