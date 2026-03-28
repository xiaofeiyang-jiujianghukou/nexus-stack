CREATE TABLE IF NOT EXISTS dwd_order_wide (
    order_id Int64,
    user_id Int64,
    amount Float64,
    level String,
    is_member Int32,
    ts Int64
) ENGINE = MergeTree()
ORDER BY order_id;

CREATE TABLE IF NOT EXISTS ads_gmv_1m (
    stat_time DateTime,
    gmv Float64
) ENGINE = MergeTree()
ORDER BY stat_time;
