# AI Notes Backend

AI笔记工具后端服务，基于Spring Boot 3.x构建。

## 技术栈

- Spring Boot 3.2.0
- Spring Security 6.x
- MyBatis Plus 3.5.5
- MySQL 8.0
- Redis 6.0+
- MinIO 8.5.7
- JWT (jjwt 0.12.3)
- Lombok
- Knife4j 4.4.0

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- MinIO Server

## 快速开始

### 1. 数据库初始化

执行数据库初始化脚本：

```bash
mysql -u root -p < ~/projects/ai-notes-tool/docs/sql/init.sql
```

### 2. 配置文件

编辑 `src/main/resources/application-dev.yml`，配置数据库、Redis、MinIO连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_notes?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379
      password: # 如果有密码

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket-name: ai-notes-dev
```

### 3. MinIO配置

确保MinIO服务已启动，并创建bucket：

```bash
# 使用MinIO客户端或Web UI创建bucket
# bucket名称: ai-notes-dev
```

### 4. 启动服务

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run
```

或使用IDE直接运行 `AiNotesApplication` 类。

### 5. 访问API文档

服务启动成功后，访问以下地址查看API文档：

- Knife4j UI: http://localhost:8080/api/doc.html

## 项目结构

```
backend/
├── src/main/java/com/ainotes/
│   ├── AiNotesApplication.java          # 启动类
│   ├── common/                          # 公共模块
│   │   ├── enums/                       # 枚举类
│   │   ├── exception/                   # 异常处理
│   │   └── result/                      # 响应结果
│   ├── config/                          # 配置类
│   │   ├── SecurityConfig.java          # 安全配置
│   │   ├── MinioConfig.java             # MinIO配置
│   │   ├── MybatisPlusConfig.java       # MyBatis Plus配置
│   │   ├── MetaObjectHandler.java       # 自动填充处理器
│   │   └── JwtAuthenticationFilter.java # JWT过滤器
│   ├── controller/                      # 控制器层
│   │   ├── AuthController.java          # 认证控制器
│   │   ├── NoteController.java          # 笔记控制器
│   │   ├── FolderController.java        # 文件夹控制器
│   │   └── FileController.java          # 文件控制器
│   ├── service/                         # 服务层
│   │   ├── AuthService.java             # 认证服务
│   │   ├── NoteService.java             # 笔记服务
│   │   ├── FolderService.java           # 文件夹服务
│   │   ├── FileService.java             # 文件服务
│   │   └── impl/                        # 服务实现
│   ├── mapper/                          # 数据访问层
│   │   ├── UserMapper.java
│   │   ├── NoteMapper.java
│   │   ├── FolderMapper.java
│   │   └── AttachmentMapper.java
│   ├── entity/                          # 实体类
│   │   ├── User.java
│   │   ├── Note.java
│   │   ├── Folder.java
│   │   └── Attachment.java
│   ├── dto/                             # 数据传输对象
│   │   ├── request/                     # 请求DTO
│   │   ├── response/                    # 响应DTO
│   │   └── query/                       # 查询DTO
│   └── util/                            # 工具类
│       └── JwtUtil.java                 # JWT工具类
└── src/main/resources/
    ├── application.yml                  # 主配置文件
    └── application-dev.yml              # 开发环境配置
```

## 核心功能

### 1. 用户认证
- 用户注册
- 用户登录
- Token刷新
- 个人信息管理

### 2. 笔记管理
- 创建/编辑/删除笔记
- 笔记列表查询（支持筛选）
- 全文搜索
- 收藏/置顶
- 查看次数统计

### 3. 文件夹管理
- 创建/编辑/删除文件夹
- 文件夹树形结构
- 多级文件夹支持

### 4. 文件上传
- 图片上传（最大10MB）
- 文件上传（最大50MB）
- 文件列表查询
- 文件删除

## API接口

### 认证接口
- POST /api/auth/register - 用户注册
- POST /api/auth/login - 用户登录
- POST /api/auth/refresh - 刷新Token
- GET /api/auth/profile - 获取个人信息
- PUT /api/auth/profile - 更新个人信息

### 笔记接口
- GET /api/notes - 笔记列表
- POST /api/notes - 创建笔记
- GET /api/notes/{id} - 笔记详情
- PUT /api/notes/{id} - 更新笔记
- DELETE /api/notes/{id} - 删除笔记
- GET /api/notes/search - 搜索笔记
- POST /api/notes/{id}/favorite - 收藏切换
- POST /api/notes/{id}/top - 置顶切换

### 文件夹接口
- GET /api/folders - 文件夹列表
- GET /api/folders/tree - 文件夹树
- POST /api/folders - 创建文件夹
- PUT /api/folders/{id} - 更新文件夹
- DELETE /api/folders/{id} - 删除文件夹

### 文件接口
- POST /api/files/upload/image - 上传图片
- POST /api/files/upload/file - 上传文件
- GET /api/files - 文件列表
- DELETE /api/files/{id} - 删除文件

## 配置说明

### JWT配置
```yaml
jwt:
  secret: ai-notes-dev-secret-key-2024  # JWT密钥
  expiration: 7200000                   # Token过期时间（毫秒），2小时
```

### 文件上传配置
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB   # 单文件最大大小
      max-request-size: 50MB # 请求最大大小
```

### MyBatis Plus配置
```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true  # 驼峰命名转换
  global-config:
    db-config:
      logic-delete-field: status        # 逻辑删除字段
      logic-delete-value: 0             # 删除值
      logic-not-delete-value: 1         # 未删除值
```

## 开发规范

### 代码规范
- 所有类添加完整注释
- 使用Lombok简化代码
- Service层使用@Transactional
- Controller层使用@Validated参数校验
- 统一异常处理

### 命名规范
- 包名：com.ainotes.{module}
- 类名：大驼峰命名
- 方法名：小驼峰命名
- 常量：全大写下划线分隔
- 数据库字段：下划线命名

## 测试账号

数据库初始化脚本已创建测试账号：

- 用户名：admin / 密码：123456
- 用户名：testuser / 密码：123456

## 常见问题

### 1. 连接数据库失败
检查 `application-dev.yml` 中的数据库配置是否正确。

### 2. MinIO连接失败
确保MinIO服务已启动，检查endpoint、access-key、secret-key配置。

### 3. Token验证失败
检查JWT密钥配置，确保前后端使用相同的密钥。

## 下一步计划

- [ ] 版本管理模块
- [ ] AI对话模块
- [ ] 团队协作模块
- [ ] 导出功能
- [ ] 单元测试
- [ ] 集成测试

## 联系方式

- 开发团队：AI Notes Team
- 项目地址：~/projects/ai-notes-tool
