#!/bin/bash

echo "===== 重新构建后端 ====="

# 停止并删除后端容器
echo "停止并删除后端容器..."
docker stop lemomate-backend 2>/dev/null || true
docker rm lemomate-backend 2>/dev/null || true

# 删除后端镜像
echo "删除后端镜像..."
docker rmi lemomate_backend 2>/dev/null || true

# 检查并创建Docker网络
echo "检查Docker网络..."
if ! docker network inspect lemomate-network &>/dev/null; then
    echo "创建Docker网络: lemomate-network"
    docker network create lemomate-network
else
    echo "Docker网络 lemomate-network 已存在"
fi

# 检查MySQL容器是否运行
echo "检查MySQL容器..."
if ! docker ps | grep -q lemomate-mysql; then
    echo "MySQL容器未运行，正在启动..."
    docker start lemomate-mysql 2>/dev/null || docker run -d --name lemomate-mysql --network lemomate-network -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=lemomate mysql:8.0
    
    # 等待MySQL启动
    echo "等待MySQL启动..."
    sleep 10
else
    echo "MySQL容器已运行"
fi

# 重新构建后端
echo "重新构建后端..."
docker build -t lemomate_backend -f Dockerfile.backend .

# 启动后端容器
echo "启动后端容器..."
docker run -d --name lemomate-backend --network lemomate-network \
    -e SPRING_DATASOURCE_URL=jdbc:mysql://lemomate-mysql:3306/lemomate?useSSL=false&serverTimezone=UTC&characterEncoding=utf8 \
    -e SPRING_DATASOURCE_USERNAME=root \
    -e SPRING_DATASOURCE_PASSWORD=root \
    -e JWT_SECRET=EldqqV5j3JQax6yaKe3XKKNVQLZRpT2g \
    -e JITSI_DOMAIN=meet.lemomate.com \
    -e JITSI_APP_ID=lemomate_app \
    lemomate_backend

echo "后端重建完成！"
