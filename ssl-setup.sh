#!/bin/bash

# Lemomate SSL证书设置脚本
echo "开始为meet.lemomate.com配置SSL证书..."

# 安装certbot
apt-get update
apt-get install -y certbot

# 停止nginx以释放80端口
docker-compose stop frontend

# 获取SSL证书
echo "获取SSL证书..."
certbot certonly --standalone --preferred-challenges http -d meet.lemomate.com --non-interactive --agree-tos --email admin@lemomate.com

# 复制SSL证书到nginx使用的目录
echo "配置SSL证书..."
mkdir -p ssl
cp /etc/letsencrypt/live/meet.lemomate.com/fullchain.pem ssl/cert.pem
cp /etc/letsencrypt/live/meet.lemomate.com/privkey.pem ssl/key.pem

# 重启nginx
docker-compose start frontend

echo "SSL证书配置完成！"
echo "您现在可以通过 https://meet.lemomate.com 访问应用"

# 添加证书自动更新的cron任务
echo "配置证书自动更新..."
PROJECT_DIR=$(pwd)
(crontab -l 2>/dev/null; echo "0 3 * * * certbot renew --quiet && cp /etc/letsencrypt/live/meet.lemomate.com/fullchain.pem $PROJECT_DIR/ssl/cert.pem && cp /etc/letsencrypt/live/meet.lemomate.com/privkey.pem $PROJECT_DIR/ssl/key.pem && docker-compose restart frontend") | crontab -

echo "证书自动更新已配置，将在每天凌晨3点检查并更新"