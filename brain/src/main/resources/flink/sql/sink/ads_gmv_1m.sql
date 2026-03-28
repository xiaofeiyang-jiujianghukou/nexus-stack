CREATE TABLE IF NOT EXISTS ads_gmv_1m (
    stat_time   TIMESTAMP_LTZ(3),
    gmv         DECIMAL(10, 2)
) WITH (
    'connector' = 'jdbc',
    'url' = '${app.datasource.clickhouse.jdbc-url}',
    'table-name' = 'ads_gmv_1m',
    'username' = '${app.datasource.clickhouse.username}',
    'password' = '${app.datasource.clickhouse.password}',
    'driver' = 'com.clickhouse.jdbc.ClickHouseDriver',
    'sink.buffer-flush.max-rows' = '5000',
    'sink.buffer-flush.interval' = '2s',
    'sink.max-retries' = '3'
);