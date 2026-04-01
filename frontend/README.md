# AI Notes - 前端项目

基于 Vue 3 + TypeScript + Vite 构建的智能笔记工具前端应用。

## 技术栈

- **框架**: Vue 3.5+ (Composition API + setup语法糖)
- **语言**: TypeScript 5.3+
- **构建工具**: Vite 5.0+
- **UI组件**: Element Plus 2.4+
- **状态管理**: Pinia 2.1+
- **路由**: Vue Router 4.2+
- **HTTP客户端**: Axios 1.6+
- **Markdown编辑器**: Vditor 3.9+
- **样式**: Sass

## 项目结构

```
src/
├── api/                 # API接口
│   ├── auth.ts         # 认证相关API
│   ├── note.ts         # 笔记相关API
│   ├── folder.ts       # 文件夹相关API
│   └── index.ts        # API导出
├── assets/             # 静态资源
├── components/         # 公共组件
│   ├── common/         # 通用组件
│   │   ├── NoteCard.vue          # 笔记卡片
│   │   └── FolderTree.vue        # 文件夹树
│   ├── editor/         # 编辑器组件
│   │   └── MarkdownEditor.vue    # Markdown编辑器
│   └── index.ts        # 组件导出
├── layouts/            # 布局组件
│   └── DefaultLayout.vue   # 主布局
├── router/             # 路由配置
│   └── index.ts        # 路由定义和守卫
├── stores/             # Pinia状态管理
│   ├── auth.ts         # 认证状态
│   ├── note.ts         # 笔记状态
│   └── index.ts        # 状态导出
├── styles/             # 全局样式
│   ├── variables.scss  # CSS变量
│   └── global.scss     # 全局样式
├── types/              # TypeScript类型定义
│   └── index.ts        # 类型定义
├── utils/              # 工具函数
│   ├── request.ts      # Axios封装
│   └── storage.ts      # 本地存储工具
├── views/              # 页面视图
│   ├── auth/           # 认证相关页面
│   │   ├── LoginView.vue       # 登录页
│   │   └── RegisterView.vue    # 注册页
│   ├── notes/          # 笔记相关页面
│   │   ├── NotesView.vue       # 笔记列表
│   │   └── NoteEditView.vue    # 笔记编辑
│   ├── HomeView.vue    # 首页
│   └── SettingsView.vue # 设置页
├── App.vue             # 根组件
└── main.ts             # 入口文件
```

## 功能模块

### 1. 用户认证
- ✅ 用户登录/注册
- ✅ JWT Token认证
- ✅ Token自动刷新
- ✅ 个人信息管理

### 2. 笔记管理
- ✅ 笔记CRUD操作
- ✅ 文件夹分类管理
- ✅ 全文搜索
- ✅ 收藏/置顶笔记
- ✅ 标签管理
- ✅ 分页加载

### 3. 编辑器
- ✅ Markdown编辑器（Vditor）
- ✅ 实时预览
- ✅ 自动保存（防抖3秒）
- ✅ 图片上传支持
- ✅ 工具栏配置

### 4. 布局与交互
- ✅ 响应式设计
- ✅ 侧边栏折叠
- ✅ 亮色/暗色主题切换
- ✅ 路由守卫

## 开发指南

### 安装依赖

```bash
npm install
```

### 开发环境运行

```bash
npm run dev
```

开发服务器将在 http://localhost:3000 启动

### 生产构建

```bash
npm run build
```

构建产物将生成在 `dist` 目录

### 预览生产构建

```bash
npm run preview
```

## 配置说明

### API代理配置

在 `vite.config.ts` 中配置了API代理：

```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

所有以 `/api` 开头的请求将被代理到后端服务（默认 http://localhost:8080）

### 环境变量

根据需要创建 `.env.development` 和 `.env.production` 文件：

```env
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api

# .env.production
VITE_API_BASE_URL=https://api.example.com/api
```

## API接口说明

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/refresh` - 刷新Token
- `GET /api/auth/profile` - 获取个人信息
- `PUT /api/auth/profile` - 更新个人信息

### 笔记接口
- `GET /api/notes` - 获取笔记列表
- `GET /api/notes/:id` - 获取笔记详情
- `POST /api/notes` - 创建笔记
- `PUT /api/notes/:id` - 更新笔记
- `DELETE /api/notes/:id` - 删除笔记
- `GET /api/notes/search` - 搜索笔记
- `POST /api/notes/:id/favorite` - 收藏/取消收藏
- `POST /api/notes/:id/top` - 置顶/取消置顶

### 文件夹接口
- `GET /api/folders` - 获取文件夹列表
- `GET /api/folders/tree` - 获取文件夹树
- `POST /api/folders` - 创建文件夹
- `PUT /api/folders/:id` - 更新文件夹
- `DELETE /api/folders/:id` - 删除文件夹

## 代码规范

- 使用 Vue 3 Composition API + setup语法糖
- TypeScript 严格模式
- 组件命名使用 PascalCase
- 使用 Element Plus 组件库
- 响应式设计
- 清晰的代码注释

## 待完成功能

- [ ] 富文本编辑器集成
- [ ] AI智能体对话功能
- [ ] 版本管理
- [ ] 文件上传和附件管理
- [ ] 导出功能（PDF/Word）
- [ ] 团队协作功能
- [ ] 评论功能
- [ ] 实时协作编辑

## 浏览器支持

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## License

MIT
