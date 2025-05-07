#!/bin/bash

echo "===== 开始构建后端 ====="

# 增加Docker构建的日志输出
docker build --progress=plain -t lemomate-backend -f Dockerfile.backend .

if [ $? -eq 0 ]; then
    echo "===== 后端构建成功 ====="
else
    echo "===== 后端构建失败 ====="
    exit 1
fi
