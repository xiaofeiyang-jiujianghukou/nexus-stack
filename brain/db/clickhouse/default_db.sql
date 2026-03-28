drop table if exists default.ads_gmv_1m;

create table default.ads_gmv_1m
(
    stat_time DateTime64(3),
    gmv       Decimal(16, 2)
)
    engine = MergeTree ORDER BY stat_time
        SETTINGS index_granularity = 8192;

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

