CREATE TABLE ck_wide_orders (
                                     order_id  BIGINT,
                                     user_id   BIGINT,
                                     amount    DECIMAL(10, 2),
                                     `level`   STRING,
                                     is_member INT,
                                     ts        BIGINT,
                                     PRIMARY KEY (order_id) NOT ENFORCED
) WITH (
    'connector' = 'jdbc',
    'url' = '${app.datasource.clickhouse.jdbc-url}',
    'table-name' = 'dwd_order_wide',
    'username' = '${app.datasource.clickhouse.username}',
    'password' = '${app.datasource.clickhouse.password}',
    'driver' = 'com.clickhouse.jdbc.ClickHouseDriver',
    'sink.buffer-flush.max-rows' = '5000',
    'sink.buffer-flush.interval' = '2s',
    'sink.max-retries' = '3'
);