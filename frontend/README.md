# AI笔记工具 - 前端项目

## 项目简介

这是一个现代化的AI笔记工具前端项目，使用Vue 3 + TypeScript + Element Plus构建。

## 技术栈

- **框架**: Vue 3.5.30
- **语言**: TypeScript 5.9.3
- **UI组件库**: Element Plus 2.13.6
- **状态管理**: Pinia 3.0.4
- **路由**: Vue Router 5.0.4
- **HTTP客户端**: Axios 1.14.0
- **Markdown编辑器**: Vditor 3.11.2
- **富文本编辑器**: Tiptap 2.13.2
- **构建工具**: Vite 8.0.1
- **CSS预处理**: Sass 1.98.0

## 功能特性

### 已实现功能

1. **用户认证**
   - 用户注册/登录
   - JWT Token认证
   - 个人信息管理

2. **笔记管理**
   - 笔记CRUD操作
   - 文件夹分类管理
   - 标签管理
   - 收藏/置顶功能

3. **编辑器**
   - Markdown编辑器（Vditor）
   - 富文本编辑器（Tiptap）
   - 编辑器模式切换
   - 实时自动保存

4. **AI智能体**
   - AI对话功能
   - AI内容生成
   - 多AI提供商支持（DeepSeek、GLM）

### 新增高级功能（本次实现）

1. **版本管理**
   - 版本历史查看
   - 手动创建版本快照
   - 版本对比功能
   - 版本恢复功能
   - 版本删除功能

2. **导出功能**
   - 导出为Markdown (.md)
   - 导出为PDF (.pdf)
   - 导出为Word (.docx)

3. **搜索增强**
   - 关键词搜索
   - 文件夹筛选
   - 时间筛选（今天/本周/本月）
   - 排序功能（更新时间/创建时间/标题）
   - 搜索历史记录
   - 搜索结果高亮

4. **富文本编辑器**
   - 完整的工具栏
   - 文本格式（加粗、斜体、下划线、删除线）
   - 标题（H1、H2、H3）
   - 列表（有序、无序）
   - 对齐方式（左、中、右）
   - 插入图片/链接/水平线
   - 撤销/重做
   - 图片上传支持

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API接口
│   │   ├── index.ts      # API导出
│   │   ├── auth.ts       # 认证API
│   │   ├── note.ts       # 笔记API
│   │   ├── folder.ts     # 文件夹API
│   │   ├── ai.ts         # AI API
│   │   ├── version.ts    # 版本管理API
│   │   └── export.ts     # 导出API
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   │   ├── ai/           # AI组件
│   │   ├── common/       # 通用组件
│   │   │   ├── NoteCard.vue
│   │   │   ├── FolderTree.vue
│   │   │   └── MarkdownRenderer.vue
│   │   ├── editor/       # 编辑器组件
│   │   │   ├── MarkdownEditor.vue
│   │   │   ├── RichTextEditor.vue
│   │   │   ├── VersionPanel.vue
│   │   │   ├── VersionCompare.vue
│   │   │   └── ExportMenu.vue
│   │   └── index.ts
│   ├── layouts/          # 布局组件
│   ├── router/           # 路由配置
│   ├── stores/           # 状态管理
│   │   ├── index.ts
│   │   ├── auth.ts
│   │   ├── note.ts
│   │   └── ai.ts
│   ├── types/            # TypeScript类型定义
│   │   └── index.ts
│   ├── utils/            # 工具函数
│   │   ├── request.ts    # HTTP请求封装
│   │   └── storage.ts    # 本地存储封装
│   ├── views/            # 页面组件
│   │   ├── HomeView.vue
│   │   ├── SettingsView.vue
│   │   ├── auth/
│   │   ├── notes/
│   │   └── ai/
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── package.json          # 依赖配置
├── vite.config.ts        # Vite配置
├── tsconfig.json         # TypeScript配置
└── index.html            # HTML模板
```

## 安装依赖

```bash
cd frontend
npm install
```

## 开发运行

```bash
npm run dev
```

项目将在 `http://localhost:5173` 启动。

## 生产构建

```bash
npm run build
```

构建产物将输出到 `dist/` 目录。

## 代码规范

- 使用 Vue 3 Composition API + setup语法糖
- TypeScript严格模式
- ESLint代码检查（待配置）
- Prettier代码格式化（待配置）

## API接口说明

所有API请求都经过统一的HTTP客户端封装，位于 `src/utils/request.ts`。

### 基础URL

开发环境：`http://localhost:8080/api`

生产环境：根据部署配置

### 请求格式

所有请求都会自动添加JWT Token到请求头：

```typescript
{
  headers: {
    'Authorization': 'Bearer <token>'
  }
}
```

### 响应格式

```typescript
{
  code: number,        // 状态码，200表示成功
  message: string,     // 响应消息
  data: T,            // 响应数据
  timestamp: number    // 时间戳
}
```

## 环境变量

在项目根目录创建 `.env` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## 注意事项

1. **Tiptap安装**
   - 新增的富文本编辑器依赖Tiptap，请确保正确安装：
   ```bash
   npm install @tiptap/vue-3 @tiptap/starter-kit @tiptap/extension-underline @tiptap/extension-text-align @tiptap/extension-image @tiptap/extension-link @tiptap/extension-placeholder
   ```

2. **Markdown编辑器**
   - 使用Vditor作为Markdown编辑器，已内置在项目中

3. **图片上传**
   - 图片上传功能需要后端API支持
   - 上传接口：`POST /upload/image`
   - 返回格式：`{ url: string }`

4. **导出功能**
   - 导出功能需要后端API支持
   - 返回类型为Blob

## 待优化项

- [ ] 添加ESLint和Prettier配置
- [ ] 添加单元测试
- [ ] 添加E2E测试
- [ ] 性能优化
- [ ] PWA支持
- [ ] 多语言支持
- [ ] 主题定制

## 联系方式

如有问题，请联系开发团队。
