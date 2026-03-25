INSERT INTO ck_wide_orders
SELECT
    o.orderId                                   AS order_id,
    o.userId                                    AS user_id,
    o.amount,
    COALESCE(u.level, 'NORMAL')                 AS level,
    COALESCE(m.is_member, 0)                    AS is_member,
    PROCTIME()                                  AS proc_time

FROM kafka_orders o
         LEFT JOIN mysql_users AS u
                   ON CAST(o.userId AS VARCHAR) = CAST(u.user_id AS VARCHAR)
         LEFT JOIN mysql_members AS m
                   ON CAST(o.userId AS VARCHAR) = CAST(m.user_id AS VARCHAR);