#!/bin/bash

echo "===== 检查容器状态 ====="

# 检查容器状态
echo "容器状态:"
sudo docker ps -a | grep lemomate

# 检查网络
echo -e "\n网络状态:"
sudo docker network ls | grep lemomate

# 检查后端日志
echo -e "\n后端日志 (最后20行):"
sudo docker logs lemomate-backend 2>&1 | tail -20 || echo "后端容器不存在或无法访问日志"

# 检查前端日志
echo -e "\n前端日志 (最后20行):"
sudo docker logs lemomate-frontend 2>&1 | tail -20 || echo "前端容器不存在或无法访问日志"

# 检查MySQL日志
echo -e "\nMySQL日志 (最后20行):"
sudo docker logs lemomate-mysql 2>&1 | tail -20 || echo "MySQL容器不存在或无法访问日志"

echo -e "\n===== 检查完成 ====="