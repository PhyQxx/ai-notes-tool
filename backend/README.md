# AI笔记工具 - 后端项目

> 基于Spring Boot 3.x + Java 17的高性能后端服务

## 📋 项目简介

AI笔记工具后端是一个RESTful API服务，提供用户认证、笔记管理、AI集成、文件存储等核心功能，支持个人使用和小团队协作。

## 🛠️ 技术栈

### 核心框架
- **Spring Boot 3.x** - 基础应用框架
- **Java 17** - 编程语言
- **Spring Security** - 安全框架
- **Spring Data Redis** - Redis集成

### 数据持久层
- **MyBatis Plus** - ORM框架
- **MySQL 8.0** - 关系型数据库
- **Redis 7.0** - 缓存和会话存储
- **HikariCP** - 数据库连接池

### 文件存储
- **MinIO** - 对象存储服务

### 文档和测试
- **Knife4j (Swagger)** - API文档
- **JUnit 5** - 单元测试
- **Lombok** - 简化Java代码

### 工具库
- **Hutool** - Java工具类库
- **Jackson** - JSON处理
- **JWT (java-jwt)** - JSON Web Token

## 📁 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/ainotes/
│   │   │   ├── AiNotesApplication.java    # 应用启动类
│   │   │   ├── config/                    # 配置类
│   │   │   │   ├── SecurityConfig.java    # 安全配置
│   │   │   │   ├── MybatisPlusConfig.java # MyBatis Plus配置
│   │   │   │   ├── RedisConfig.java       # Redis配置
│   │   │   │   ├── MinioConfig.java       # MinIO配置
│   │   │   │   ├── AIConfig.java          # AI服务配置
│   │   │   │   └── JwtAuthenticationFilter.java  # JWT过滤器
│   │   │   ├── controller/                # 控制器
│   │   │   │   ├── auth/                  # 认证控制器
│   │   │   │   ├── note/                  # 笔记控制器
│   │   │   │   ├── folder/                # 文件夹控制器
│   │   │   │   ├── ai/                    # AI控制器
│   │   │   │   └── attachment/            # 附件控制器
│   │   │   ├── service/                   # 服务层
│   │   │   │   ├── auth/                  # 认证服务
│   │   │   │   ├── note/                  # 笔记服务
│   │   │   │   ├── folder/                # 文件夹服务
│   │   │   │   ├── ai/                    # AI服务
│   │   │   │   └── attachment/            # 附件服务
│   │   │   ├── mapper/                    # MyBatis Mapper
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── NoteMapper.java
│   │   │   │   ├── FolderMapper.java
│   │   │   │   └── ...
│   │   │   ├── entity/                    # 实体类
│   │   │   │   ├── User.java
│   │   │   │   ├── Note.java
│   │   │   │   ├── Folder.java
│   │   │   │   └── ...
│   │   │   ├── dto/                       # 数据传输对象
│   │   │   │   ├── request/               # 请求DTO
│   │   │   │   └── response/              # 响应DTO
│   │   │   ├── common/                    # 公共类
│   │   │   │   ├── Result.java            # 统一响应结果
│   │   │   │   ├── ResultCode.java        # 响应码枚举
│   │   │   │   └── PageResult.java        # 分页结果
│   │   │   ├── exception/                 # 异常处理
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── BusinessException.java
│   │   │   ├── util/                      # 工具类
│   │   │   │   ├── JwtUtil.java           # JWT工具
│   │   │   │   ├── MinioUtil.java         # MinIO工具
│   │   │   │   └── RedisUtil.java         # Redis工具
│   │   │   └── security/                  # 安全相关
│   │   │       ├── UserDetailsServiceImpl.java
│   │   │       └── JwtAuthenticationFilter.java
│   │   └── resources/
│   │       ├── application.yml            # 主配置文件
│   │       ├── application-dev.yml        # 开发环境配置
│   │       ├── application-prod.yml       # 生产环境配置
│   │       └── mapper/                    # MyBatis XML映射文件
│   └── test/                              # 测试代码
│       └── java/com/ainotes/
│           └── ...
└── pom.xml                                # Maven配置文件
```

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **MinIO**: RELEASE.2023+

### 安装依赖

```bash
# 进入后端目录
cd backend

