CREATE TABLE kafka_users (
                             userId STRING,
                             level STRING,
                             registerTime BIGINT,
                             ts BIGINT,
                             PRIMARY KEY (userId) NOT ENFORCED
) WITH (
      'connector' = 'upsert-kafka',
      'topic' = 'user-topic',
      'properties.bootstrap.servers' = '${kafka.bootstrap-servers}',
      'key.format' = 'raw',
      'value.format' = 'json'
      );