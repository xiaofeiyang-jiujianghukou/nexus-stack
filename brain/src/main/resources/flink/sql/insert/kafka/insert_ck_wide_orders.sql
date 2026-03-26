INSERT INTO ck_wide_orders
SELECT
    o.order_id,
    o.user_id,
    o.amount,
    COALESCE(u.level, 'NORMAL') AS level,
    COALESCE(m.is_member, 0) AS is_member,
    o.ts,
    o.ts AS version
FROM mysql_orders o
         LEFT JOIN mysql_users AS u
                   ON o.user_id = u.user_id
         LEFT JOIN mysql_members AS m
                   ON o.user_id = m.user_id;