/*
 Navicat Premium Dump SQL

 Source Server         : mysql-server
 Source Server Type    : MySQL
 Source Server Version : 90300 (9.3.0)
 Source Host           : 127.0.0.1:3306
 Source Schema         : brain_db

 Target Server Type    : MySQL
 Target Server Version : 90300 (9.3.0)
 File Encoding         : 65001

 Date: 26/03/2026 10:42:59
*/
CREATE DATABASE IF NOT EXISTS brain_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE brain_db;

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
  `amount` double DEFAULT NULL COMMENT '订单金额',
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
-- Procedure structure for insert_one_order
-- ----------------------------
DROP PROCEDURE IF EXISTS `insert_one_order`;
delimiter ;;
CREATE PROCEDURE `insert_one_order`()
BEGIN
    DECLARE v_user_id BIGINT;
    DECLARE v_amount  DOUBLE;

    SELECT user_id INTO v_user_id 
    FROM users 
    ORDER BY RAND() LIMIT 1;

    SET v_amount = ROUND(10 + RAND() * 990, 2);

    INSERT INTO orders (user_id, amount, ts)
    VALUES (v_user_id, v_amount, UNIX_TIMESTAMP() * 1000);
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
EVERY '2' MINUTE STARTS '2026-03-25 17:01:41'
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
EVERY '3' MINUTE STARTS '2026-03-25 17:01:41'
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
EVERY '5' MINUTE STARTS '2026-03-25 17:01:41'
DO CALL toggle_member_status()
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
