/*
 Navicat Premium Dump SQL

 Source Server         : nexus-stack
 Source Server Type    : MySQL
 Source Server Version : 80045 (8.0.45)
 Source Host           : 127.0.0.1:33306
 Source Schema         : brain_db

 Target Server Type    : MySQL
 Target Server Version : 80045 (8.0.45)
 File Encoding         : 65001

 Date: 29/03/2026 21:10:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for members
-- ----------------------------
DROP TABLE IF EXISTS `members`;
CREATE TABLE `members` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `is_member` tinyint DEFAULT NULL COMMENT '是否会员: 1是 0否',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint DEFAULT NULL COMMENT '下单用户ID',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '订单金额',
  `ts` bigint DEFAULT NULL COMMENT '订单创建时间',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `level` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级: VIP | NORMAL',
  `register_time` bigint NOT NULL COMMENT '注册时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Procedure structure for init_data
-- ----------------------------
DROP PROCEDURE IF EXISTS `init_data`;
delimiter ;;
CREATE PROCEDURE `init_data`()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_start_time DATETIME;
    DECLARE v_end_time DATETIME;
    
    SET v_start_time = NOW();
    SELECT CONCAT('[', v_start_time, '] 开始初始化数据...') AS action_log;
    
    -- 1. 先清空现有数据
    DELETE FROM orders;
    DELETE FROM members;
    DELETE FROM users;
    
    -- 2. 重置自增ID
    ALTER TABLE users AUTO_INCREMENT = 1;
    ALTER TABLE orders AUTO_INCREMENT = 1;
    
    -- 3. 循环生成 50 个用户
    WHILE i < 50 DO
        CALL insert_one_user();
        SET i = i + 1;
    END WHILE;
    
    SELECT CONCAT('✅ 已生成 ', i, ' 个用户') AS action_log;
    
    -- 4. 重置循环变量
    SET i = 0;
    
    -- 5. 循环执行 50 次会员状态切换（产生会员变化）
    WHILE i < 50 DO
        CALL toggle_member_status();
        SET i = i + 1;
    END WHILE;
    
    SELECT CONCAT('✅ 已执行 ', i, ' 次会员状态切换') AS action_log;
    
    -- 6. 循环生成 200 个订单（确保有足够数据）
    SET i = 0;
    WHILE i < 200 DO
        CALL insert_one_order();
        SET i = i + 1;
    END WHILE;
    
    SELECT CONCAT('✅ 已生成 ', i, ' 个订单') AS action_log;
    
    SET v_end_time = NOW();
    SELECT CONCAT('[', v_end_time, '] 数据初始化完成！总耗时: ', TIMESTAMPDIFF(SECOND, v_start_time, v_end_time), ' 秒') AS action_log;
    
    -- 7. 输出统计信息
    SELECT 
        (SELECT COUNT(*) FROM users) AS 用户总数,
        (SELECT COUNT(*) FROM members WHERE is_member = 1) AS 会员数,
        (SELECT COUNT(*) FROM orders) AS 订单总数;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insert_one_order
-- ----------------------------
DROP PROCEDURE IF EXISTS `insert_one_order`;
delimiter ;;
CREATE PROCEDURE `insert_one_order`()
BEGIN
    DECLARE v_user_id BIGINT;
    DECLARE v_amount  DOUBLE;
    DECLARE v_user_exists INT DEFAULT 0;

    -- 检查用户表是否有数据
    SELECT COUNT(*) INTO v_user_exists FROM users;
    
    IF v_user_exists > 0 THEN
        -- 随机选择一个用户
        SELECT user_id INTO v_user_id 
        FROM users 
        ORDER BY RAND() 
        LIMIT 1;
        
        IF v_user_id IS NOT NULL THEN
            SET v_amount = ROUND(10 + RAND() * 990, 2);
            
            INSERT INTO orders (user_id, amount, ts)
            VALUES (v_user_id, v_amount, UNIX_TIMESTAMP() * 1000);
        END IF;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for insert_one_user
-- ----------------------------
DROP PROCEDURE IF EXISTS `insert_one_user`;
delimiter ;;
CREATE PROCEDURE `insert_one_user`()
BEGIN
    DECLARE v_user_id BIGINT;
    DECLARE v_ts      BIGINT;
    DECLARE v_level   VARCHAR(10);

    SET v_ts = UNIX_TIMESTAMP() * 1000;   -- 毫秒级时间戳
    
    -- 随机等级：70% NORMAL，30% VIP
    IF RAND() < 0.3 THEN
        SET v_level = 'VIP';
    ELSE
        SET v_level = 'NORMAL';
    END IF;

    -- 插入用户
    INSERT INTO users (level, register_time)
    VALUES (v_level, v_ts);

    SET v_user_id = LAST_INSERT_ID();

    -- 插入 members 表（默认不是会员，如果是VIP可以随机决定是否会员）
    INSERT INTO members (user_id, is_member)
    VALUES (v_user_id, IF(v_level = 'VIP', IF(RAND() < 0.8, 1, 0), 0));

    -- 日志
    SELECT CONCAT('[', FROM_UNIXTIME(v_ts/1000), '] 新用户生成 → user_id=', v_user_id, ', level=', v_level) AS action_log;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for toggle_member_status
-- ----------------------------
DROP PROCEDURE IF EXISTS `toggle_member_status`;
delimiter ;;
CREATE PROCEDURE `toggle_member_status`()
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
		
		UPDATE users
		SET `level` = IF(v_current = 0, 'VIP', 'NORMAL')
		WHERE user_id = v_user_id;

    -- 输出日志，便于观察
    SELECT CONCAT('[', NOW(), '] 用户 ', v_user_id, 
                  ' 会员状态切换为: ', IF(1-v_current=1, '会员', '非会员')) AS action_log;
END
;;
delimiter ;

-- ----------------------------
-- Event structure for evt_generate_order
-- ----------------------------
DROP EVENT IF EXISTS `evt_generate_order`;
delimiter ;;
CREATE EVENT `evt_generate_order`
ON SCHEDULE
EVERY '5' SECOND STARTS '2026-03-25 17:01:41'
DO CALL insert_one_order()
;;
delimiter ;

-- ----------------------------
-- Event structure for evt_generate_user
-- ----------------------------
DROP EVENT IF EXISTS `evt_generate_user`;
delimiter ;;
CREATE EVENT `evt_generate_user`
ON SCHEDULE
EVERY '30' MINUTE STARTS '2026-03-25 17:01:41'
DO CALL insert_one_user()
;;
delimiter ;

-- ----------------------------
-- Event structure for evt_toggle_member
-- ----------------------------
DROP EVENT IF EXISTS `evt_toggle_member`;
delimiter ;;
CREATE EVENT `evt_toggle_member`
ON SCHEDULE
EVERY '10' MINUTE STARTS '2026-03-25 17:01:41'
DO CALL toggle_member_status()
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
