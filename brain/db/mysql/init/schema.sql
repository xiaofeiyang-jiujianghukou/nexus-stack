DROP TABLE IF EXISTS `members`;
CREATE TABLE `members` (
                           `user_id` bigint NOT NULL COMMENT '用户ID',
                           `is_member` tinyint DEFAULT NULL COMMENT '是否会员: 1是 0否',
                           PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
                          `order_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                          `user_id` bigint DEFAULT NULL COMMENT '下单用户ID',
                          `amount` double DEFAULT NULL COMMENT '订单金额',
                          `ts` bigint DEFAULT NULL COMMENT '订单创建时间',
                          PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=544 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
                         `user_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
                         `level` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级: VIP | NORMAL',
                         `register_time` bigint NOT NULL COMMENT '注册时间',
                         PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1186 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;