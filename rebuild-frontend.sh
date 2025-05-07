#!/bin/bash

echo "===== 重新构建前端 ====="

# 停止并删除前端容器
echo "停止并删除前端容器..."
docker stop lemomate-frontend 2>/dev/null || true
docker rm lemomate-frontend 2>/dev/null || true

# 删除前端镜像
echo "删除前端镜像..."
docker rmi lemomate_frontend 2>/dev/null || true

# 检查并创建Docker网络
echo "检查Docker网络..."
if ! docker network inspect lemomate-network &>/dev/null; then
    echo "创建Docker网络: lemomate-network"
    docker network create lemomate-network
else
    echo "Docker网络 lemomate-network 已存在"
fi

# 重新构建前端
echo "重新构建前端..."
docker build -t lemomate_frontend -f Dockerfile.frontend .

# 启动前端容器
echo "启动前端容器..."
docker run -d --name lemomate-frontend --network lemomate-network -p 8089:80 lemomate_frontend

echo "前端重建完成！"
