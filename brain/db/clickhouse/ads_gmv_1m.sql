drop table default.ads_gmv_1m;

CREATE TABLE IF NOT EXISTS default.ads_gmv_1m
(
    stat_time DateTime64(3),
    gmv       Decimal(16, 2)
)
    ENGINE = MergeTree()
        ORDER BY stat_time
        SETTINGS index_granularity = 8192;