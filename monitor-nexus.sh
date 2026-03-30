#!/bin/bash

echo "========================================="
echo "Nexus Stack 简化监控 - $(date '+%Y-%m-%d %H:%M:%S')"
echo "========================================="
echo ""

# 显示容器资源使用
echo "📊 容器资源使用:"
docker stats --no-stream $(docker ps --filter "name=nexus-stack" -q) --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}"

echo ""
echo "🔥 CPU 占用 TOP 3:"
docker stats --no-stream $(docker ps --filter "name=nexus-stack" -q) --format "{{.Name}}\t{{.CPUPerc}}" | sort -k2 -r | head -3

echo ""
echo "💾 内存占用 TOP 3:"
docker stats --no-stream $(docker ps --filter "name=nexus-stack" -q) --format "{{.Name}}\t{{.MemUsage}}" | sort -k2 -r | head -3

echo ""
echo "📈 ClickHouse 活动查询数:"
docker exec nexus-stack-clickhouse clickhouse-client --password 123456 --query "
SELECT count() as active_queries 
FROM system.processes 
WHERE query NOT LIKE '%system.processes%'
" 2>/dev/null || echo "0"

echo ""
echo "🔄 ClickHouse 合并任务数:"
docker exec nexus-stack-clickhouse clickhouse-client --password 123456 --query "
SELECT count() as merging_tasks 
FROM system.merges
" 2>/dev/null || echo "0"

echo ""
echo "📁 ClickHouse 表分区数:"
docker exec nexus-stack-clickhouse clickhouse-client --password 123456 --query "
SELECT 
    table,
    count() as parts,
    formatReadableSize(sum(bytes)) as size
FROM system.parts 
WHERE active
GROUP BY table
ORDER BY parts DESC
LIMIT 3
FORMAT PrettyCompact
" 2>/dev/null

echo ""
echo "⏰ 系统负载:"
uptime