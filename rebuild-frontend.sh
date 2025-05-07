#!/bin/bash

echo "===== 重新构建前端 ====="

# 停止并删除前端容器
echo "停止并删除前端容器..."
docker stop lemomate-frontend
docker rm lemomate-frontend

# 删除前端镜像
echo "删除前端镜像..."
docker rmi lemomate_frontend

# 重新构建前端
echo "重新构建前端..."
docker build -t lemomate_frontend -f Dockerfile.frontend .

# 启动前端容器
echo "启动前端容器..."
docker run -d --name lemomate-frontend --network lemomate-network -p 8089:80 lemomate_frontend

echo "前端重建完成！"
