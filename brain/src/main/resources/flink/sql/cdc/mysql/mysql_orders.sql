CREATE TABLE mysql_orders (
                              order_id    BIGINT,
                              user_id     BIGINT,
                              amount      DOUBLE,
                              ts          BIGINT,
                              proc_time   AS PROCTIME(),
                              row_time    AS TO_TIMESTAMP(FROM_UNIXTIME(ts / 1000)),
                              WATERMARK FOR row_time AS row_time - INTERVAL '5' SECOND
) WITH (
      'connector' = 'mysql-cdc',                    -- ← 必须改成 mysql-cdc
      'hostname' = '127.0.0.1',
      'port' = '3306',
      'username' = 'xiaofeiyang',
      'password' = 'xfy@930112',
      'database-name' = 'brain_db',
      'table-name' = 'orders',
      'server-time-zone' = 'Asia/Shanghai',
      'scan.startup.mode' = 'latest-offset',        -- 生产建议用 'initial' 首次全量
      'scan.incremental.snapshot.enabled' = 'true'
      );