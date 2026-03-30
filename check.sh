# 一键检查所有服务
#!/bin/bash

echo "========================================="
echo "Nexus Stack 健康检查"
echo "========================================="
echo ""

echo "1. ClickHouse 状态:"
if docker exec nexus-stack-clickhouse clickhouse-client --password 123456 --query "SELECT 1" 2>/dev/null; then
    echo "✅ ClickHouse 正常"
else
    echo "❌ ClickHouse 异常"
fi

echo ""
echo "2. MySQL 状态:"
if docker exec nexus-stack-mysql mysql -p123456 -e "SELECT 1" 2>/dev/null; then
    echo "✅ MySQL 正常"
else
    echo "❌ MySQL 异常"
fi

echo ""
echo "3. Redis 状态:"
if docker exec nexus-stack-redis redis-cli -a 123456 ping 2>/dev/null | grep -q PONG; then
    echo "✅ Redis 正常"
else
    echo "❌ Redis 异常"
fi

echo ""
echo "4. Brain 到 ClickHouse 连接:"
if docker exec nexus-stack-brain sh -c "timeout 3 nc -zv clickhouse 8123 2>&1" | grep -q "open"; then
    echo "✅ Brain 可以连接 ClickHouse"
else
    echo "❌ Brain 无法连接 ClickHouse"
fi

echo ""
echo "5. Brain 日志（最近5行）:"
docker logs nexus-stack-brain --tail=5 2>&1 | grep -v "WARN"

echo ""
echo "6. 容器资源使用:"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" \
    $(docker ps --filter "name=nexus-stack" -q)
EOF