CREATE TABLE IF NOT EXISTS ads_gmv_1m (
    stat_time   TIMESTAMP_LTZ(3),
    gmv         DECIMAL(10, 2)
) WITH (
    'connector' = 'jdbc',
    'url' = 'jdbc:clickhouse://127.0.0.1:38123/default',
    'username' = 'default',
    'password' = '123456',
    'table-name' = 'ads_gmv_1m',
    'driver' = 'com.clickhouse.jdbc.ClickHouseDriver',
    'sink.buffer-flush.max-rows' = '5000',
    'sink.buffer-flush.interval' = '2s',
    'sink.max-retries' = '3'
);