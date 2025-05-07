#!/bin/bash

# 设置错误时退出
set -e

echo "===== 开始构建 Lemomate 项目 ====="

# 1. 构建后端
echo "正在构建后端..."
docker build -t lemomate-backend -f Dockerfile.backend .
if [ $? -ne 0 ]; then
    echo "后端构建失败！"
    exit 1
fi
echo "后端构建成功！"

# 2. 构建前端
echo "正在构建前端..."
docker build -t lemomate-frontend -f Dockerfile.frontend .
if [ $? -ne 0 ]; then
    echo "前端构建失败！"
    exit 1
fi
echo "前端构建成功！"

# 3. 启动服务
echo "正在启动服务..."
docker-compose up -d
if [ $? -ne 0 ]; then
    echo "服务启动失败！"
    exit 1
fi

echo "===== Lemomate 项目构建并启动成功 ====="
echo "您可以通过以下地址访问应用："
echo "http://schedulemeet.lemomate.com"
