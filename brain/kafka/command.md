## 创建 *user-topic*
### 这个 Topic 就像数据库的表，相同的 userId 只会保留最后一条记录，方便 Flink 启动时快速回放最新的用户信息。
docker exec -it nexus-stack-kafka kafka-topics --bootstrap-server localhost:9092 --create \
--topic user-topic \
--partitions 1 \
--replication-factor 1 \
--config cleanup.policy=compact \
--config min.cleanable.dirty.ratio=0.01 \
--config segment.ms=100

## 创建 *order-topic*
### 这个 Topic 承载实时订单流，数据按时间顺序进入。
docker exec -it nexus-stack-kafka kafka-topics --bootstrap-server localhost:9092 --create \
--topic order-topic \
--partitions 1 \
--replication-factor 1

## 创建 *member-topic*
### 在流处理架构中，user 和 member 都属于维度数据（Dimension Data）。
docker exec -it nexus-stack-kafka kafka-topics --create \
--bootstrap-server localhost:9092 \
--topic member-topic \
--partitions 1 \
--replication-factor 1 \
--config cleanup.policy=compact \
--config delete.retention.ms=100 \
--config min.cleanable.dirty.ratio=0.01

## 删除
docker exec -it nexus-stack-kafka kafka-topics --bootstrap-server localhost:9092 --delete --topic order-topic

## 列表
docker exec -it nexus-stack-kafka kafka-topics --bootstrap-server localhost:9092 --list

## 状态
docker exec -it nexus-stack-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic user-topic

## 消息
docker exec -it nexus-stack-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic order-topic --from-beginning --max-messages 1

## 消息内容
docker exec -it nexus-stack-kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic order-topic --from-beginning --max-messages 10
