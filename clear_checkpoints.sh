#!/bin/bash
echo "清理 Flink TaskManager..."

# 清理 checkpoints
docker exec nexus-stack-taskmanager rm -rf /opt/flink/checkpoints/* 2>/dev/null

# 清理日志
docker exec nexus-stack-taskmanager find /opt/flink/log -type f -name "*.log*" -exec rm -f {} \; 2>/dev/null

# 重启容器释放空间
docker restart nexus-stack-taskmanager

echo "清理完成！"
docker ps -a --size | grep taskmanager
df -h