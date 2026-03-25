-- ====================== 一键部署 ======================
SET GLOBAL event_scheduler = ON;

DROP PROCEDURE IF EXISTS toggle_member_status;
DROP EVENT IF EXISTS evt_toggle_member;

DELIMITER //

CREATE PROCEDURE toggle_member_status()
BEGIN
    DECLARE v_user_id BIGINT;
    DECLARE v_current  TINYINT;

SELECT user_id, is_member
INTO v_user_id, v_current
FROM members
ORDER BY RAND()
    LIMIT 1;

UPDATE members
SET is_member = 1 - v_current
WHERE user_id = v_user_id;

-- 输出日志，便于观察
SELECT CONCAT('[', NOW(), '] 用户 ', v_user_id,
              ' 会员状态切换为: ', IF(1-v_current=1, '会员', '非会员')) AS action_log;
END //

DELIMITER ;

CREATE EVENT evt_toggle_member
ON SCHEDULE EVERY 10 SECOND
DO
    CALL toggle_member_status();

-- 查看事件状态
SHOW EVENTS LIKE 'evt_toggle_member';