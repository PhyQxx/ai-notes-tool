# AI Notes Tool - 部署指南

## 目录结构

```
ai-notes-tool/
├── docker/
│   ├── docker-compose.yml      # Docker Compose 编排文件
│   ├── Dockerfile.backend      # 后端应用 Dockerfile
│   ├── Dockerfile.frontend     # 前端应用 Dockerfile
│   └── nginx.conf              # Nginx 配置文件
├── scripts/
│   ├── deploy.sh               # 生产环境部署脚本
│   ├── dev.sh                  # 开发环境启动脚本
│   └── backup.sh               # 数据库备份脚本
├── .env.example                # 环境变量模板
└── DEPLOYMENT.md               # 部署文档
```

## 快速开始

### 1. 配置环境变量

复制环境变量模板并修改配置：

```bash
cp .env.example .env
vi .env  # 根据实际情况修改配置
```

### 2. 生产环境部署

```bash
cd ~/projects/ai-notes-tool
./scripts/deploy.sh
```

部署脚本会自动完成以下步骤：
- 拉取最新代码
- 构建 Docker 镜像
- 启动所有服务
- 健康检查

### 3. 开发环境启动

如果需要在本地开发，只启动基础设施服务：

```bash
cd ~/projects/ai-notes-tool
./scripts/dev.sh
```

然后分别在本地启动前后端：

```bash
# 启动后端
./mvnw spring-boot:run

# 启动前端（新终端）
npm run dev
```

### 4. 数据备份

执行数据备份：

```bash
cd ~/projects/ai-notes-tool
./scripts/backup.sh
```

备份文件会保存在 `~/projects/ai-notes-tool/backup/` 目录，默认保留 30 天。

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| Frontend | 80 | 前端 Web 页面 |
| Backend | 8080 | 后端 API |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| MinIO | 9000 | 对象存储 API |
| MinIO Console | 9001 | MinIO 管理控制台 |

## Docker Compose 命令

### 查看服务状态
```bash
cd docker
docker-compose ps
```

### 查看日志
```bash
cd docker
docker-compose logs -f [service_name]
```

### 重启服务
```bash
cd docker
docker-compose restart [service_name]
```

### 停止所有服务
```bash
cd docker
docker-compose down
```

### 停止所有服务并删除数据卷
```bash
cd docker
docker-compose down -v
```

## 健康检查

部署完成后，可以通过以下方式检查服务状态：

```bash
# 检查前端
curl http://localhost/health

# 检查后端
curl http://localhost:8080/actuator/health

# 检查数据库
docker exec ai-notes-mysql mysqladmin ping -h localhost -u root -p

# 检查 Redis
docker exec ai-notes-redis redis-cli -a redis123456 ping

# 检查 MinIO
curl http://localhost:9000/minio/health/live
```

## 数据恢复

### MySQL 恢复
```bash
gunzip backup/mysql_backup.sql.gz
docker exec -i ai-notes-mysql mysql -uroot -p ai_notes < backup/mysql_backup.sql
```

### MinIO 恢复
```bash
docker cp backup/minio_data ai-notes-minio:/data
```

### Redis 恢复
```bash
docker cp backup/redis_backup.rdb ai-notes-redis:/data/dump.rdb
docker restart ai-notes-redis
```

## 故障排查

### 容器无法启动
```bash
# 查看容器日志
docker-compose logs [service_name]

# 检查容器状态
docker-compose ps
```

### 数据库连接失败
- 检查 MySQL 容器是否正常运行
- 确认环境变量中的数据库密码配置正确
- 检查网络连接：`docker network inspect ai-notes-network`

### 镜像构建失败
- 检查 `pom.xml` 和 `package.json` 文件是否存在
- 确认 Maven 和 Node.js 版本兼容
- 查看构建日志：`docker-compose build --no-cache`

## 注意事项

1. **生产环境部署前请务必修改默认密码**
2. 定期执行数据备份（建议每日备份）
3. 监控磁盘空间，及时清理 Docker 镜像和旧备份
4. 生产环境建议配置 HTTPS 和防火墙规则
5. 根据实际负载调整 JVM 内存参数（JAVA_OPTS）

## 技术栈

- **容器编排**: Docker Compose
- **后端**: Spring Boot + Java 17
- **前端**: Nginx + 静态文件
- **数据库**: MySQL 8.0
- **缓存**: Redis 7.0
- **对象存储**: MinIO

## 支持

如有问题，请联系运维团队或查看项目文档。
