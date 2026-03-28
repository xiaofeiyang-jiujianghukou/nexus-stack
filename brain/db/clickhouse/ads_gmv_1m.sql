drop table if exists default.ads_gmv_1m;

create table default.ads_gmv_1m
(
    stat_time DateTime64(3),
    gmv       Decimal(16, 2)
)
    engine = MergeTree ORDER BY stat_time
        SETTINGS index_granularity = 8192;

