# Lemomate 应用Docker部署指南

本文档提供了使用Docker将Lemomate应用部署到VPS的详细步骤。

## 部署前准备

1. 确保您的VPS（IP: 104.233.196.100）已安装以下软件：
   - Docker
   - Docker Compose

2. 确保域名 `meet.lemomate.com` 已正确解析到IP地址 `104.233.196.100`

## 部署步骤

### 1. 将项目文件上传到VPS

```bash
# 使用scp或其他方式将项目文件上传到VPS
scp -r lemomate_au/ user@104.233.196.100:/path/to/deploy/
```

### 2. 执行部署脚本

```bash
# 进入项目目录
cd /path/to/deploy/lemomate_au

# 给部署脚本添加执行权限
chmod +x deploy.sh

# 执行部署脚本
./deploy.sh
```

部署脚本将自动执行以下操作：
- 安装必要的软件（Docker、Docker Compose、Certbot）
- 获取SSL证书
- 配置SSL证书
- 启动Docker容器

### 3. 验证部署

部署完成后，您可以通过以下URL访问应用：
- 前端应用：https://meet.lemomate.com:8443
- 后端API：https://meet.lemomate.com:8443/api

**注意**：应用使用8087端口(HTTP)和8443端口(HTTPS)，确保这些端口未被其他服务（如Jitsi）占用

## 部署架构

本部署方案包含三个主要服务：

1. **MySQL数据库**
   - 容器名称：lemomate-mysql
   - 端口：3306
   - 数据持久化：使用Docker卷存储

2. **Spring Boot后端**
   - 容器名称：lemomate-backend
   - 端口：8085
   - 依赖：MySQL数据库

3. **Vue.js前端 + Nginx**
   - 容器名称：lemomate-frontend
   - 端口：8087 (HTTP), 8443 (HTTPS)
   - 功能：提供前端静态文件，反向代理后端API

## 文件说明

- `docker-compose.yml`：定义服务、网络和卷
- `Dockerfile-backend`：构建Spring Boot后端镜像
- `frontend/Dockerfile-frontend`：构建Vue.js前端镜像
- `frontend/nginx.conf`：Nginx配置文件
- `deploy.sh`：一键部署脚本

## 常见问题

### 如何查看日志？

```bash
# 查看所有容器日志
docker-compose logs

# 查看特定服务日志
docker-compose logs backend
docker-compose logs frontend
docker-compose logs mysql
```

### 如何重启服务？

```bash
# 重启所有服务
docker-compose restart

# 重启特定服务
docker-compose restart backend
```

### SSL证书更新

SSL证书通常有效期为90天，需要定期更新：

```bash
certbot renew
cp /etc/letsencrypt/live/meet.lemomate.com/fullchain.pem ssl/cert.pem
cp /etc/letsencrypt/live/meet.lemomate.com/privkey.pem ssl/key.pem
docker-compose restart frontend
```

## 安全注意事项

1. 生产环境中应修改默认的数据库密码
2. 考虑配置防火墙，只开放必要的端口（80、443）
3. 定期更新Docker镜像和系统软件

## 支持与帮助

如有任何部署问题，请联系系统管理员或开发团队。