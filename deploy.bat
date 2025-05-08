@echo off
echo 开始部署Lemomate应用到VPS...

REM 确保目录存在
echo 创建必要的目录...
mkdir ssl 2>nul
mkdir uploads\avatars 2>nul

REM 检查Docker是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：Docker未安装，请先安装Docker和Docker Compose
    exit /b 1
)

REM 构建和启动Docker容器
echo 启动Docker容器...
docker-compose up -d

echo.
echo 部署完成！
echo.
echo 请确保：
echo 1. 您的域名 schedulemeet.lemomate.com 已经正确解析到IP: 104.233.196.100
echo 2. 在VPS上运行以下命令获取SSL证书：
echo    certbot certonly --standalone -d schedulemeet.lemomate.com
echo    cp /etc/letsencrypt/live/schedulemeet.lemomate.com/fullchain.pem ssl/cert.pem
echo    cp /etc/letsencrypt/live/schedulemeet.lemomate.com/privkey.pem ssl/key.pem
echo    docker-compose restart frontend
echo.
echo 应用将在 https://schedulemeet.lemomate.com:8443 上线
echo 注意：应用使用8087端口(HTTP)和8443端口(HTTPS)，确保这些端口未被其他服务占用

pause