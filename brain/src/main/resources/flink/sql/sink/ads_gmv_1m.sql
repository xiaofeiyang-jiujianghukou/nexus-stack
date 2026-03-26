CREATE TABLE IF NOT EXISTS ads_gmv_1m (
    stat_time   TIMESTAMP_LTZ(3),
    gmv         DECIMAL(10, 2)
) WITH (
  'connector' = 'clickhouse',
  'url' = '${app.datasource.clickhouse.jdbc-url}',
  'table-name' = 'ads_gmv_1m',
  'username' = '${app.datasource.clickhouse.username}',
  'password' = '${app.datasource.clickhouse.password}',
  'database-name' = 'default',
  'sink.batch-size' = '1000',
  'sink.flush-interval' = '1s',
  'sink.max-retries' = '3',
  'sink.parallelism' = '1',
  'sink.ignore-delete' = 'true'
);