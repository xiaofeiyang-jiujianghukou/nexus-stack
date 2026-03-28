CREATE TABLE IF NOT EXISTS mysql_members (
                                             user_id     BIGINT,
                                             is_member   INT,
                                             PRIMARY KEY (user_id) NOT ENFORCED
    ) WITH (
          'connector' = 'jdbc',
          'url' = 'jdbc:mysql://127.0.0.1:33306/brain_db?useSSL=false&connectionTimeZone=UTC&allowPublicKeyRetrieval=true',
          'username' = 'root',
          'password' = '123456',
          'driver' = 'com.mysql.cj.jdbc.Driver',
          'table-name' = 'members',
          'lookup.cache' = 'PARTIAL',
          'lookup.partial-cache.max-rows' = '20000',
          'lookup.partial-cache.expire-after-access' = '5s',
          'lookup.max-retries' = '3'
          );

CREATE TABLE IF NOT EXISTS mysql_users (
                                           user_id        BIGINT,
                                           level         STRING,
                                           register_time  BIGINT,
                                           PRIMARY KEY (user_id) NOT ENFORCED
    ) WITH (
          'connector' = 'jdbc',
          'url' = 'jdbc:mysql://127.0.0.1:33306/brain_db?useSSL=false&connectionTimeZone=UTC&allowPublicKeyRetrieval=true',
          'username' = 'root',
          'password' = '123456',
          'driver' = 'com.mysql.cj.jdbc.Driver',
          'table-name' = 'users',
          'lookup.cache' = 'PARTIAL',
          'lookup.partial-cache.max-rows' = '20000',
          'lookup.partial-cache.expire-after-access' = '5s',
          'lookup.max-retries' = '3'
          );

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

CREATE TABLE ck_wide_orders (
                                order_id  BIGINT,
                                user_id   BIGINT,
                                amount    DECIMAL(10, 2),
                                `level`   STRING,
                                is_member INT,
                                ts        BIGINT,
                                PRIMARY KEY (order_id) NOT ENFORCED
) WITH (
      'connector' = 'jdbc',
      'url' = 'jdbc:clickhouse://127.0.0.1:38123/default',
      'username' = 'default',
      'password' = '123456',
      'table-name' = 'dwd_order_wide',
      'driver' = 'com.clickhouse.jdbc.ClickHouseDriver',
      'sink.buffer-flush.max-rows' = '5000',
      'sink.buffer-flush.interval' = '2s',
      'sink.max-retries' = '3'
      );

INSERT INTO ck_wide_orders
SELECT
    o.order_id                                   AS order_id,
    o.user_id                                    AS user_id,
    o.amount,
    COALESCE(u.level, 'NORMAL')                  AS level,
    COALESCE(m.is_member, 0)                     AS is_member,
    o.ts                                         AS ts

FROM mysql_orders o
         LEFT JOIN mysql_users /*+ LOOKUP('table'='mysql_users', 'cache'='NONE') */ AS u
                   ON o.user_id = u.user_id
         LEFT JOIN mysql_members /*+ LOOKUP('table'='mysql_members', 'cache'='NONE') */ AS m
                   ON o.user_id = m.user_id;