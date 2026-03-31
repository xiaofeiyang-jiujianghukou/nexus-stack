#!/bin/bash
echo "=== Nexus-Stack 日志使用情况 ==="
echo ""

# 检查各容器日志大小
echo "容器日志大小:"
for container in $(docker ps -a --filter "name=nexus-stack" --format "{{.Names}}"); do
    log_path=$(docker inspect --format='{{.LogPath}}' $container 2>/dev/null)
    if [ -f "$log_path" ]; then
        size=$(ls -lh $log_path | awk '{print $5}')
        echo "  $container: $size"
    fi
done

echo ""
echo "Flink checkpoints:"
docker exec nexus-stack-taskmanager du -sh /opt/flink/checkpoints 2>/dev/null

echo ""
echo "ClickHouse 日志:"
docker exec nexus-stack-clickhouse du -sh /var/log/clickhouse-server 2>/dev/null

echo ""
df -h | grep vda3