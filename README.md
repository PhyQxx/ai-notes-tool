# AI笔记工具 (AI Notes)

> 一个集成AI智能体的现代化Web笔记工具，支持个人使用和小团队协作。

## 📋 项目简介

AI笔记工具是一个基于Vue 3 + Spring Boot开发的智能化笔记应用，集成了DeepSeek、GLM等国内主流AI服务，为用户提供智能化的笔记管理、内容生成和知识整理能力。

### 核心特性

- ✅ **智能编辑**：Markdown + 富文本双模式编辑
- 🤖 **AI辅助**：支持AI对话、内容生成、摘要优化
- 📁 **分类管理**：文件夹分类 + 标签管理
- 🔍 **强大搜索**：全文搜索 + 高级筛选
- 📜 **版本管理**：自动保存历史版本
- 📎 **附件支持**：图片、文档上传
- 📤 **多格式导出**：Markdown、PDF、Word
- 👥 **团队协作**：空间管理、权限控制、评论互动

## 🚀 技术栈

### 前端
- **框架**：Vue 3 + TypeScript
- **UI组件**：Element Plus
- **状态管理**：Pinia
- **编辑器**：Vditor (Markdown) + Tiptap (富文本)
- **构建工具**：Vite

### 后端
- **框架**：Spring Boot 3.x + Java 17
- **数据库**：MySQL 8.0
- **缓存**：Redis 7.0
- **文件存储**：MinIO
- **ORM**：MyBatis Plus

### AI服务
- **DeepSeek**：https://platform.deepseek.com/
- **智谱GLM**：https://open.bigmodel.cn/

## 📚 文档

- [产品需求文档 (PRD)](./docs/PRD.md) - 详细的功能需求和验收标准
- [技术架构文档 (ARCHITECTURE)](./docs/ARCHITECTURE.md) - 技术选型和系统设计
- [开发计划 (DEVELOPMENT_PLAN)](./docs/DEVELOPMENT_PLAN.md) - 开发阶段和时间安排

## 📅 项目规划

| 阶段 | 时间 | 主要内容 |
|------|------|----------|
| MVP版本 | Week 1-4 | 核心笔记功能（CRUD、搜索、分类） |
| AI集成 | Week 5-7 | AI对话、生成、摘要、优化 |
| 高级功能 | Week 8-11 | 富文本编辑、版本管理、附件、导出 |
| 团队协作 | Week 12-14 | 空间管理、权限、评论 |
| 优化上线 | Week 15-16 | 性能优化、部署上线 |

预计上线时间：**2026年7月15日**

## 🎯 功能演示

### 笔记管理
- 创建、编辑、删除笔记
- 文件夹分类管理
- 收藏、置顶功能
- 全文搜索

### AI智能体
- 与AI助手实时对话
- AI辅助生成内容
- AI智能摘要
- AI优化润色

### 版本管理
- 自动保存历史版本
- 版本对比
- 版本恢复

### 团队协作
- 创建团队空间
- 邀请成员
- 权限管理
- 评论互动

## 🛠️ 快速开始

### 环境要求

- Node.js >= 18
- Java >= 17
- MySQL >= 8.0
- Redis >= 7.0
- MinIO >= RELEASE.2023

### 安装运行

```bash
# 克隆项目
git clone https://github.com/yourusername/ai-notes-tool.git
cd ai-notes-tool

# 启动后端
cd backend
mvn spring-boot:run

# 启动前端
cd frontend
npm install
npm run dev
```

### Docker部署

```bash
# 使用Docker Compose一键启动
docker-compose up -d
```

## 📊 项目进度

- [x] 需求分析
- [x] 技术选型
- [x] PRD文档编写
- [x] 技术架构设计
- [x] 开发计划制定
- [ ] 项目初始化
- [ ] 用户认证模块
- [ ] 笔记CRUD功能
- [ ] AI集成
- [ ] 高级功能开发
- [ ] 团队协作功能
- [ ] 上线部署

## 👥 团队

| 角色 | 负责人 |
|------|--------|
| 项目总监 (PD) | 裴浩宇 |
| 产品经理 (PM) | 待定 |
| 设计师 (Des) | 待定 |
| 开发工程师 (Dev) | 待定 |
| 测试工程师 (QA) | 待定 |
| 运维工程师 (Ops) | 待定 |

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！

---

**项目创建时间**：2026-04-01
**当前版本**：v1.0-alpha
**项目状态**：规划阶段
