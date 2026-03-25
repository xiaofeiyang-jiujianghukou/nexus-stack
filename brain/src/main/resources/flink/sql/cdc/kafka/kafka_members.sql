CREATE TABLE kafka_members (
                               userId STRING,
                               isMember BOOLEAN,
                               ts BIGINT,
                               PRIMARY KEY (userId) NOT ENFORCED
) WITH (
      'connector' = 'upsert-kafka',
      'topic' = 'member-topic',
      'properties.bootstrap.servers' = '${kafka.bootstrap-servers}',
      'key.format' = 'raw',
      'value.format' = 'json'
      )