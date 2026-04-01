# AI笔记工具 - 前端项目

> 基于Vue 3 + TypeScript + Element Plus的现代化笔记应用前端

## 📋 项目简介

AI笔记工具前端是一个单页应用(SPA)，提供友好的用户界面和流畅的交互体验，支持Markdown和富文本双模式编辑，集成了DeepSeek和GLM等AI服务。

## 🛠️ 技术栈

### 核心框架
- **Vue 3** - 渐进式JavaScript框架
- **TypeScript** - JavaScript的超集，提供类型安全
- **Vite** - 下一代前端构建工具

### UI框架
- **Element Plus** - Vue 3组件库
- **@element-plus/icons-vue** - Element Plus图标库

### 状态管理与路由
- **Pinia** - Vue官方状态管理库
- **Vue Router** - Vue.js官方路由

### 编辑器
- **Vditor** - 浏览器端的Markdown编辑器
- **Tiptap** - 富文本编辑器

### 工具库
- **Axios** - HTTP客户端
- **Day.js** - 轻量级日期处理库
- **Sass** - CSS预处理器

## 📁 项目结构

```
frontend/
├── public/                 # 静态资源
│   └── favicon.ico
├── src/
│   ├── api/               # API接口定义
│   │   ├── auth.ts
│   │   ├── note.ts
│   │   ├── folder.ts
│   │   ├── ai.ts
│   │   └── index.ts
│   ├── assets/            # 资源文件
│   │   └── logo.svg
│   ├── components/        # 公共组件
│   │   ├── common/        # 通用组件
│   │   ├── editor/        # 编辑器组件
│   │   └── ai/            # AI相关组件
│   ├── layouts/           # 布局组件
│   │   └── DefaultLayout.vue
│   ├── router/            # 路由配置
│   │   └── index.ts
│   ├── stores/            # Pinia状态管理
│   │   ├── auth.ts
│   │   ├── note.ts
│   │   ├── folder.ts
│   │   └── ai.ts
│   ├── styles/            # 全局样式
│   │   ├── global.scss
│   │   ├── variables.scss
│   │   └── element-plus.scss
│   ├── types/             # TypeScript类型定义
│   │   ├── api.ts
│   │   ├── note.ts
│   │   └── index.ts
│   ├── utils/             # 工具函数
│   │   ├── request.ts
│   │   ├── storage.ts
│   │   └── validate.ts
│   ├── views/             # 页面组件
│   │   ├── auth/          # 认证相关页面
│   │   ├── notes/         # 笔记相关页面
│   │   ├── folders/       # 文件夹相关页面
│   │   └── settings/      # 设置页面
│   ├── App.vue            # 根组件
│   └── main.ts            # 入口文件
├── .env.development       # 开发环境配置
├── .env.production        # 生产环境配置
├── index.html             # HTML模板
├── package.json           # 依赖配置
├── tsconfig.json          # TypeScript配置
└── vite.config.ts         # Vite配置
```

## 🚀 快速开始

### 环境要求

- **Node.js**: >= 18.0.0
- **npm**: >= 8.0.0 或 **pnpm**: >= 7.0.0

### 安装依赖

```bash
# 使用npm
npm install

# 或使用pnpm（推荐）
pnpm install
```

### 开发模式

```bash
# 启动开发服务器
npm run dev

# 或使用pnpm
pnpm dev
```

访问 http://localhost:3000

### 生产构建

```bash
# 构建生产版本
npm run build

# 或使用pnpm
pnpm build
```

构建产物将输出到 `dist` 目录。

### 预览生产构建

```bash
# 预览生产构建
npm run preview

# 或使用pnpm
pnpm preview
```

## 🔧 开发指南

### 环境变量

项目支持通过环境变量进行配置，相关文件位于项目根目录：

- `.env.development` - 开发环境配置
- `.env.production` - 生产环境配置

支持的环境变量：

```bash
# API基础URL
VITE_API_BASE_URL=http://localhost:8080/api

# 应用标题
VITE_APP_TITLE=AI笔记工具

# 文件上传大小限制（MB）
VITE_MAX_UPLOAD_SIZE=50
```

### 代码规范

#### 命名规范
- **组件文件**: PascalCase（如：`NoteList.vue`）
- **工具函数文件**: camelCase（如：`request.ts`）
- **TypeScript文件**: camelCase（如：`api.ts`）
- **变量/函数**: camelCase（如：`getUserInfo`）
- **常量**: UPPER_SNAKE_CASE（如：`API_BASE_URL`）
- **组件名**: PascalCase（如：`<NoteList />`）

