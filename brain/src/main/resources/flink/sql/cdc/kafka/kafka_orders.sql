CREATE TABLE IF NOT EXISTS kafka_orders (
                              orderId BIGINT,
                              userId BIGINT,
                              amount DOUBLE,
                              ts BIGINT,
                              proc_time AS PROCTIME(),
                              row_time AS TO_TIMESTAMP(FROM_UNIXTIME(ts / 1000)),
                              WATERMARK FOR row_time AS row_time - INTERVAL '3' SECOND
) WITH (
      'connector' = 'kafka',
      'topic' = 'order-topic',
      'properties.bootstrap.servers' = '${kafka.bootstrap-servers}',
      'format' = 'json',
      'scan.startup.mode' = 'latest-offset'
      )