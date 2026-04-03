<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <div class="logo-icon">
            <el-icon :size="28"><EditPen /></el-icon>
          </div>
          <h2>AI Notes</h2>
          <p>智能笔记工具</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="0"
        size="large"
      >
        <el-form-item prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="邮箱"
            prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="footer-links">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { EditPen } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const loginFormRef = ref<FormInstance>();
const loading = ref(false);

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
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return;

    loading.value = true;
    try {
      await authStore.login({
        email: loginForm.email,
        password: loginForm.password
      });
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
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--nt-primary) 0%, var(--nt-primary-dark) 100%);
  position: relative;
  overflow: hidden;

  &::before,
  &::after {
    content: '';
    position: absolute;
    border-radius: 50%;
    opacity: 0.15;
    filter: blur(80px);
    animation: float 8s ease-in-out infinite;
  }

  &::before {
    width: 400px;
    height: 400px;
    background: var(--nt-primary-light);
    top: -100px;
    right: -100px;
    animation-delay: 0s;
  }

  &::after {
    width: 300px;
    height: 300px;
    background: var(--nt-primary-lighter);
    bottom: -80px;
    left: -80px;
    animation-delay: -4s;
  }

  .login-card {
    width: 400px;
    border-radius: var(--nt-radius-xl);
    box-shadow: var(--nt-shadow-xl);
    animation: cardEnter 0.6s ease-out;
    position: relative;
    z-index: 1;

    :deep(.el-card__body) {
      padding: 24px 32px 32px;
    }

    .card-header {
      text-align: center;

      .logo-icon {
        width: 56px;
        height: 56px;
        margin: 0 auto 16px;
        border-radius: 16px;
        background: linear-gradient(135deg, var(--nt-primary) 0%, var(--nt-primary-dark) 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 28px;
      }

      h2 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 700;
        color: var(--nt-text-primary);
      }

      p {
        margin: 0;
        font-size: var(--nt-font-size-body);
        color: var(--nt-text-tertiary);
      }
    }

    .el-form {
      margin-top: var(--nt-spacing-lg);

      .el-form-item {
        margin-bottom: var(--nt-spacing-lg);
      }
    }

    .footer-links {
      text-align: center;
      margin-top: var(--nt-spacing-md);

      a {
        color: var(--nt-primary);
        text-decoration: none;
        font-size: var(--nt-font-size-body);

        &:hover {
          text-decoration: underline;
        }
      }
    }
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

@keyframes cardEnter {
  from { opacity: 0; transform: translateY(24px) scale(0.96); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
</style>
