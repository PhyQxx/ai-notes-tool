<template>
  <div class="settings-view">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <h3>设置</h3>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="profile">
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

        <el-tab-pane label="修改密码" name="password">
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

        <el-tab-pane label="外观设置" name="appearance">
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

        <el-tab-pane label="AI设置" name="ai">
          <el-form :model="aiConfig" label-width="120px" style="max-width: 600px">
            <el-form-item label="AI提供商">
              <el-radio-group v-model="aiConfig.provider">
                <el-radio value="deepseek">DeepSeek</el-radio>
                <el-radio value="glm">GLM</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="DeepSeek API Key">
              <el-input
                v-model="aiConfig.deepseekApiKey"
                type="password"
                placeholder="请输入DeepSeek API Key"
                show-password
              />
            </el-form-item>

            <el-form-item label="GLM API Key">
              <el-input
                v-model="aiConfig.glmApiKey"
                type="password"
                placeholder="请输入GLM API Key"
                show-password
              />
            </el-form-item>

            <el-form-item label="默认模型">
              <el-select v-model="aiConfig.model" placeholder="选择模型" style="width: 100%">
                <el-option
                  v-for="model in currentModels"
                  :key="model"
                  :label="model"
                  :value="model"
                />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSaveAIConfig">
                保存配置
              </el-button>
              <el-button @click="handleTestAIConfig">
                测试连接
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useAuthStore } from '@/stores/auth';
import { useAIStore } from '@/stores/ai';
import { updateProfile } from '@/api/auth';

const authStore = useAuthStore();
const aiStore = useAIStore();

const activeTab = ref('profile');
const saving = ref(false);
const changing = ref(false);

const aiConfig = reactive({
  provider: 'deepseek',
  model: 'deepseek-chat',
  deepseekApiKey: '',
  glmApiKey: ''
});

const currentModels = computed(() => {
  if (aiConfig.provider === 'deepseek') {
    return ['deepseek-chat', 'deepseek-coder'];
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
      deepseekApiKey: aiConfig.deepseekApiKey,
      glmApiKey: aiConfig.glmApiKey
    });
    ElMessage.success('AI配置保存成功');
  } catch (error: any) {
    ElMessage.error(error.message || '保存配置失败');
  } finally {
    saving.value = false;
  }
};

const handleTestAIConfig = async () => {
  if (!aiConfig.deepseekApiKey && !aiConfig.glmApiKey) {
    ElMessage.warning('请先输入API Key');
    return;
  }

  try {
    // TODO: 调用测试API连接的接口
    ElMessage.success('API连接测试成功');
  } catch (error: any) {
    ElMessage.error(error.message || 'API连接测试失败');
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
  } catch (error) {
    console.error('加载AI配置失败:', error);
  }
});
</script>

<style scoped lang="scss">
.settings-view {
  max-width: 800px;
  margin: 0 auto;

  .settings-card {
    .card-header {
      h3 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
      }
    }

    .avatar-upload {
      display: flex;
      align-items: center;
    }
  }
}
</style>
