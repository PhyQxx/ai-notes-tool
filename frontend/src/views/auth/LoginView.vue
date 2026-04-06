<template>
  <div class="login-page">
    <div class="auth-card">
      <!-- Logo -->
      <div class="auth-logo">
        <div class="logo-icon">📝</div>
        <h1>AI Notes</h1>
        <p>智能笔记助手</p>
      </div>

      <!-- Form -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <div class="form-group">
          <label>邮箱</label>
          <div class="input-wrap">
            <span class="input-icon">📧</span>
            <el-input
              v-model="loginForm.email"
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
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              class="form-input"
            />
          </div>
        </div>

        <!-- Remember + Links -->
        <div class="auth-links">
          <label class="checkbox-row">
            <input type="checkbox" v-model="rememberMe" />
            <span>记住我</span>
          </label>
          <router-link to="/register">忘记密码？</router-link>
        </div>

        <button type="button" class="btn-primary" :disabled="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </el-form>

      <div class="mode-switch">
        还没有账号？<router-link to="/register">立即注册</router-link>
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

const loginFormRef = ref<FormInstance>();
const loading = ref(false);
const rememberMe = ref(false);

const loginForm = reactive({
  email: '',
  password: ''
});

const loginRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return;
    loading.value = true;
    try {
      await authStore.login({ email: loginForm.email, password: loginForm.password });
      ElMessage.success('登录成功');
      router.push('/');
    } catch (error: any) {
      ElMessage.error(error.message || '登录失败');
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped lang="scss">
.login-page {
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

.auth-links {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0 8px;
  font-size: 13px;
  color: var(--text-secondary);

  a {
    color: var(--brand-primary);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.checkbox-row {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;

  input[type="checkbox"] {
    width: 16px;
    height: 16px;
    accent-color: var(--brand-primary);
    cursor: pointer;
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
  margin-top: 24px;

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
