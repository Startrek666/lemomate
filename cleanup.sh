#!/bin/bash

echo "===== 停止并清理 Lemomate 容器 ====="

# 停止并删除容器
echo "停止并删除容器..."
docker-compose down

# 删除容器
echo "删除所有相关容器..."
docker rm -f lemomate-frontend lemomate-backend lemomate-mysql 2>/dev/null || true

# 删除镜像
echo "删除镜像..."
docker rmi lemomate-frontend lemomate-backend 2>/dev/null || true

# 删除网络
echo "删除Docker网络..."
docker network rm lemomate-network 2>/dev/null || true

echo "清理完成！"
