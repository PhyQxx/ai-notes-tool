<template>
  <div class="register-page">
    <div class="auth-card">
      <!-- Logo -->
      <div class="auth-logo">
        <div class="logo-icon">📝</div>
        <h1>注册账号</h1>
        <p>创建您的 AI Notes 账号</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        size="large"
        @keyup.enter="handleRegister"
      >
        <div class="form-group">
          <label>用户名</label>
          <div class="input-wrap">
            <span class="input-icon">👤</span>
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              clearable
              class="form-input"
            />
          </div>
        </div>

        <div class="form-group">
          <label>邮箱</label>
          <div class="input-wrap">
            <span class="input-icon">📧</span>
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              clearable
              class="form-input"
            />
          </div>
        </div>

        <div class="form-group">
          <label>密码</label>
          <div class="input-wrap">
            <span class="input-icon">🔒</span>
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              class="form-input"
            />
          </div>
        </div>

        <div class="form-group">
          <label>确认密码</label>
          <div class="input-wrap">
            <span class="input-icon">🔒</span>
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              show-password
              class="form-input"
            />
          </div>
        </div>

        <label class="checkbox-row">
          <input type="checkbox" v-model="agreePolicy" />
          <span>我已阅读并同意</span>
          <a href="#" @click.prevent>《服务协议》</a>
          <span>和</span>
          <a href="#" @click.prevent>《隐私政策》</a>
        </label>

        <button
          type="button"
          class="btn-primary"
          :disabled="loading || !agreePolicy"
          @click="handleRegister"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </el-form>

      <div class="mode-switch">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const registerFormRef = ref<FormInstance>();
const loading = ref(false);
const agreePolicy = ref(false);

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});

const validatePassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'));
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'));
  } else if (!/[A-Za-z]/.test(value) || !/[0-9]/.test(value)) {
    callback(new Error('密码必须包含字母和数字'));
  } else {
    callback();
  }
};

const validateConfirmPassword = (_rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'));
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return;
    loading.value = true;
    try {
      if (!agreePolicy.value) {
        ElMessage.warning('请先阅读并同意服务协议和隐私政策');
        return;
      }
      await authStore.register({
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password
      });
      ElMessage.success('注册成功');
      router.push('/');
    } catch (error: any) {
      if (!error.response) {
        ElMessage.error('网络错误，请稍后重试');
      }
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F0F3FF 0%, #E8ECFF 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    width: 500px;
    height: 500px;
    background: rgba(91, 127, 255, 0.07);
    border-radius: 50%;
    top: -150px;
    right: -80px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 350px;
    height: 350px;
    background: rgba(91, 127, 255, 0.05);
    border-radius: 50%;
    bottom: -80px;
    left: -40px;
  }
}

.auth-card {
  background: var(--bg-white);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-md);
  padding: 48px 40px;
  width: 420px;
  z-index: 1;
  position: relative;
}

.auth-logo {
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    font-size: 40px;
    margin-bottom: 8px;
  }

  h1 {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-strong);
    margin-bottom: 4px;
  }

  p {
    font-size: 14px;
    color: var(--text-secondary);
  }
}

.form-group {
  margin-bottom: 20px;

  label {
    display: block;
    font-size: 13px;
    font-weight: 500;
    color: var(--text-secondary);
    margin-bottom: 8px;
  }
}

.input-wrap {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  color: var(--text-tertiary);
  pointer-events: none;
  z-index: 1;
}

.form-input {
  width: 100%;

  :deep(.el-input__wrapper) {
    padding: 0 14px 0 40px;
    border-radius: var(--radius-md);
    box-shadow: 0 0 0 1.5px var(--border) !important;
    border: none;
    height: 46px;
    transition: border-color 0.2s, box-shadow 0.2s;

    &:hover {
      box-shadow: 0 0 0 1.5px var(--brand-primary) !important;
    }

    &.is-focus {
      box-shadow: 0 0 0 2px #5B7FFF, 0 0 0 4px rgba(91, 127, 255, 0.12) !important;
    }
  }

  :deep(.el-input__inner) {
    font-size: 15px;
    color: var(--text-primary);

    &::placeholder {
      color: var(--text-tertiary) !important;
    }
  }
}

.checkbox-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin: 16px 0 12px;
  font-size: 13px;
  color: var(--text-secondary);

  input[type="checkbox"] {
    width: 16px;
    height: 16px;
    accent-color: var(--brand-primary);
    cursor: pointer;
  }

  a {
    color: var(--brand-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.btn-primary {
  width: 100%;
  height: 46px;
  background: var(--brand-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 20px;

  &:hover {
    background: var(--brand-primary-light-1);
  }

  &:active {
    background: var(--brand-primary-dark-1);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.mode-switch {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: var(--text-tertiary);

  a {
    color: var(--brand-primary);
    cursor: pointer;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
