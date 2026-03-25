SET GLOBAL event_scheduler = ON;

-- ====================== 1. 用户生成部分 ======================
DROP PROCEDURE IF EXISTS insert_one_user;
DROP EVENT IF EXISTS evt_generate_user;

DELIMITER //

CREATE PROCEDURE insert_one_user()
BEGIN
    DECLARE v_user_id BIGINT;
    DECLARE v_ts      BIGINT;

    SET v_ts = UNIX_TIMESTAMP() * 1000;   -- 毫秒级时间戳

    -- 插入用户
    INSERT INTO users (level, register_time)
    VALUES ('NORMAL', v_ts);

    SET v_user_id = LAST_INSERT_ID();

    -- 插入 members 表（默认不是会员）
    INSERT INTO members (user_id, is_member)
    VALUES (v_user_id, 0);

    -- 日志
    SELECT CONCAT('[', FROM_UNIXTIME(v_ts/1000), '] 新用户生成 → user_id=', v_user_id) AS action_log;
END //

DELIMITER ;

CREATE EVENT evt_generate_user
ON SCHEDULE EVERY 1 MINUTE
DO
    CALL insert_one_user();

CREATE EVENT evt_generate_user
ON SCHEDULE EVERY 10 MINUTE
DO
    CALL insert_one_order();

CREATE EVENT evt_toggle_member
ON SCHEDULE EVERY 10 MINUTE
DO
    CALL toggle_member_status();