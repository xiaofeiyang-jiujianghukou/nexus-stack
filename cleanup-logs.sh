#!/bin/bash

# 只清理 nexus-stack 项目的日志
# 项目目录
PROJECT_DIR="/root/nexus-stack"
cd $PROJECT_DIR

echo "=== 清理 nexus-stack 项目日志 ==="

# 1. 清理 nexus-stack 相关容器的 Docker 日志
echo "清理容器日志..."
for container in $(docker ps -a --filter "name=nexus-stack" -q); do
    container_name=$(docker inspect --format='{{.Name}}' $container | sed 's/\///')
    log_path=$(docker inspect --format='{{.LogPath}}' $container 2>/dev/null)
    if [ -f "$log_path" ]; then
        echo "清理: $container_name"
        sudo truncate -s 0 $log_path
    fi
done

# 2. 清理 Flink checkpoints（保留最近3个）
echo "清理 Flink checkpoints..."
docker exec nexus-stack-taskmanager find /opt/flink/checkpoints -type d -mtime +1 -exec rm -rf {} \; 2>/dev/null

# 3. 清理 Flink 日志
echo "清理 Flink 日志..."
docker exec nexus-stack-taskmanager find /opt/flink/log -name "*.log.*" -mtime +1 -delete 2>/dev/null
docker exec nexus-stack-jobmanager find /opt/flink/log -name "*.log.*" -mtime +1 -delete 2>/dev/null

# 4. 清理 ClickHouse 日志（保留最近3天）
echo "清理 ClickHouse 日志..."
docker exec nexus-stack-clickhouse find /var/log/clickhouse-server -name "*.log" -mtime +3 -exec truncate -s 0 {} \; 2>/dev/null

# 5. 清理项目挂载的日志文件
echo "清理挂载日志..."
find $PROJECT_DIR/data -name "*.log" -size +10M -exec truncate -s 0 {} \;

# 6. 清理未使用的 Docker 资源（只针对 nexus-stack）
echo "清理 Docker 资源..."
docker system prune -f --filter "label=com.docker.compose.project=nexus-stack" 2>/dev/null

echo "=== 清理完成 ==="
df -h