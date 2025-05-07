#!/bin/bash

echo "===== 检查容器状态 ====="

# 检查容器状态
echo "容器状态:"
docker ps -a | grep lemomate

# 检查网络
echo -e "\n网络状态:"
docker network ls | grep lemomate

# 检查后端日志
echo -e "\n后端日志 (最后20行):"
docker logs lemomate-backend 2>&1 | tail -20

# 检查前端日志
echo -e "\n前端日志 (最后20行):"
docker logs lemomate-frontend 2>&1 | tail -20

echo -e "\n===== 检查完成 ====="
