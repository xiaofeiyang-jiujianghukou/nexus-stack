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