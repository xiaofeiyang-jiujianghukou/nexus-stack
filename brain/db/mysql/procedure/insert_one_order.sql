-- ====================== 2. 订单生成部分 ======================
DROP PROCEDURE IF EXISTS insert_one_order;
DROP EVENT IF EXISTS evt_generate_order;

DELIMITER //

CREATE PROCEDURE insert_one_order()
BEGIN
    DECLARE v_user_id BIGINT;
    DECLARE v_amount  DOUBLE;
    DECLARE v_ts      BIGINT;

    -- 随机从已有用户中挑选一个
SELECT user_id INTO v_user_id
FROM users
ORDER BY RAND()
    LIMIT 1;

SET v_amount = ROUND(10 + RAND() * 990, 2);   -- 10 ~ 999.99 元
    SET v_ts = UNIX_TIMESTAMP() * 1000;

INSERT INTO orders (user_id, amount, ts)
VALUES (v_user_id, v_amount, v_ts);

-- 日志
SELECT CONCAT('[', FROM_UNIXTIME(v_ts/1000), '] 新订单生成 → user_id=', v_user_id, ', amount=', v_amount) AS action_log;
END //

DELIMITER ;

CREATE EVENT evt_generate_order
ON SCHEDULE EVERY 1 SECOND
DO
    CALL insert_one_order();