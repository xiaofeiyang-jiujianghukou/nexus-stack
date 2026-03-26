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