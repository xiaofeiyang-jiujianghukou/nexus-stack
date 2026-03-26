
drop table default.dwd_order_wide;

CREATE TABLE default.dwd_order_wide
(
    order_id   Int64,
    user_id    Int64,
    amount     Float64,
    level      String,
    is_member  Int32,
    ts         Int64
)
    ENGINE = ReplacingMergeTree(ts)
        PRIMARY KEY order_id
        ORDER BY (order_id)
        SETTINGS index_granularity = 8192;