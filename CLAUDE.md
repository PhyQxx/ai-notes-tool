# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

AI 笔记工具 —— 全栈 Web 应用。Vue 3 + TypeScript 前端，Spring Boot 3 后端，集成 DeepSeek/GLM 的 AI 能力（聊天、内容生成、摘要）。

## 常用命令

### 前端（`frontend/` 目录）
```bash
npm install          # 安装依赖
npm run dev          # 启动开发服务器（端口 3000，代理 /api → localhost:8093）
npm run build        # 生产构建 → dist/
```

### 后端（`backend/` 目录）
```bash
mvn spring-boot:run              # 启动开发服务器（端口 8093）
mvn clean install                # 构建
mvn spring-boot:run -Dspring-boot.run.profiles=prod  # 生产配置运行
```

### Docker
```bash
docker-compose up -d             # 启动所有服务（backend:8083, frontend:8091, redis, minio）
```

## 外部依赖

- **MySQL 8.0**（数据库 `ai_notes`，端口 3306）
- **Redis 7**（端口 6379）
- **MinIO**（端口 9000/9001，文件存储）
- **DeepSeek / GLM API Key**（AI 功能，通过环境变量配置）

数据库初始化脚本：`docs/sql/init.sql`

## 架构要点

### 前后端通信
- 后端 context-path: `/api`，所有 REST 端点以此为前缀
- 前端开发时通过 Vite proxy 转发 `/api` 请求到后端
- 生产环境由 Nginx 反向代理
- API 文档：`/doc.html`（Knife4j/Swagger）

### 认证与安全
- JWT 无状态认证（`Authorization: Bearer` 头），token 存 localStorage
- Spring Security 过滤链：CSRF → Rate Limit → XSS → JWT
- 自定义 CSRF 一次性 token（`/api/csrf/token`）
- BCrypt 密码哈希，AI 输入有 prompt 注入拦截

### 后端分层
`controller/` → `service/impl/` → `mapper/`（MyBatis Plus）
- DTO 分三层：`dto/request/`、`dto/response/`、`dto/query/`
- 实体类使用 Lombok，逻辑删除字段（0=已删除，1=正常）
- 统一响应包装 `Result<T>`（code/message/data）
- 异常处理：`BusinessException` + `GlobalExceptionHandler`

### AI 提供者抽象
- `AIProvider` 接口 → `DeepSeekProvider`、`GLMProvider`
- `AIProviderFactory` 工厂选择
- 流式响应通过 OkHttp SSE 实现

### 前端状态管理
- Pinia stores（Composition API 风格）：auth、note、ai、space、theme、notification
- API 模块按领域拆分（17 个模块），在 `api/` 目录
- HTTP 客户端：`utils/request.ts`（Axios 封装，自动附加 JWT/CSRF）

### 实时协作
- WebSocket（Spring WebSocket + 前端 `websocket.ts`）
- 按笔记 ID 分房间，支持光标广播、内容同步、用户加入/离开通知
- 前端有心跳（30s）和指数退避重连（最多 5 次）

### 国际化
- vue-i18n，中文（zh-CN）和英文（en-US），Element Plus locale 同步切换

### 多格式导出
- 后端支持 PDF（iText）、Word（Apache POI）、Markdown、HTML

## 代码风格

- Java 缩进 4 空格，其余文件 2 空格（见 `.editorconfig`）
- 前端 TypeScript strict 模式
- MyBatis Plus 下划线转驼峰映射

## gstack

使用 `/browse` skill 进行所有 Web 浏览操作，不使用 `mcp__claude-in-chrome__*` 工具。

可用的 gstack skills：
`/office-hours`、`/plan-ceo-review`、`/plan-eng-review`、`/plan-design-review`、`/design-consultation`、`/design-shotgun`、`/design-html`、`/review`、`/ship`、`/land-and-deploy`、`/canary`、`/benchmark`、`/browse`、`/connect-chrome`、`/qa`、`/qa-only`、`/design-review`、`/setup-browser-cookies`、`/setup-deploy`、`/retro`、`/investigate`、`/document-release`、`/codex`、`/cso`、`/autoplan`、`/plan-devex-review`、`/devex-review`、`/careful`、`/freeze`、`/guard`、`/unfreeze`、`/gstack-upgrade`、`/learn`