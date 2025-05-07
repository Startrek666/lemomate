#!/bin/bash

echo "===== 开始重新部署 Lemomate 项目 ====="

# 清理现有容器和网络
echo "清理现有容器和网络..."
./cleanup.sh

# 创建Docker网络
echo "创建Docker网络..."
docker network create lemomate-network

# 启动MySQL容器
echo "启动MySQL容器..."
docker run -d --name lemomate-mysql --network lemomate-network \
    -e MYSQL_ROOT_PASSWORD=root \
    -e MYSQL_DATABASE=lemomate \
    mysql:8.0

# 等待MySQL启动
echo "等待MySQL启动..."
sleep 15

# 构建并启动后端
echo "构建并启动后端..."
docker build -t lemomate_backend -f Dockerfile.backend .
docker run -d --name lemomate-backend --network lemomate-network \
    -e SPRING_DATASOURCE_URL=jdbc:mysql://lemomate-mysql:3306/lemomate?useSSL=false&serverTimezone=UTC&characterEncoding=utf8 \
    -e SPRING_DATASOURCE_USERNAME=root \
    -e SPRING_DATASOURCE_PASSWORD=root \
    -e JWT_SECRET=EldqqV5j3JQax6yaKe3XKKNVQLZRpT2g \
    -e JITSI_DOMAIN=meet.lemomate.com \
    -e JITSI_APP_ID=lemomate_app \
    lemomate_backend

# 构建并启动前端
echo "构建并启动前端..."
docker build -t lemomate_frontend -f Dockerfile.frontend .
docker run -d --name lemomate-frontend --network lemomate-network -p 8089:80 lemomate_frontend

# 配置Nginx
echo "配置Nginx..."
if [ -d "/etc/nginx/conf.d" ]; then
    sudo cp nginx-proxy.conf /etc/nginx/conf.d/schedulemeet.lemomate.com.conf
    sudo nginx -t && sudo systemctl reload nginx
    echo "Nginx配置已更新"
else
    echo "警告: 找不到Nginx配置目录，请手动配置Nginx"
fi

# 检查容器状态
echo "检查容器状态..."
docker ps -a | grep lemomate

echo "===== 重新部署完成 ====="
echo "请访问 http://schedulemeet.lemomate.com 查看应用"
