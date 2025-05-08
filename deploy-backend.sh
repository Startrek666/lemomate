#!/bin/bash

echo "===== 部署后端 ====="

# 停止并删除后端容器
echo "停止并删除后端容器..."
sudo docker stop lemomate-backend 2>/dev/null || true
sudo docker rm lemomate-backend 2>/dev/null || true

# 构建后端
echo "构建后端..."
sudo docker build -t lemomate_backend -f Dockerfile.backend .

# 启动后端容器
echo "启动后端容器..."
sudo docker run -d --name lemomate-backend --network lemomate-network \
    -e SPRING_DATASOURCE_URL="jdbc:mysql://lemomate-mysql:3306/lemomate?useSSL=false&serverTimezone=UTC&characterEncoding=utf8" \
    -e SPRING_DATASOURCE_USERNAME="root" \
    -e SPRING_DATASOURCE_PASSWORD="root" \
    -e JWT_SECRET="EldqqV5j3JQax6yaKe3XKKNVQLZRpT2g" \
    -e JITSI_DOMAIN="meet.lemomate.com" \
    -e JITSI_APP_ID="lemomate_app" \
    lemomate_backend

# 检查后端容器状态
echo "后端容器状态:"
sudo docker ps -a | grep lemomate-backend

# 显示后端日志
echo "后端日志:"
sudo docker logs lemomate-backend | tail -20

echo "===== 后端部署完成 ====="