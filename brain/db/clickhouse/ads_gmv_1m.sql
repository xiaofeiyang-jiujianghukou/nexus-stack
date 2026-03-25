drop table default.ads_gmv_1m;

CREATE TABLE default.ads_gmv_1m (
                                     stat_time DateTime64(3),
                                     gmv       Float64
) ENGINE = ReplacingMergeTree()
ORDER BY stat_time;