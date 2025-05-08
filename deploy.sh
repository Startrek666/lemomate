#!/bin/bash

# Lemomate一键部署脚本
echo "开始部署Lemomate应用到VPS..."

# 确保目录存在
mkdir -p ssl uploads/avatars

# 安装必要的软件
echo "安装Docker和Docker Compose..."
apt-get update
apt-get install -y docker.io docker-compose certbot

# 获取SSL证书
echo "获取SSL证书..."
certbot certonly --standalone --preferred-challenges http -d schedulemeet.lemomate.com --non-interactive --agree-tos --email admin@lemomate.com

# 复制SSL证书到nginx使用的目录
echo "配置SSL证书..."
mkdir -p ssl
cp /etc/letsencrypt/live/schedulemeet.lemomate.com/fullchain.pem ssl/cert.pem
cp /etc/letsencrypt/live/schedulemeet.lemomate.com/privkey.pem ssl/key.pem

# 启动Docker容器
echo "启动Docker容器..."
docker-compose up -d

echo "部署完成！应用已在 https://schedulemeet.lemomate.com:8443 上线"
echo "请确保您的域名 schedulemeet.lemomate.com 已经正确解析到IP: 104.233.196.100"
echo "注意：应用使用8087端口(HTTP)和8443端口(HTTPS)，确保这些端口未被其他服务占用"