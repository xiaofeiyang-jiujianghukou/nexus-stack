#!/bin/bash
set -e

echo "========================================="
echo "Nexus Stack 部署脚本"
echo "========================================="

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker 未安装${NC}"
    exit 1
fi

# 创建必要的目录
echo "📁 创建目录..."
mkdir -p \
    mysql/init \
    clickhouse/init \
    redis/logs \
    kafka/logs

# 自动修复权限（让容器能写入）
echo "设置目录权限..."

# ClickHouse 需要 uid 101
sudo chown -R 101:101 data/clickhouse 2>/dev/null || true
sudo chmod -R 755 data/clickhouse 2>/dev/null || true

# MySQL 需要 uid 999
sudo chown -R 999:999 data/mysql 2>/dev/null || true
sudo chmod -R 755 data/mysql 2>/dev/null || true

# Redis 需要 uid 999
sudo chown -R 999:999 data/redis 2>/dev/null || true

echo "✅ 权限设置完成"

# 启动服务
echo "🚀 启动服务..."
docker-compose up -d

# 等待服务就绪
echo "⏳ 等待服务就绪..."
sleep 10

# 显示状态
echo ""
echo "========================================="
echo -e "${GREEN}✅ 部署完成！${NC}"
echo "========================================="
echo ""
echo "服务访问地址:"
echo "  MySQL:      localhost:33306"
echo "  ClickHouse: localhost:38123"
echo "  Redis:      localhost:36379"
echo "  Kafka:      localhost:9092 / 39092"
echo "  Flink:      localhost:38081"
echo "  SQL Gateway: localhost:38083"
echo ""
echo "查看状态: docker-compose ps"
echo "停止服务: ./shutdown.sh stop"
echo "查看日志: docker-compose logs -f"