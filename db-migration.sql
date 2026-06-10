-- =========================================
-- 云外卖项目 - 数据库变更脚本
-- 执行方式：在 cloud_takeout 数据库中执行
-- =========================================

USE cloud_takeout;

-- ===== User 表新增字段 =====
-- 注意：MySQL不支持 ADD COLUMN IF NOT EXISTS，使用存储过程安全添加
-- 如果字段已存在则跳过，不存在则添加
SET @sql_nickname = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN `nickname` VARCHAR(50) DEFAULT NULL COMMENT ''昵称''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'nickname');
PREPARE stmt FROM @sql_nickname; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_email = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT ''邮箱''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'email');
PREPARE stmt FROM @sql_email; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_gender = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN `gender` VARCHAR(10) DEFAULT ''保密'' COMMENT ''性别''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'gender');
PREPARE stmt FROM @sql_gender; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_birthday = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN `birthday` VARCHAR(20) DEFAULT NULL COMMENT ''出生日期''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'birthday');
PREPARE stmt FROM @sql_birthday; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_avatar = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) DEFAULT NULL COMMENT ''头像URL''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'avatar');
PREPARE stmt FROM @sql_avatar; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_role = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) DEFAULT ''user'' COMMENT ''角色: user/merchant/admin/rider''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'user' AND COLUMN_NAME = 'role');
PREPARE stmt FROM @sql_role; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ===== Order 表新增字段 =====
SET @sql_order_receiver = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `order` ADD COLUMN `receiver` VARCHAR(50) DEFAULT NULL COMMENT ''收货人''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'order' AND COLUMN_NAME = 'receiver');
PREPARE stmt FROM @sql_order_receiver; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_order_address = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `order` ADD COLUMN `address` VARCHAR(255) DEFAULT NULL COMMENT ''收货地址''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'order' AND COLUMN_NAME = 'address');
PREPARE stmt FROM @sql_order_address; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_order_items = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `order` ADD COLUMN `items` TEXT DEFAULT NULL COMMENT ''订单菜品明细JSON''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'order' AND COLUMN_NAME = 'items');
PREPARE stmt FROM @sql_order_items; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_order_remark = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `order` ADD COLUMN `remark` VARCHAR(255) DEFAULT NULL COMMENT ''备注''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'order' AND COLUMN_NAME = 'remark');
PREPARE stmt FROM @sql_order_remark; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ===== 07-4 新增：Order 表增加 shop_id =====
SET @sql_order_shop_id = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `order` ADD COLUMN `shop_id` BIGINT DEFAULT NULL COMMENT ''店铺ID''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'order' AND COLUMN_NAME = 'shop_id');
PREPARE stmt FROM @sql_order_shop_id; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_order_shop_idx = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `order` ADD INDEX `idx_shop_id` (`shop_id`)')
FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'order' AND INDEX_NAME = 'idx_shop_id');
PREPARE stmt FROM @sql_order_shop_idx; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ===== 07-4 新增：dish 表增加 shop_id =====
SET @sql_dish_shop_id = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `dish` ADD COLUMN `shop_id` BIGINT DEFAULT NULL COMMENT ''店铺ID''')
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'dish' AND COLUMN_NAME = 'shop_id');
PREPARE stmt FROM @sql_dish_shop_id; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql_dish_shop_idx = (SELECT IF(
    COUNT(*) > 0, 'SELECT 1',
    'ALTER TABLE `dish` ADD INDEX `idx_shop_id` (`shop_id`)')
FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = 'cloud_takeout' AND TABLE_NAME = 'dish' AND INDEX_NAME = 'idx_shop_id');
PREPARE stmt FROM @sql_dish_shop_idx; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ===== 07-4 新增：创建店铺表 =====
CREATE TABLE IF NOT EXISTS `shop` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `logo` VARCHAR(255) DEFAULT NULL COMMENT '店铺Logo URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '店铺地址',
  `open_time` VARCHAR(10) DEFAULT '09:00' COMMENT '营业开始时间',
  `close_time` VARCHAR(10) DEFAULT '22:00' COMMENT '营业结束时间',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '店铺简介',
  `user_id` BIGINT NOT NULL COMMENT '店主用户ID（关联user表）',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1=营业中, 0=已关闭',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE INDEX `uk_user_id` (`user_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- ===== 创建积分日志表 =====
CREATE TABLE IF NOT EXISTS `points_log` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `order_id` BIGINT DEFAULT NULL COMMENT '关联订单ID',
  `order_no` VARCHAR(50) DEFAULT NULL COMMENT '订单号',
  `points` INT NOT NULL COMMENT '积分变动(正=获得,负=扣除)',
  `type` VARCHAR(50) NOT NULL COMMENT '类型: ORDER_PAID/ORDER_CANCELLED',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '说明',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分日志表';

-- ===== 插入/更新演示数据 =====
-- 【修复】使用 ON DUPLICATE KEY UPDATE 确保已存在的用户也能正确更新角色
-- 这解决了 INSERT IGNORE 导致已存在用户 role 无法更新为 merchant/admin 的问题

-- 管理员账号（密码: admin123 的MD5）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`)
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 'admin', '13800000000')
ON DUPLICATE KEY UPDATE
    `role` = VALUES(`role`),
    `nickname` = VALUES(`nickname`);

-- 商家账号（密码: merchant123 的MD5）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`)
VALUES ('merchant', 'a52f2c0dbf38ade4f715e02c7124046e', '美食商家', 'merchant', '13900000001')
ON DUPLICATE KEY UPDATE
    `role` = VALUES(`role`),
    `nickname` = VALUES(`nickname`);