# 安装Maven依赖
mvn clean install
```

### 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE IF NOT EXISTS ai_notes_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p ai_notes_db < ../docs/sql/init.sql
```

### 配置文件

修改 `src/main/resources/application-dev.yml` 中的数据库、Redis、MinIO等配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_notes_db?...
    username: your_username
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password

minio:
  endpoint: http://localhost:9000
  access-key: your_access_key
  secret-key: your_secret_key
```

### 启动应用

```bash
# 开发环境启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用IDE启动主类
# com.ainotes.AiNotesApplication
```

应用启动后访问：
- API文档: http://localhost:8080/doc.html
- 健康检查: http://localhost:8080/actuator/health

### 生产部署

```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar target/ai-notes-backend-1.0.0.jar --spring.profiles.active=prod
```

## 📡 API文档

项目集成Knife4j生成API文档，启动应用后访问：

- **文档地址**: http://localhost:8080/doc.html
- **Swagger JSON**: http://localhost:8080/v3/api-docs

### 主要API端点

#### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/me` - 获取当前用户信息

#### 笔记相关
- `GET /api/notes` - 获取笔记列表（分页）
- `GET /api/notes/{id}` - 获取笔记详情
- `POST /api/notes` - 创建笔记
- `PUT /api/notes/{id}` - 更新笔记
- `DELETE /api/notes/{id}` - 删除笔记

#### 文件夹相关
- `GET /api/folders` - 获取文件夹列表
- `POST /api/folders` - 创建文件夹
- `PUT /api/folders/{id}` - 更新文件夹
- `DELETE /api/folders/{id}` - 删除文件夹

#### AI相关
- `POST /api/ai/chat` - AI对话
- `POST /api/ai/generate` - AI生成内容
- `POST /api/ai/summarize` - AI摘要
- `POST /api/ai/optimize` - AI优化润色

#### 附件相关
- `POST /api/attachments/upload` - 上传附件
- `DELETE /api/attachments/{id}` - 删除附件
- `GET /api/attachments/{id}` - 获取附件信息

详细的API文档请访问：http://localhost:8080/doc.html

## 🔧 开发指南

### 代码规范

#### 命名规范
- **类名**: PascalCase（如：`UserService`）
- **方法名**: camelCase（如：`getUserById`）
- **常量**: UPPER_SNAKE_CASE（如：`MAX_RETRY_COUNT`）
- **变量名**: camelCase（如：`userName`）

#### 注释规范
使用Javadoc风格注释：

```java
/**
 * 根据用户ID获取用户信息
 *
 * @param userId 用户ID
 * @return 用户信息对象
 * @throws BusinessException 当用户不存在时抛出
 */
public UserInfo getUserById(Long userId) {
    // ...
}
```

### 分层架构

项目采用经典的分层架构：

1. **Controller层**: 处理HTTP请求，参数校验
2. **Service层**: 业务逻辑处理
3. **Mapper层**: 数据访问
4. **Entity层**: 数据库实体映射
5. **DTO层**: 数据传输对象

示例：

```java
// Controller层
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping("/{id}")
    public Result<NoteResponse> getNote(@PathVariable Long id) {
        NoteResponse note = noteService.getNoteById(id);
        return Result.success(note);
    }
}

// Service层
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteMapper noteMapper;
    private final UserService userService;

    public NoteResponse getNoteById(Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        // 转换为响应DTO
        return convertToResponse(note);
    }
}

// Mapper层
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
    // MyBatis Plus提供基础CRUD方法
    // 可自定义SQL方法
}
```

### 统一响应格式

所有API响应使用统一的格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

定义在 `Result` 类中：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
}
```

### 异常处理

使用 `@ControllerAdvice` 进行全局异常处理：

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getResultCode());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
    }
}
```

### 数据验证

使用 `@Valid` 和 `javax.validation` 进行参数验证：

