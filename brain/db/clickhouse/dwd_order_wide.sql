
drop table default.dwd_order_wide;

CREATE TABLE default.dwd_order_wide (
                                         order_id   UInt64,
                                         user_id    String,
                                         amount     Float64,
                                         level      String,
                                         is_member  Int32,
                                         proc_time  DateTime64(3) DEFAULT now64()   -- version 列
) ENGINE = ReplacingMergeTree(proc_time)      -- 用 proc_time 作为 version
      ORDER BY order_id                                -- 必须和 Flink 主键一致
      PRIMARY KEY order_id
      SETTINGS index_granularity = 8192;