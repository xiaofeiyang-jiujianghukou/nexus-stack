CREATE TABLE IF NOT EXISTS ck_wide_orders (
    order_id  BIGINT,
    user_id   BIGINT,
    amount    DECIMAL(10, 2),
    `level`   STRING,
    is_member INT,
    ts        BIGINT,
    PRIMARY KEY (order_id) NOT ENFORCED
) WITH (
    'connector' = 'jdbc',
    'url' = 'jdbc:clickhouse://127.0.0.1:38123/default',
    'username' = 'default',
    'password' = '123456',
    'table-name' = 'dwd_order_wide',
    'driver' = 'com.clickhouse.jdbc.ClickHouseDriver',
    'sink.buffer-flush.max-rows' = '5000',
    'sink.buffer-flush.interval' = '2s',
    'sink.max-retries' = '3'
);