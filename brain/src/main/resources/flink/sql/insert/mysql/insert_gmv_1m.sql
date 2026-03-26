INSERT INTO ads_gmv_1m
SELECT
    TUMBLE_START(row_time, INTERVAL '10' SECOND) AS stat_time,
    ROUND(SUM(amount), 2)                                   AS gmv
FROM mysql_orders
GROUP BY TUMBLE(row_time, INTERVAL '10' SECOND);