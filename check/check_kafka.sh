#!/bin/bash
echo "=== Kafka 深度诊断 ==="
echo ""

# 1. 当前 CPU
echo "1. CPU 占用:"
docker stats nexus-stack-kafka --no-stream --format "  {{.CPUPerc}}"

# 2. 查看高 CPU 线程
echo ""
echo "2. CPU 最高的 5 个线程:"
docker exec nexus-stack-kafka ps -eLo pid,tid,pcpu,comm | grep java | sort -k3 -rn | head -5

# 3. GC 统计
echo ""
echo "3. GC 统计:"
docker exec nexus-stack-kafka jstat -gcutil 1 2>/dev/null | tail -2

# 4. LogCleaner 线程
echo ""
echo "4. LogCleaner 线程:"
docker exec nexus-stack-kafka jstack 1 2>/dev/null | grep -i cleaner | head -5

# 5. 磁盘 I/O
echo ""
echo "5. 磁盘 I/O:"
docker exec nexus-stack-kafka iostat -x 1 2 2>/dev/null || echo "  iostat 不可用"

# 6. 查看 Kafka 日志最后 10 行
echo ""
echo "6. 最近日志:"
docker logs nexus-stack-kafka --tail 10