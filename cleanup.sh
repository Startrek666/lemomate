#!/bin/bash

echo "===== 停止并清理 Lemomate 容器 ====="

# 停止并删除容器
echo "停止并删除容器..."
docker-compose down

# 删除镜像
echo "删除镜像..."
docker rmi lemomate-frontend lemomate-backend

echo "清理完成！"
