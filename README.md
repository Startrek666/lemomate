# Lemomate 会议管理系统

Lemomate是一个基于Jitsi的会议管理系统，提供会议创建、管理和参与功能。

## 技术栈

- 前端：Vue.js + Element UI
- 后端：Spring Boot + Spring Security + Spring Data JPA
- 数据库：MySQL
- 视频会议：Jitsi Meet

## 部署指南

### 前提条件

- 一台运行Linux的VPS服务器（推荐Ubuntu 20.04或更高版本）
- 域名已配置并指向服务器IP（例如：schedulemeet.lemomate.com）
- Docker和Docker Compose已安装
- Jitsi Meet已在同一服务器上部署（域名为meet.lemomate.com）
- Nginx已安装并运行（用于反向代理）

### 端口使用

- Lemomate前端容器使用端口8089
- Lemomate后端容器使用端口8085
- Nginx配置为将schedulemeet.lemomate.com的请求代理到端口8089

### 部署步骤

1. 克隆项目到服务器

```bash
git clone <repository-url> lemomate
cd lemomate
```

2. 配置环境变量

编辑`.env`文件，设置安全的密码和密钥：

```bash
nano .env
```

3. 执行部署脚本

```bash
chmod +x deploy.sh
./deploy.sh
```

4. 配置域名

确保您的域名`schedulemeet.lemomate.com`已正确指向服务器IP地址。

5. 配置SSL（可选但推荐）

使用Let's Encrypt配置HTTPS：

```bash
sudo apt-get update
sudo apt-get install certbot python3-certbot-nginx
sudo certbot --nginx -d schedulemeet.lemomate.com
```

### 维护命令

- 查看日志：`docker-compose logs -f`
- 重启服务：`docker-compose restart`
- 停止服务：`docker-compose down`
- 更新并重启：`git pull && docker-compose up -d --build`

## 用户角色

- 普通用户：参加会议
- 团队管理员：创建和管理会议，管理团队成员
- 平台管理员：管理所有用户和团队

## Jitsi集成

本项目与Jitsi Meet集成，使用以下方式：

1. 会议链接格式：`https://meet.lemomate.com/[room_name]?jwt=[YOUR_TOKEN]`
2. JWT令牌包含用户信息（姓名、邮箱、头像）和权限信息
3. 会议房间名称基于会议标题生成，确保唯一性

要使此集成正常工作，请确保：

1. Jitsi Meet服务器已配置JWT认证
2. `.env`文件中的`JITSI_APP_ID`和`JWT_SECRET`与Jitsi配置一致

## 注意事项

- 首次部署后，请立即创建平台管理员账户
- 定期备份MySQL数据：`docker-compose exec mysql mysqldump -u root -p lemomate > backup.sql`
- 生产环境中请更改默认的JWT密钥
- 确保您的Jitsi Meet实例已正确配置JWT认证
