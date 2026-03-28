drop table if exists default.dwd_order_wide;

create table default.dwd_order_wide
(
    order_id  Int64,
    user_id   Int64,
    amount    Decimal(10, 2),
    level     String,
    is_member Int32,
    ts        Int64
)
    engine = ReplacingMergeTree(ts)
        PRIMARY KEY order_id
        ORDER BY order_id
        SETTINGS index_granularity = 8192;

