# 快速开始指南

## 前置要求

- Node.js 18+
- npm 或 yarn

## 安装依赖

```bash
cd ~/projects/ai-notes-tool/frontend
npm install
```

## 开发模式

启动开发服务器：

```bash
npm run dev
```

服务器将在 http://localhost:3000 启动

## 生产构建

构建生产版本：

```bash
npm run build
```

构建产物将在 `dist` 目录中

## 预览生产版本

```bash
npm run preview
```

## 项目依赖

已安装的主要依赖：

- vue: ^3.5.30
- element-plus: ^2.13.6
- @element-plus/icons-vue: ^2.3.2
- pinia: ^3.0.4
- vue-router: ^5.0.4
- axios: ^1.14.0
- vditor: ^3.11.2
- dayjs: ^1.11.20
- sass: ^1.98.0

## 已完成的功能模块

✅ 用户认证（登录/注册）
✅ 笔记管理（CRUD）
✅ 文件夹管理
✅ 笔记搜索
✅ Markdown编辑器
✅ 标签管理
✅ 收藏/置顶
✅ 响应式设计
✅ 主题切换

## 后端API配置

开发环境默认代理到：http://localhost:8080/api

如需修改，请编辑 `vite.config.ts`：

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

## 注意事项

1. 确保后端服务已启动（默认端口8080）
2. 首次运行需要注册账号
3. Token存储在LocalStorage中
4. 笔记编辑支持自动保存（3秒防抖）

## 问题排查

如果遇到问题：

1. 检查Node.js版本：`node -v`
2. 清除缓存：`rm -rf node_modules package-lock.json`
3. 重新安装：`npm install`
4. 查看错误日志

## 技术支持

查看详细文档：
- README.md - 完整项目文档
- IMPLEMENTATION.md - 实现报告
