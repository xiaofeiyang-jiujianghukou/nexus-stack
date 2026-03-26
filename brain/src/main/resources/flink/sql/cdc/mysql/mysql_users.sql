CREATE TABLE IF NOT EXISTS mysql_users (
                             user_id        BIGINT,
                             level         STRING,
                             register_time  BIGINT,
                             ts            BIGINT,
                             PRIMARY KEY (user_id) NOT ENFORCED
) WITH (
        'connector' = 'jdbc',
        'url' = '${app.datasource.mysql.jdbc-url}',
        'username' = '${app.datasource.mysql.username}',
        'password' = '${app.datasource.mysql.password}',
        'driver' = 'com.mysql.cj.jdbc.Driver',
        'table-name' = 'users',
        --'lookup.cache' = 'NONE'
        'lookup.cache' = 'PARTIAL',
        'lookup.partial-cache.max-rows' = '20000',
        'lookup.partial-cache.expire-after-access' = '5s',
        'lookup.max-retries' = '3'
      )