```java
@Data
public class NoteCreateRequest {

    @NotBlank(message = "笔记标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @NotNull(message = "内容类型不能为空")
    private String contentType;

    private String content;
}

@RestController
public class NoteController {

    @PostMapping
    public Result<NoteResponse> createNote(@Valid @RequestBody NoteCreateRequest request) {
        // 自动验证参数
        NoteResponse note = noteService.createNote(request);
        return Result.success(note);
    }
}
```

### 分页查询

使用MyBatis Plus的分页插件：

```java
// Service层
public PageResult<NoteResponse> getNotes(NoteQueryRequest request) {
    Page<Note> page = new Page<>(request.getPageNum(), request.getPageSize());
    LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<Note>()
            .eq(Note::getUserId, SecurityUtils.getUserId())
            .like(StringUtils.isNotBlank(request.getKeyword()), Note::getTitle, request.getKeyword())
            .orderByDesc(Note::getUpdatedAt);

    Page<Note> result = noteMapper.selectPage(page, wrapper);
    return PageResult.of(result, this::convertToResponse);
}

// Controller层
@GetMapping
public Result<PageResult<NoteResponse>> getNotes(NoteQueryRequest request) {
    PageResult<NoteResponse> notes = noteService.getNotes(request);
    return Result.success(notes);
}
```

## 🔒 安全配置

### JWT认证

项目使用JWT进行用户认证：

1. 用户登录成功后生成JWT Token
2. 客户端请求时在Header中携带Token
3. `JwtAuthenticationFilter` 拦截请求，验证Token
4. 验证通过后设置SecurityContext

### CORS配置

跨域请求配置在 `SecurityConfig` 中：

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/doc.html").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

### 权限控制

使用 `@PreAuthorize` 进行方法级权限控制：

```java
@RestController
@PreAuthorize("hasRole('USER')")
public class NoteController {
    // 需要用户角色才能访问
}

@PreAuthorize("hasRole('ADMIN')")
public void deleteNote(Long id) {
    // 需要管理员角色才能删除
}
```

## 🗄️ 数据库配置

### 连接池配置

使用HikariCP作为连接池：

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-test-query: SELECT 1
```

### MyBatis Plus配置

```yaml
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      logic-delete-field: status
      logic-delete-value: 0
      logic-not-delete-value: 1
```

## 📊 监控和日志

### 日志配置

```yaml
logging:
  level:
    com.ainotes: debug
    com.baomidou.mybatisplus: debug
  file:
    name: logs/ai-notes.log
    max-size: 10MB
    max-history: 30
```

### Actuator监控

启用Spring Boot Actuator进行健康检查和监控：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

## 🧪 测试

### 单元测试

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest

# 运行指定测试方法
mvn test -Dtest=UserServiceTest#testGetUserById
```

### 测试示例

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testGetUserById() {
        User user = userService.getUserById(1L);
        assertNotNull(user);
        assertEquals("admin", user.getUsername());
    }

    @Test
    @Transactional
    void testCreateUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        Long userId = userService.createUser(user);
        assertNotNull(userId);
    }
}
```

## 📝 开发计划

### 已完成
- [x] 项目初始化
- [x] 基础框架搭建
- [x] 安全认证模块
- [x] 用户管理
- [x] 笔记CRUD
- [x] 文件夹管理
- [x] MinIO集成
- [x] Redis缓存

### 进行中
- [ ] AI服务集成
- [ ] 文件上传功能
- [ ] 搜索功能

### 待完成
- [ ] 导出功能
- [ ] 版本管理
- [ ] 团队协作
- [ ] 评论功能
- [ ] 性能优化

## 🚀 性能优化

### 缓存策略
- 使用Redis缓存热点数据
- 使用Spring Cache注解简化缓存操作

### 数据库优化
- 合理使用索引
- 分页查询
- 避免N+1查询

### 接口优化
- 异步处理耗时操作
- 批量操作优化
- 合理使用DTO

## 🐛 常见问题

### 数据库连接失败
检查数据库配置和网络连接，确保MySQL服务正常运行。

### Redis连接失败
检查Redis服务状态和配置，确保Redis服务正常运行。

### MinIO上传失败
检查MinIO服务状态和存储空间，确保bucket配置正确。

### JWT Token过期
Token默认有效期为2小时，过期后需要重新登录。

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
