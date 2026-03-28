CREATE TABLE IF NOT EXISTS mysql_orders (
                                            order_id    BIGINT,
                                            user_id     BIGINT,
                                            amount      DECIMAL(10, 2),
    ts          BIGINT,
    row_time AS TO_TIMESTAMP_LTZ(ts, 3),
    WATERMARK FOR row_time AS row_time - INTERVAL '5' SECOND,
    PRIMARY KEY (order_id) NOT ENFORCED
    ) WITH (
          'connector' = 'mysql-cdc',
          'hostname' = '${app.datasource.mysql.hostname}',
          'port' = '${app.datasource.mysql.port}',
          'username' = '${app.datasource.mysql.username}',
          'password' = '${app.datasource.mysql.password}',
          'database-name' = 'brain_db',
          'table-name' = 'orders',
          'server-time-zone' = 'UTC',
          'scan.startup.mode' = 'initial',
          'scan.incremental.snapshot.enabled' = 'true'
          );

CREATE TABLE IF NOT EXISTS ads_gmv_1m (
                                          stat_time   TIMESTAMP_LTZ(3),
    gmv         DECIMAL(10, 2)
    ) WITH (
          'connector' = 'jdbc',
          'url' = '${app.datasource.clickhouse.jdbc-url}',
          'table-name' = 'ads_gmv_1m',
          'username' = '${app.datasource.clickhouse.username}',
          'password' = '${app.datasource.clickhouse.password}',
          'driver' = 'com.clickhouse.jdbc.ClickHouseDriver',
          'sink.buffer-flush.max-rows' = '5000',
          'sink.buffer-flush.interval' = '2s',
          'sink.max-retries' = '3'
          );

INSERT INTO ads_gmv_1m
SELECT
    TUMBLE_START(row_time, INTERVAL '10' SECOND) AS stat_time,
    ROUND(SUM(amount), 2)                        AS gmv
FROM mysql_orders
GROUP BY TUMBLE(row_time, INTERVAL '10' SECOND);