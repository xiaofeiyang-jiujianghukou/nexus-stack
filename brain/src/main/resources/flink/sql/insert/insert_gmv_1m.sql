INSERT INTO ads_gmv_1m
SELECT
    TUMBLE_START(proc_time, INTERVAL '10' SECOND) AS stat_time,
    SUM(amount)                                   AS gmv
FROM mysql_orders
GROUP BY TUMBLE(proc_time, INTERVAL '10' SECOND);