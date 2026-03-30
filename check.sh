#!/bin/bash

echo "========================================="
echo "Nexus Stack 健康检查"
echo "========================================="
echo ""

# 1. ClickHouse 状态
echo -n "1. ClickHouse: "
if docker exec nexus-stack-clickhouse clickhouse-client --password 123456 --query "SELECT 1" > /dev/null 2>&1; then
    echo "✅ 正常"
else
    echo "❌ 异常"
fi

# 2. MySQL 状态
echo -n "2. MySQL: "
if docker exec nexus-stack-mysql mysql -uroot -p123456 -e "SELECT 1" > /dev/null 2>&1; then
    echo "✅ 正常"
else
    echo "❌ 异常"
fi

# 3. Redis 状态
echo -n "3. Redis: "
if docker exec nexus-stack-redis redis-cli -a 123456 ping > /dev/null 2>&1; then
    echo "✅ 正常"
else
    echo "❌ 异常"
fi

# 4. Flink JobManager 状态
echo -n "4. Flink JobManager: "
if docker exec nexus-stack-jobmanager curl -s http://localhost:8081/overview > /dev/null 2>&1; then
    echo "✅ 正常"
else
    echo "❌ 异常"
fi

# 5. Flink TaskManager 状态
echo -n "5. Flink TaskManager: "
if docker exec nexus-stack-taskmanager curl -s http://localhost:8081/overview > /dev/null 2>&1; then
    echo "✅ 正常"
else
    echo "❌ 异常"
fi

# 6. Flink SQL Gateway 状态
echo -n "6. Flink SQL Gateway: "
if docker exec nexus-stack-sql-gateway curl -s http://localhost:8083/ > /dev/null 2>&1; then
    echo "✅ 正常"
else
    echo "❌ 异常"
fi

# 7. Brain 到 ClickHouse 连接
echo -n "7. Brain -> ClickHouse: "
if docker exec nexus-stack-brain sh -c "timeout 3 nc -zv clickhouse 8123" > /dev/null 2>&1; then
    echo "✅ 连通"
else
    echo "❌ 不通"
fi

# 8. Brain 到 MySQL 连接
echo -n "8. Brain -> MySQL: "
if docker exec nexus-stack-brain sh -c "timeout 3 nc -zv mysql 3306" > /dev/null 2>&1; then
    echo "✅ 连通"
else
    echo "❌ 不通"
fi

# 9. Brain 到 Redis 连接
echo -n "9. Brain -> Redis: "
if docker exec nexus-stack-brain sh -c "timeout 3 nc -zv redis 6379" > /dev/null 2>&1; then
    echo "✅ 连通"
else
    echo "❌ 不通"
fi

echo ""
echo "10. Brain 日志（最近3行）:"
docker logs nexus-stack-brain --tail=3 2>&1 | grep -v "WARN"

echo ""
echo "11. 容器资源使用:"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" \
    $(docker ps --filter "name=nexus-stack" -q 2>/dev/null) 2>/dev/null

echo ""
echo "12. Flink 集群状态:"

# 获取 overview 数据
OVERVIEW=$(docker exec nexus-stack-jobmanager curl -s http://localhost:8081/overview 2>/dev/null)

# TaskManagers 数量
TM_COUNT=$(echo "$OVERVIEW" | grep -o '"taskmanakers":[0-9]*' 2>/dev/null || echo "")
if [ -z "$TM_COUNT" ]; then
    TM_COUNT=$(echo "$OVERVIEW" | sed -n 's/.*"taskmanagers":\([0-9]*\).*/\1/p')
fi
echo "    TaskManagers: ${TM_COUNT:-0}"

# Slots 总数
SLOTS_TOTAL=$(echo "$OVERVIEW" | sed -n 's/.*"slots-total":\([0-9]*\).*/\1/p')
echo "    Slots: ${SLOTS_TOTAL:-0}"

# 可用 Slots
SLOTS_AVAILABLE=$(echo "$OVERVIEW" | sed -n 's/.*"slots-available":\([0-9]*\).*/\1/p')
echo "    Available Slots: ${SLOTS_AVAILABLE:-0}"

# 获取 jobs overview 数据
JOBS_OVERVIEW=$(docker exec nexus-stack-jobmanager curl -s http://localhost:8081/jobs/overview 2>/dev/null)

# 运行中的 Jobs
JOBS_RUNNING=$(echo "$JOBS_OVERVIEW" | sed -n 's/.*"running":\([0-9]*\).*/\1/p')
echo "    Jobs Running: ${JOBS_RUNNING:-0}"

# 完成的 Jobs
JOBS_FINISHED=$(echo "$JOBS_OVERVIEW" | sed -n 's/.*"finished":\([0-9]*\).*/\1/p')
echo "    Jobs Finished: ${JOBS_FINISHED:-0}"

# 取消的 Jobs
JOBS_CANCELLED=$(echo "$JOBS_OVERVIEW" | sed -n 's/.*"cancelled":\([0-9]*\).*/\1/p')
echo "    Jobs Cancelled: ${JOBS_CANCELLED:-0}"

# 失败的 Jobs
JOBS_FAILED=$(echo "$JOBS_OVERVIEW" | sed -n 's/.*"failed":\([0-9]*\).*/\1/p')
echo "    Jobs Failed: ${JOBS_FAILED:-0}"

echo ""
echo "========================================="
echo "检查完成"
echo "========================================="