#### 注释规范
使用JSDoc风格注释：

```typescript
/**
 * 获取用户信息
 * @param userId 用户ID
 * @returns 用户信息对象
 */
async function getUserInfo(userId: number): Promise<UserInfo> {
  // ...
}
```

### 组件开发

#### 创建新组件

1. 在 `src/components` 下创建组件文件
2. 组件使用 `<script setup lang="ts">` 语法
3. 添加必要的注释和类型定义
4. 导出组件供其他模块使用

示例：

```vue
<template>
  <div class="my-component">
    <!-- 组件内容 -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

// Props定义
interface Props {
  title: string;
  count?: number;
}

const props = withDefaults(defineProps<Props>(), {
  count: 0,
});

// Emits定义
const emit = defineEmits<{
  (e: 'update', value: string): void;
  (e: 'delete', id: number): void;
}>();

// 响应式数据
const loading = ref(false);

// 计算属性
const displayTitle = computed(() => {
  return `${props.title} (${props.count})`;
});

// 方法
const handleUpdate = (value: string) => {
  emit('update', value);
};
</script>

<style scoped lang="scss">
.my-component {
  // 样式
}
</style>
```

### API调用

使用Axios进行HTTP请求，配置在 `src/utils/request.ts` 中：

```typescript
import { request } from '@/utils/request';
import type { UserInfo } from '@/types';

export async function getUserInfo(userId: number): Promise<UserInfo> {
  return request.get(`/api/users/${userId}`);
}

export async function updateUser(userId: number, data: Partial<UserInfo>): Promise<UserInfo> {
  return request.put(`/api/users/${userId}`, data);
}
```

### 状态管理

使用Pinia进行状态管理，Store定义在 `src/stores` 目录下：

```typescript
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { getUserInfo as fetchUserInfo } from '@/api/auth';
import type { UserInfo } from '@/types';

export const useAuthStore = defineStore('auth', () => {
  // State
  const userInfo = ref<UserInfo | null>(null);
  const token = ref<string | null>(localStorage.getItem('token'));

  // Getters
  const isLoggedIn = computed(() => !!token.value);

  // Actions
  async function getUserInfo() {
    if (!token.value) return null;
    const data = await fetchUserInfo();
    userInfo.value = data;
    return data;
  }

  function logout() {
    userInfo.value = null;
    token.value = null;
    localStorage.removeItem('token');
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    getUserInfo,
    logout,
  };
});
```

### 路由配置

路由配置在 `src/router/index.ts` 中：

```typescript
import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/notes/NoteList.vue'),
      },
      {
        path: 'notes/:id',
        name: 'NoteDetail',
        component: () => import('@/views/notes/NoteDetail.vue'),
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
```

## 📦 构建部署

### 构建优化

项目配置了以下构建优化：

1. **代码分割**：自动分割第三方依赖和业务代码
2. **Tree Shaking**：自动移除未使用的代码
3. **压缩优化**：生产环境自动压缩代码
4. **懒加载**：路由级别懒加载
5. **打包分析**：生成打包分析报告

### Nginx部署示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/dist;
    index index.html;

    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    # SPA路由支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://backend:8080/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### Docker部署

```dockerfile
FROM node:18-alpine as builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 🧪 测试

```bash
# 运行单元测试（待配置）
npm run test

# 运行端到端测试（待配置）
npm run test:e2e
```

## 📝 开发计划

### 已完成
- [x] 项目初始化
- [x] 基础框架搭建
- [x] 路由配置
- [x] 状态管理
- [x] UI组件库集成
- [x] API请求封装

### 进行中
- [ ] 认证模块开发
- [ ] 笔记CRUD功能
- [ ] 编辑器集成
- [ ] AI对话功能

### 待完成
- [ ] 文件上传功能
- [ ] 搜索功能
- [ ] 导出功能
- [ ] 版本管理功能
- [ ] 团队协作功能

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

MIT License

## 📞 联系方式

- 项目地址: https://github.com/yourusername/ai-notes-tool
- 问题反馈: https://github.com/yourusername/ai-notes-tool/issues

---

**维护团队**: AI Notes Team
**最后更新**: 2026-04-01
