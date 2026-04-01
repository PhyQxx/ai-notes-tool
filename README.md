<div align="center">

# 📝 AI笔记工具

> 一个集成AI智能体的现代化Web笔记工具，支持个人使用和小团队协作

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen.svg)](https://vuejs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)

**[在线演示](https://demo.ainotes.com)** • **[快速开始](#-快速开始)** • **[API文档](#api文档)** • **[贡献指南](#-贡献指南)**

</div>

---

## 📋 项目简介

AI笔记工具是一个基于Vue 3 + Spring Boot 3.x开发的智能化笔记应用，集成了DeepSeek、智谱GLM等国内主流AI服务，为用户提供智能化的笔记管理、内容生成和知识整理能力。

### ✨ 核心特性

#### 📝 智能编辑
- **Markdown编辑器**: 支持实时预览、语法高亮、代码块
- **富文本编辑器**: 所见即所得的富文本编辑体验
- **双模式切换**: Markdown与富文本无缝切换
- **自动保存**: 防止数据丢失

#### 🤖 AI辅助
- **AI对话**: 与DeepSeek、GLM等AI模型实时对话
- **内容生成**: AI辅助生成文章、摘要、标题
- **智能优化**: AI优化润色文本、改进表达
- **知识问答**: 基于笔记内容的智能问答

#### 📁 分类管理
- **文件夹管理**: 多层级文件夹组织
- **标签系统**: 灵活的标签分类
- **收藏置顶**: 重要笔记快速访问
- **智能排序**: 按时间、名称、访问量排序

#### 🔍 强大搜索
- **全文搜索**: 支持标题、内容、标签搜索
- **高级筛选**: 按日期、类型、文件夹筛选
- **搜索历史**: 快速访问最近搜索

#### 📜 版本管理
- **自动版本**: 每次保存自动创建版本
- **版本对比**: 可视化对比版本差异
- **版本恢复**: 一键恢复到历史版本
- **版本备注**: 为版本添加说明

#### 📎 附件支持
- **图片上传**: 支持JPG、PNG、GIF等格式
- **文档上传**: 支持PDF、Word、Excel等格式
- **视频上传**: 支持MP4等视频格式
- **MinIO存储**: 分布式对象存储

#### 📤 多格式导出
- **Markdown导出**: 导出为.md文件
- **PDF导出**: 导出为PDF文档
- **Word导出**: 导出为.docx文件
- **HTML导出**: 导出为HTML网页

#### 👥 团队协作
- **团队空间**: 创建团队工作空间
- **成员管理**: 邀请成员、设置权限
- **权限控制**: 所有者、管理员、成员、查看者
- **评论互动**: 笔记评论和回复

---

## 🏗️ 技术架构

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                         用户层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   浏览器     │  │  移动端App   │  │  桌面客户端  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         接入层                                │
│                     Nginx (反向代理)                         │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼                               ▼
┌─────────────────────────────┐   ┌─────────────────────────────┐
│        前端应用             │   │        后端服务             │
│  ┌───────────────────────┐  │   │  ┌───────────────────────┐ │
│  │  Vue 3 + TypeScript   │  │   │  │  Spring Boot 3.x     │ │
│  │  Element Plus         │  │   │  │  Spring Security     │ │
│  │  Vite                 │  │   │  │  MyBatis Plus        │ │
│  │  Pinia + Vue Router   │  │   │  │  Redis               │ │
│  └───────────────────────┘  │   │  └───────────────────────┘ │
└─────────────────────────────┘   └─────────────────────────────┘
              │                               │
              └───────────────┬───────────────┘
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         数据层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   MySQL      │  │    Redis     │  │    MinIO     │      │
│  │  (主数据库)   │  │  (缓存)      │  │  (文件存储)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         外部服务                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   DeepSeek   │  │  智谱GLM     │  │   短信/邮件  │      │
│  │  (AI服务)     │  │  (AI服务)     │  │  (通知服务)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

#### 前端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 渐进式JavaScript框架 |
| TypeScript | 5.9 | JavaScript超集 |
| Vite | 8.0 | 下一代前端构建工具 |
| Element Plus | 2.13 | Vue 3组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 5.0 | 路由管理 |
| Axios | 1.14 | HTTP客户端 |
| Vditor | 3.11 | Markdown编辑器 |
| Tiptap | 2.13 | 富文本编辑器 |

#### 后端技术
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.x | 基础应用框架 |
| Java | 17 | 编程语言 |
| Spring Security | 6.x | 安全框架 |
| MyBatis Plus | 3.5 | ORM框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.0 | 缓存和会话存储 |
| MinIO | Latest | 对象存储 |
| HikariCP | 5.x | 数据库连接池 |
| Lombok | Latest | 简化Java代码 |
| Knife4j | 4.x | API文档 |

#### AI服务
| 服务 | 说明 |
|------|------|
| DeepSeek | 深度求索AI服务 |
| 智谱GLM | 智谱AI大模型 |

---

## 🚀 快速开始

### 方式一：Docker部署（推荐）

使用Docker Compose一键启动所有服务：

```bash
# 克隆项目
git clone https://github.com/yourusername/ai-notes-tool.git
cd ai-notes-tool

# 启动所有服务（包括MySQL、Redis、MinIO）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

访问应用：
- 前端地址: http://localhost:3000
- 后端地址: http://localhost:8080
- API文档: http://localhost:8080/doc.html
- MinIO控制台: http://localhost:9000 (minioadmin/minioadmin)

默认账号：
- 用户名: `admin`
- 密码: `123456`

### 方式二：本地开发

#### 前置条件

- Node.js >= 18
- Java >= 17
- MySQL >= 8.0
- Redis >= 7.0
- MinIO >= RELEASE.2023

#### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ai_notes_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入初始化脚本
mysql -u root -p ai_notes_db < docs/sql/init.sql
```

#### 2. 启动后端

```bash
cd backend

# 配置数据库、Redis、MinIO连接信息
vim src/main/resources/application-dev.yml

# 安装依赖并启动
mvn clean install
mvn spring-boot:run
```

后端服务启动在 http://localhost:8080

#### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务启动在 http://localhost:3000

---

## 📚 文档

### 产品文档
- [产品需求文档 (PRD)](./docs/PRD.md) - 详细的功能需求和验收标准
- [技术架构文档](./docs/ARCHITECTURE.md) - 技术选型和系统设计
- [开发计划](./docs/DEVELOPMENT_PLAN.md) - 开发阶段和时间安排

### 开发文档
- [前端开发指南](./frontend/README.md) - 前端项目结构和开发说明
- [后端开发指南](./backend/README.md) - 后端项目结构和API文档
- [部署文档](./DEPLOYMENT.md) - 部署和运维指南

### API文档
启动后端服务后访问：
- **Knife4j文档**: http://localhost:8080/doc.html
- **Swagger JSON**: http://localhost:8080/v3/api-docs

---

## 📊 项目进度

| 阶段 | 状态 | 完成度 | 说明 |
|------|------|--------|------|
| 需求分析 | ✅ 已完成 | 100% | PRD文档已完成 |
| 技术选型 | ✅ 已完成 | 100% | 架构设计已完成 |
| 项目初始化 | ✅ 已完成 | 100% | 前后端项目已搭建 |
| MVP版本 | 🚀 进行中 | 80% | 核心笔记功能开发中 |
| AI集成 | 🚀 进行中 | 60% | DeepSeek和GLM集成中 |
| 高级功能 | ⏳ 待开始 | 0% | 富文本、版本管理、附件、导出 |
| 团队协作 | ⏳ 待开始 | 0% | 空间管理、权限、评论 |
| 优化上线 | ⏳ 待开始 | 0% | 性能优化、部署上线 |

**预计上线时间**: 2026年7月15日

---

## 🎯 功能演示

### 笔记管理
- ✅ 创建、编辑、删除笔记
- ✅ 文件夹分类管理
- ✅ 收藏、置顶功能
- ✅ 全文搜索
- ✅ 标签管理

### AI智能体
- ✅ 与AI助手实时对话
- 🚧 AI辅助生成内容
- 🚧 AI智能摘要
- 🚧 AI优化润色

### 版本管理
- 🚧 自动保存历史版本
- ⏳ 版本对比
- ⏳ 版本恢复

### 团队协作
- ⏳ 创建团队空间
- ⏳ 邀请成员
- ⏳ 权限管理
- ⏳ 评论互动

---

## 🛠️ 开发指南

### 项目结构

```
ai-notes-tool/
├── frontend/              # 前端项目
│   ├── src/
│   │   ├── api/          # API接口
│   │   ├── components/   # 组件
│   │   ├── views/        # 页面
│   │   ├── stores/       # 状态管理
│   │   ├── router/       # 路由
│   │   └── utils/        # 工具函数
│   ├── public/           # 静态资源
│   ├── package.json
│   └── vite.config.ts
├── backend/              # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ainotes/
│   │   │   │   ├── controller/  # 控制器
│   │   │   │   ├── service/     # 服务层
│   │   │   │   ├── mapper/      # 数据访问
│   │   │   │   ├── entity/      # 实体
│   │   │   │   ├── dto/         # 数据传输对象
│   │   │   │   ├── config/      # 配置
│   │   │   │   └── util/        # 工具
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── mapper/      # MyBatis XML
│   │   └── test/                # 测试
│   └── pom.xml
├── docker/                # Docker配置
│   ├── docker-compose.yml
│   ├── Dockerfile.frontend
│   └── Dockerfile.backend
├── docs/                  # 文档
│   ├── sql/
│   │   └── init.sql       # 数据库初始化脚本
│   ├── PRD.md
│   ├── ARCHITECTURE.md
│   └── DEVELOPMENT_PLAN.md
├── scripts/               # 脚本工具
├── .env.example           # 环境变量示例
├── .gitignore
└── README.md
```

### 代码规范

#### 前端规范
- 组件使用PascalCase命名
- 工具函数使用camelCase命名
- 常量使用UPPER_SNAKE_CASE命名
- 使用TypeScript类型定义
- 组件使用`<script setup>`语法

#### 后端规范
- 类名使用PascalCase命名
- 方法名使用camelCase命名
- 常量使用UPPER_SNAKE_CASE命名
- 使用Javadoc注释
- 遵循RESTful API设计规范

### 提交规范

使用[Conventional Commits](https://www.conventionalcommits.org/)规范：

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 重构代码
perf: 性能优化
test: 测试相关
chore: 构建/工具相关
```

示例：
```bash
git commit -m "feat: 添加笔记导出功能"
git commit -m "fix: 修复登录接口token过期问题"
```

---

## 🐛 常见问题

### 1. 前端启动失败

**问题**: `npm install` 时报错

**解决**:
```bash
# 清除缓存
npm cache clean --force
# 删除node_modules
rm -rf node_modules package-lock.json
# 重新安装
npm install
```

### 2. 后端连接数据库失败

**问题**: 启动时报错 `Communications link failure`

**解决**:
1. 检查MySQL服务是否启动
2. 检查`application-dev.yml`中的数据库配置
3. 确保数据库已创建且导入初始化脚本

### 3. MinIO上传文件失败

**问题**: 上传附件时报错

**解决**:
1. 检查MinIO服务是否启动
2. 检查MinIO的bucket是否创建
3. 检查access-key和secret-key配置

### 4. AI服务调用失败

**问题**: AI对话时报错

**解决**:
1. 检查API Key是否配置正确
2. 检查网络连接是否正常
3. 检查API调用额度是否充足

更多问题请查看 [Issue Tracker](https://github.com/yourusername/ai-notes-tool/issues)

---

## 📦 部署指南

### Docker部署

详细的Docker部署指南请查看 [DEPLOYMENT.md](./DEPLOYMENT.md)

### 手动部署

1. **前端部署**:
```bash
cd frontend
npm run build
# 将dist目录部署到Nginx
```

2. **后端部署**:
```bash
cd backend
mvn clean package -DskipTests
java -jar target/ai-notes-backend-1.0.0.jar --spring.profiles.active=prod
```

3. **Nginx配置**:
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. Fork本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建一个Pull Request

### 报告问题

如果发现了bug或有功能建议，请：

1. 检查[Issue Tracker](https://github.com/yourusername/ai-notes-tool/issues)确保问题未被报告
2. 使用提供的[问题模板](.github/ISSUE_TEMPLATE/bug_report.md)创建Issue
3. 提供详细的问题描述和复现步骤

---

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

---

## 👥 团队

| 角色 | 负责人 | 职责 |
|------|--------|------|
| 项目总监 (PD) | 裴浩宇 | 项目统筹、任务分发、进度管控 |
| 产品经理 (PM) | 待定 | 需求分析、PRD输出、验收标准 |
| 设计师 (Des) | 待定 | 原型设计、UI视觉、交互规范 |
| 开发工程师 (Dev) | 待定 | 代码开发、架构实现、接口编写 |
| 测试工程师 (QA) | 待定 | 测试用例、功能测试、Bug提交 |
| 运维工程师 (Ops) | 待定 | 环境部署、版本发布、服务监控 |

---

## 📞 联系方式

- **项目地址**: https://github.com/yourusername/ai-notes-tool
- **问题反馈**: https://github.com/yourusername/ai-notes-tool/issues
- **文档中心**: https://docs.ainotes.com

---

## 🙏 致谢

感谢以下开源项目：

- [Vue.js](https://vuejs.org/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Element Plus](https://element-plus.org/)
- [MyBatis Plus](https://baomidou.com/)
- [Vditor](https://b3log.org/vditor/)
- [Tiptap](https://tiptap.dev/)

---

<div align="center">

**如果这个项目对你有帮助，请给个⭐️支持一下！**

Made with ❤️ by AI Notes Team

[⬆ 回到顶部](#-ai笔记工具)

</div>
