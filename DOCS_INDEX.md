# 文档索引

本索引帮助您快速找到所需的文档。

## 📚 项目文档

### 核心文档
- [主 README](./README.md) - 项目概述、快速开始、功能介绍
- [项目结构](./PROJECT_STRUCTURE.md) - 详细的项目目录结构说明
- [部署指南](./DEPLOYMENT.md) - 部署和运维指南
- [优化总结](./OPTIMIZATION_SUMMARY.md) - 最近一次优化的详细报告

### 产品文档
- [产品需求文档 (PRD)](./docs/PRD.md) - 详细的功能需求和验收标准
- [技术架构文档](./docs/ARCHITECTURE.md) - 技术选型和系统设计
- [开发计划]((./docs/DEVELOPMENT_PLAN.md) - 开发阶段和时间安排

## 🎯 前端文档

- [前端 README](./frontend/README.md) - 前端项目完整文档
  - 项目简介
  - 技术栈
  - 项目结构
  - 快速开始
  - 开发指南
  - 构建部署
  - 测试指南
  - 开发计划

- [前端快速开始](./frontend/QUICK_START.md) - 快速启动前端开发环境
- [前端实现总结](./frontend/IMPLEMENTATION.md) - 功能实现说明
- [AI集成总结](./frontend/AI_INTEGRATION_SUMMARY.md) - AI功能集成说明

## 🔧 后端文档

- [后端 README](./backend/README.md) - 后端项目完整文档
  - 项目简介
  - 技术栈
  - 项目结构
  - 快速开始
  - API文档
  - 开发指南
  - 安全配置
  - 数据库配置
  - 测试指南
  - 常见问题
  - 开发计划

- [后端高级功能总结](./backend/ADVANCED_FEATURES_SUMMARY.md) - 高级功能实现说明

## 🐳 Docker文档

- [Docker README](./docker/README.md) - Docker部署完整指南
  - 前置要求
  - 快速开始
  - 服务说明
  - 常用操作
  - 数据持久化
  - 备份恢复
  - 故障排查
  - 性能优化
  - 安全建议
  - 生产环境部署

## 🗄️ 数据库文档

- [数据库初始化脚本](./docs/sql/init.sql) - MySQL数据库初始化脚本
  - 建表语句
  - 索引创建
  - 初始数据
  - 视图、触发器、存储过程

## 🔧 脚本工具

- [部署脚本](./scripts/deploy.sh) - 生产环境部署脚本
- [备份脚本](./scripts/backup.sh) - 数据库备份脚本
- [开发环境脚本](./scripts/dev.sh) - 开发环境启动脚本

## 📝 配置文件

### 环境配置
- [环境变量示例](./.env.example) - 环境变量配置模板
- [前端开发环境配置](./frontend/.env.development)
- [前端生产环境配置](./frontend/.env.production)
- [后端主配置](./backend/src/main/resources/application.yml)
- [后端开发环境配置](./backend/src/main/resources/application-dev.yml)
- [后端生产环境配置](./backend/src/main/resources/application-prod.yml)

### Docker配置
- [Docker Compose配置](./docker/docker-compose.yml)
- [前端Dockerfile](./docker/Dockerfile.frontend)
- [后端Dockerfile](./docker/Dockerfile.backend)
- [Nginx配置](./docker/nginx.conf)

### 构建配置
- [前端Vite配置](./frontend/vite.config.ts)
- [前端TypeScript配置](./frontend/tsconfig.json)
- [前端Package配置](./frontend/package.json)
- [后端Maven配置](./backend/pom.xml)

## 🐛 问题反馈

### GitHub模板
- [Bug报告模板](./.github/ISSUE_TEMPLATE/bug_report.md) - 报告Bug时使用此模板
- [功能请求模板](./.github/ISSUE_TEMPLATE/feature_request.md) - 提交新功能请求时使用此模板

## 📄 法律文档

- [MIT许可证](./LICENSE) - 项目许可证
- [代码规范](./.editorconfig) - 编辑器配置

## 🔗 快速导航

### 新手入门
1. 阅读 [主 README](./README.md) 了解项目概况
2. 按照 [部署指南](./DEPLOYMENT.md) 部署项目
3. 阅读 [前端 README](./frontend/README.md) 和 [后端 README](./backend/README.md) 了解技术细节

### 开发人员
1. 阅读 [项目结构](./PROJECT_STRUCTURE.md) 了解代码组织
2. 阅读 [前端开发指南](./frontend/README.md#开发指南) 和 [后端开发指南](./backend/README.md#开发指南)
3. 查看 [API文档](http://localhost:8080/doc.html) (启动服务后)

### 运维人员
1. 阅读 [部署指南](./DEPLOYMENT.md) 部署应用
2. 阅读 [Docker文档](./docker/README.md) 了解容器化部署
3. 使用 [备份脚本](./scripts/backup.sh) 定期备份数据

### 产品经理
1. 阅读 [产品需求文档](./docs/PRD.md) 了解功能需求
2. 查看 [开发计划](./docs/DEVELOPMENT_PLAN.md) 了解进度安排
3. 查看 [优化总结](./OPTIMIZATION_SUMMARY.md) 了解最新改进

### 测试人员
1. 阅读 [前端测试指南](./frontend/README.md#测试)
2. 阅读 [后端测试指南](./backend/README.md#测试)
3. 使用 [Bug报告模板](./.github/ISSUE_TEMPLATE/bug_report.md) 报告问题

## 📖 相关资源

### 官方文档
- [Vue.js文档](https://vuejs.org/)
- [Spring Boot文档](https://spring.io/projects/spring-boot)
- [Element Plus文档](https://element-plus.org/)
- [MyBatis Plus文档](https://baomidou.com/)
- [Docker文档](https://docs.docker.com/)
- [MySQL文档](https://dev.mysql.com/doc/)
- [Redis文档](https://redis.io/documentation)

### AI服务文档
- [DeepSeek文档](https://platform.deepseek.com/docs)
- [智谱GLM文档](https://open.bigmodel.cn/dev/api)

## 💡 常见问题快速查找

### 开发环境问题
- 前端启动失败 → [前端README - 快速开始](./frontend/README.md#快速开始)
- 后端启动失败 → [后端README - 快速开始](./backend/README.md#快速开始)
- 数据库连接失败 → [后端README - 常见问题](./backend/README.md#常见问题)

### 部署问题
- Docker部署失败 → [Docker README - 故障排查](./docker/README.md#故障排查)
- 服务无法访问 → [部署指南 - 健康检查](./DEPLOYMENT.md#健康检查)
- 数据恢复问题 → [Docker README - 数据恢复](./docker/README.md#数据恢复)

### 功能问题
- 功能需求 → [产品需求文档](./docs/PRD.md)
- API使用 → [后端README - API文档](./backend/README.md#api文档)
- 提交Bug → [Bug报告模板](./.github/ISSUE_TEMPLATE/bug_report.md)

## 📞 获取帮助

如果文档中没有找到答案，可以通过以下方式获取帮助：

- **提交Issue**: https://github.com/yourusername/ai-notes-tool/issues
- **团队邮箱**: team@ainotes.com
- **在线文档**: https://docs.ainotes.com

---

**最后更新**: 2026-04-01
