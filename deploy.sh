#!/bin/bash

# 确保脚本在出错时停止执行
set -e

echo "开始部署 Lemomate 项目..."

# 检查 Docker 和 Docker Compose 是否已安装
if ! command -v docker &> /dev/null; then
    echo "Docker 未安装，正在安装..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    rm get-docker.sh
fi

if ! command -v docker-compose &> /dev/null; then
    echo "Docker Compose 未安装，正在安装..."
    sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# 创建上传目录
mkdir -p uploads/avatars

# 检查是否已安装certbot
if ! command -v certbot &> /dev/null; then
    echo "Certbot 未安装，正在安装..."
    sudo apt-get update
    sudo apt-get install -y certbot python3-certbot-nginx
fi

# 构建和启动容器
echo "构建和启动 Docker 容器..."
docker-compose up -d --build

# 询问是否配置SSL
read -p "是否配置SSL证书（推荐）？ [y/N] " configure_ssl
if [[ "$configure_ssl" =~ ^[Yy]$ ]]; then
    echo "正在配置SSL证书..."
    sudo certbot --nginx -d schedulemeet.lemomate.com

    # 使用SSL配置替换原来的Nginx配置
    cp nginx-ssl.conf /etc/nginx/conf.d/schedulemeet.lemomate.com.conf
    sudo systemctl reload nginx

    echo "SSL配置完成！"
    echo "您可以通过以下地址访问应用："
    echo "https://schedulemeet.lemomate.com"
else
    echo "跳过SSL配置。"
    echo "您可以通过以下地址访问应用："
    echo "http://schedulemeet.lemomate.com"
fi

echo ""
echo "部署完成！"
echo "请确保已将域名 schedulemeet.lemomate.com 指向此服务器的 IP 地址。"
