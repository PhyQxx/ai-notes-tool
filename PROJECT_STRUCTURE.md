# AI Notes Tool - Project Structure

## Overview
AI笔记工具项目，包含前后端两个子项目。

## Directory Structure

```
ai-notes-tool/
├── frontend/                    # Vue 3 + TypeScript 前端项目
│   ├── node_modules/           # 前端依赖
│   ├── src/                    # 源代码
│   ├── package.json            # 前端依赖配置
│   └── vite.config.ts          # Vite 配置
│
├── backend/                     # Spring Boot 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/com/ainotes/
│   │       │   ├── AiNotesApplication.java    # 主启动类
│   │       │   ├── config/                    # 配置类
│   │       │   │   ├── SecurityConfig.java    # 安全配置
│   │       │   │   ├── MybatisPlusConfig.java # MyBatis Plus配置
│   │       │   │   ├── RedisConfig.java       # Redis配置
│   │       │   │   └── MinioConfig.java       # MinIO配置
│   │       │   ├── controller/                # 控制器
│   │       │   ├── service/                   # 服务层
│   │       │   │   └── impl/                  # 服务实现
│   │       │   ├── mapper/                    # MyBatis Mapper
│   │       │   ├── entity/                    # 实体类
│   │       │   ├── dto/                       # 数据传输对象
│   │       │   │   ├── request/               # 请求DTO
│   │       │   │   └── response/              # 响应DTO
│   │       │   ├── vo/                        # 视图对象
│   │       │   ├── common/                    # 公共模块
│   │       │   │   ├── exception/             # 异常处理
│   │       │   │   ├── enums/                 # 枚举类
│   │       │   │   ├── result/                # 统一响应结果
│   │       │   │   └── constants/             # 常量定义
│   │       │   ├── ai/                        # AI相关模块
│   │       │   └── util/                      # 工具类
│   │       └── resources/
│   │           ├── application.yml            # 主配置文件
│   │           ├── application-dev.yml        # 开发环境配置
│   │           └── application-prod.yml       # 生产环境配置
│   └── pom.xml                    # Maven依赖配置
│
├── .gitignore                  # Git忽略文件配置
├── .editorconfig               # 编辑器配置
└── PROJECT_STRUCTURE.md        # 项目结构说明
```

## Tech Stack

### Frontend
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **编辑器**: Vditor, Tiptap

### Backend
- **框架**: Spring Boot 3.2.x
- **JDK版本**: Java 17
- **数据库**: MySQL
- **ORM**: MyBatis Plus
- **缓存**: Redis
- **对象存储**: MinIO
- **安全认证**: Spring Security + JWT
- **API文档**: Knife4j (Swagger)
- **工具**: Lombok

## Getting Started

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

## Configuration

- 开发环境: `spring.profiles.active=dev`
- 生产环境: `spring.profiles.active=prod`

## Notes
- 确保已安装 Node.js 18+ 和 Java 17
- 确保已安装 MySQL、Redis 和 MinIO
- 首次运行前需要配置数据库连接信息
