# Docker部署指南

本文档介绍如何使用Docker和Docker Compose部署AI笔记工具。

## 📋 前置要求

- Docker 20.10+
- Docker Compose 2.0+

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/ai-notes-tool.git
cd ai-notes-tool
```

### 2. 配置环境变量

复制环境变量模板并根据需要修改：

```bash
cp .env.example .env
vim .env
```

主要配置项：

```env
# MySQL配置
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=ai_notes

# Redis配置
REDIS_PASSWORD=redis123456

# MinIO配置
MINIO_USER=admin
MINIO_PASSWORD=admin123456
MINIO_BUCKET=ai-notes

# 应用配置
BACKEND_PORT=8080
FRONTEND_PORT=80
```

### 3. 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 4. 访问应用

服务启动成功后，可通过以下地址访问：

- **前端应用**: http://localhost
- **后端API**: http://localhost:8080
- **API文档**: http://localhost:8080/doc.html
- **MinIO控制台**: http://localhost:9001
  - 用户名: admin
  - 密码: admin123456

默认账号：
- 用户名: `admin`
- 密码: `123456`

## 📦 服务说明

### MySQL
- 镜像: `mysql:8.0`
- 端口: 3306
- 数据持久化: `mysql_data` volume
- 健康检查: 每10秒检查一次

### Redis
- 镜像: `redis:7.0`
- 端口: 6379
- 数据持久化: `redis_data` volume
- 启用AOF持久化
- 健康检查: 每10秒检查一次

### MinIO
- 镜像: `minio/minio:latest`
- API端口: 9000
- 控制台端口: 9001
- 数据持久化: `minio_data` volume
- 健康检查: 每10秒检查一次

### Backend
- 基础镜像: `eclipse-temurin:17-jre`
- 端口: 8080
- 依赖服务: MySQL、Redis、MinIO
- 健康检查: 每30秒检查一次
- 启动等待: 60秒

### Frontend
- 基础镜像: `nginx:alpine`
- 端口: 80
- 依赖服务: Backend
- 健康检查: 每30秒检查一次

## 🔧 常用操作

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql

# 查看最近100行日志
docker-compose logs --tail=100 backend
```

### 停止服务

```bash
# 停止所有服务
docker-compose stop

# 停止特定服务
docker-compose stop backend
```

### 启动服务

```bash
# 启动所有服务
docker-compose start

# 启动特定服务
docker-compose start backend
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend
```

### 删除服务

```bash
# 停止并删除所有容器、网络
docker-compose down

# 停止并删除所有容器、网络、卷
docker-compose down -v
```

### 重新构建

```bash
# 重新构建并启动
docker-compose up -d --build

# 重新构建特定服务
docker-compose up -d --build backend
```

### 进入容器

```bash
# 进入后端容器
docker-compose exec backend bash

# 进入MySQL容器
docker-compose exec mysql bash

# 进入Redis容器
docker-compose exec redis redis-cli -a redis123456
```

## 📊 数据持久化

所有数据通过Docker volumes持久化：

- `mysql_data`: MySQL数据
- `redis_data`: Redis数据
- `minio_data`: MinIO数据

### 备份数据

```bash
# 备份MySQL数据
docker-compose exec mysql mysqldump -u root -proot123456 ai_notes > backup.sql

# 备份MinIO数据
docker cp ai-notes-minio:/data ./minio-backup
```

### 恢复数据

```bash
# 恢复MySQL数据
docker-compose exec -T mysql mysql -u root -proot123456 ai_notes < backup.sql

# 恢复MinIO数据
docker cp ./minio-backup ai-notes-minio:/data
```

## 🔍 故障排查

### 服务无法启动

1. 检查端口占用
```bash
netstat -tulpn | grep -E '3306|6379|9000|9001|8080|80'
```

2. 检查Docker日志
```bash
docker-compose logs
```

3. 检查磁盘空间
```bash
df -h
```

### 数据库连接失败

1. 检查MySQL容器是否健康
```bash
docker-compose ps mysql
```

2. 检查数据库初始化
```bash
docker-compose exec mysql mysql -u root -proot123456 -e "SHOW DATABASES;"
```

3. 检查后端配置
```bash
docker-compose exec backend env | grep MYSQL
```

### Redis连接失败

1. 检查Redis容器是否健康
```bash
docker-compose ps redis
```

2. 测试Redis连接
```bash
docker-compose exec redis redis-cli -a redis123456 ping
```

### MinIO上传失败

1. 检查MinIO容器是否健康
```bash
docker-compose ps minio
```

2. 检查bucket是否存在
```bash
docker-compose exec minio mc alias set local http://localhost:9000 admin admin123456
docker-compose exec minio mc ls local/
```

### 前端无法访问后端

1. 检查nginx配置
```bash
docker-compose exec frontend cat /etc/nginx/nginx.conf
```

2. 检查后端服务状态
```bash
docker-compose ps backend
```

3. 检查网络连接
```bash
docker-compose exec frontend ping backend
```

## 📈 性能优化

### 调整JVM参数

编辑`.env`文件：

```env
JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### 调整Nginx工作进程

编辑`docker/nginx.conf`：

```nginx
worker_processes auto;
worker_connections 2048;
```

### 调整MySQL配置

创建`docker/mysql/my.cnf`：

```ini
[mysqld]
max_connections = 200
innodb_buffer_pool_size = 1G
```

## 🔒 安全建议

1. **修改默认密码**: 首次部署后修改所有默认密码
2. **使用HTTPS**: 配置SSL证书启用HTTPS
3. **限制访问**: 配置防火墙规则限制访问端口
4. **定期备份**: 设置定时备份任务
5. **更新镜像**: 定期更新Docker镜像到最新版本
6. **监控日志**: 定期检查应用和系统日志

## 🌐 生产环境部署

### 使用HTTPS

1. 安装certbot获取SSL证书：
```bash
sudo apt-get install certbot
sudo certbot certonly --standalone -d your-domain.com
```

2. 修改nginx配置启用SSL：
```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;
    # ...
}

server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

### 配置域名

修改`.env`文件：

```env
# 修改为你的域名
FRONTEND_PORT=80
```

修改`docker/nginx.conf`：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    # ...
}
```

### 监控和告警

建议集成以下监控工具：

- **Prometheus**: 指标收集
- **Grafana**: 可视化监控
- **AlertManager**: 告警通知

## 📚 更多资源

- [Docker官方文档](https://docs.docker.com/)
- [Docker Compose文档](https://docs.docker.com/compose/)
- [Nginx文档](https://nginx.org/en/docs/)
- [MySQL文档](https://dev.mysql.com/doc/)
- [Redis文档](https://redis.io/documentation)
- [MinIO文档](https://docs.min.io/)

---

**如有问题，请提交[Issue](https://github.com/yourusername/ai-notes-tool/issues)**
