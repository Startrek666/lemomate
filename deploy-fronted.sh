#!/bin/bash

echo "===== 部署前端 ====="

# 停止并删除前端容器
echo "停止并删除前端容器..."
sudo docker stop lemomate-frontend 2>/dev/null || true
sudo docker rm lemomate-frontend 2>/dev/null || true

# 构建前端
echo "构建前端..."
sudo docker build -t lemomate_frontend -f Dockerfile.frontend .

# 启动前端容器
echo "启动前端容器..."
sudo docker run -d --name lemomate-frontend --network lemomate-network -p 8089:80 lemomate_frontend

# 检查前端容器状态
echo "前端容器状态:"
sudo docker ps -a | grep lemomate-frontend

echo "===== 前端部署完成 ====="