#!/bin/bash

echo "========================================="
echo "清理 Nexus Stack（保留镜像）"
echo "========================================="

# 1. 停止所有容器
echo "1. 停止容器..."
docker-compose down -v

# 2. 删除 nexus-stack 相关的容器（确保干净）
echo "2. 删除容器..."
docker ps -a --filter "name=nexus-stack" -q | xargs -r docker rm -f

# 3. 删除网络
echo "3. 删除网络..."
docker network rm nexus-stack-net 2>/dev/null

# 4. 清理无用的容器和网络（不影响镜像）
echo "4. 清理 Docker 垃圾..."
docker container prune -f
docker network prune -f

# 5. 查看本地镜像（确认还在）
echo ""
echo "5. 本地 nexus-stack 镜像："
docker images | grep -E "nexus-stack|clickhouse|mysql|redis|flink" || echo "未找到镜像"

echo ""
echo "========================================="
echo "✅ 清理完成"
echo "========================================="
echo ""
echo "执行以下命令重新启动（会使用本地镜像）："
echo "  docker-compose up -d"
echo ""
echo "查看启动日志："
echo "  docker-compose logs -f"