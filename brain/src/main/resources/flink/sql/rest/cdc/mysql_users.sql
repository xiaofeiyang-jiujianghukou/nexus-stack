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