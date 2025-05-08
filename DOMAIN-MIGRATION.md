# Lemomate 域名迁移指南

## 背景

由于 `meet.lemomate.com` 已被用于部署 Jitsi 服务，Lemomate 应用需要迁移到新域名 `schedulemeet.lemomate.com`。本文档提供了域名迁移的步骤和解决方案。

## 迁移步骤

### 1. 域名解析设置

确保 `schedulemeet.lemomate.com` 已正确解析到服务器 IP 地址 `104.233.196.100`。

### 2. 停止现有容器

```bash
# 停止并移除现有容器
docker-compose down
```

### 3. 获取新域名的 SSL 证书

```bash
# 停止可能占用 80 端口的服务
systemctl stop nginx  # 如果有运行 nginx
# 或者停止其他占用 80 端口的服务

# 获取新域名的 SSL 证书
certbot certonly --standalone --preferred-challenges http -d schedulemeet.lemomate.com --non-interactive --agree-tos --email admin@lemomate.com

# 复制证书到应用目录
mkdir -p ssl
cp /etc/letsencrypt/live/schedulemeet.lemomate.com/fullchain.pem ssl/cert.pem
cp /etc/letsencrypt/live/schedulemeet.lemomate.com/privkey.pem ssl/key.pem
```

### 4. 启动应用

```bash
# 使用更新后的配置启动应用
docker-compose up -d
```

## 故障排除

### 502 错误解决方案

如果访问应用时出现 502 错误，可能是由于以下原因：

1. **SSL 证书问题**：确保已正确获取并配置 SSL 证书
   ```bash
   # 检查证书文件是否存在
   ls -la ssl/
   
   # 如果证书文件不存在，重新获取
   certbot certonly --standalone --preferred-challenges http -d schedulemeet.lemomate.com --non-interactive --agree-tos --email admin@lemomate.com
   cp /etc/letsencrypt/live/schedulemeet.lemomate.com/fullchain.pem ssl/cert.pem
   cp /etc/letsencrypt/live/schedulemeet.lemomate.com/privkey.pem ssl/key.pem
   ```

2. **端口冲突**：确保 8087 和 8443 端口未被其他服务占用
   ```bash
   # 检查端口占用情况
   netstat -tulpn | grep -E '8087|8443'
   
   # 如果有其他服务占用，停止相关服务或修改应用配置使用其他端口
   ```

3. **容器未正常启动**：检查容器状态
   ```bash
   # 查看容器状态
   docker-compose ps
   
   # 查看容器日志
   docker-compose logs frontend
   docker-compose logs backend
   ```

## 验证部署

完成上述步骤后，通过以下 URL 访问应用：

- 前端应用：https://schedulemeet.lemomate.com:8443
- 后端 API：https://schedulemeet.lemomate.com:8443/api

## 注意事项

1. 确保 Jitsi 服务（meet.lemomate.com）和 Lemomate 应用（schedulemeet.lemomate.com）使用不同的端口，避免端口冲突
2. 如果需要在同一服务器上运行多个应用，建议使用 Nginx 作为反向代理，将不同域名的请求转发到对应的应用