#!/bin/bash

echo "===== 检查端口占用情况 ====="

echo "检查端口 80..."
sudo netstat -tulpn | grep :80

echo "检查端口 8080..."
sudo netstat -tulpn | grep :8080

echo "检查端口 8085..."
sudo netstat -tulpn | grep :8085

echo "检查端口 8089..."
sudo netstat -tulpn | grep :8089

echo "===== 检查完成 ====="
