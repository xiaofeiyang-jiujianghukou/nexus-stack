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

# 创建初始化脚本（如果不存在）
if [ ! -f mysql/init/01_init.sql ]; then
    echo "📝 创建 MySQL 初始化脚本..."
    cat > mysql/init/01_init.sql << 'EOF'
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT PRIMARY KEY,
    level VARCHAR(50) DEFAULT 'NORMAL',
    register_time BIGINT
);

CREATE TABLE IF NOT EXISTS members (
    user_id BIGINT PRIMARY KEY,
    is_member TINYINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS orders (
    order_id BIGINT PRIMARY KEY,
    user_id BIGINT,
    amount DECIMAL(10,2),
    ts BIGINT,
    INDEX idx_user_id (user_id)
);
EOF
fi

if [ ! -f clickhouse/init/01_init.sql ]; then
    echo "📝 创建 ClickHouse 初始化脚本..."
    cat > clickhouse/init/01_init.sql << 'EOF'
CREATE TABLE IF NOT EXISTS dwd_order_wide (
    order_id Int64,
    user_id Int64,
    amount Float64,
    level String,
    is_member Int32,
    ts Int64
) ENGINE = MergeTree()
ORDER BY order_id;

CREATE TABLE IF NOT EXISTS ads_gmv_1m (
    stat_time DateTime,
    gmv Float64
) ENGINE = MergeTree()
ORDER BY stat_time;
EOF
fi

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