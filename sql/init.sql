-- =========================================
-- 云外卖平台 - 数据库初始化脚本
-- Docker 启动时自动执行
-- =========================================
SET NAMES utf8mb4;

-- ===== 用户表 =====
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(MD5加密)',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `gender`      VARCHAR(10)  DEFAULT '保密' COMMENT '性别',
    `birthday`    VARCHAR(20)  DEFAULT NULL COMMENT '出生日期',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `role`        VARCHAR(20)  DEFAULT 'user' COMMENT '角色: user/merchant/admin/rider',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX `uk_username` (`username`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ===== 店铺表 =====
CREATE TABLE IF NOT EXISTS `shop` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(100) NOT NULL COMMENT '店铺名称',
    `logo`        VARCHAR(255) DEFAULT NULL COMMENT '店铺Logo URL',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `address`     VARCHAR(255) DEFAULT NULL COMMENT '店铺地址',
    `open_time`   VARCHAR(10)  DEFAULT '09:00' COMMENT '营业开始时间',
    `close_time`  VARCHAR(10)  DEFAULT '22:00' COMMENT '营业结束时间',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '店铺简介',
    `user_id`     BIGINT       NOT NULL COMMENT '店主用户ID（关联user表）',
    `status`      TINYINT      DEFAULT 1 COMMENT '状态: 1=营业中, 0=已关闭',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX `uk_user_id` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺表';

-- ===== 菜品表 =====
CREATE TABLE IF NOT EXISTS `dish` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name`        VARCHAR(100)   NOT NULL COMMENT '菜品名称',
    `price`       DECIMAL(10,2)  NOT NULL COMMENT '价格',
    `description` VARCHAR(500)   DEFAULT NULL COMMENT '描述',
    `image`       VARCHAR(255)   DEFAULT NULL COMMENT '图片URL',
    `status`      INT            DEFAULT 1 COMMENT '状态: 1=在售, 0=下架',
    `category_id` BIGINT         DEFAULT NULL COMMENT '分类ID',
    `sales`       INT            DEFAULT 0 COMMENT '销量',
    `is_hot`      INT            DEFAULT 0 COMMENT '是否热门: 1=是, 0=否',
    `shop_id`     BIGINT         DEFAULT NULL COMMENT '归属店铺ID',
    `create_time` DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_shop_id` (`shop_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- ===== 订单表 =====
CREATE TABLE IF NOT EXISTS `order` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no`      VARCHAR(50)    NOT NULL COMMENT '订单号',
    `user_id`       BIGINT         NOT NULL COMMENT '用户ID',
    `user_name`     VARCHAR(50)    DEFAULT NULL COMMENT '用户名',
    `user_phone`    VARCHAR(20)    DEFAULT NULL COMMENT '用户电话',
    `total_amount`  DECIMAL(10,2)  NOT NULL COMMENT '订单总金额',
    `shop_id`       BIGINT         DEFAULT NULL COMMENT '店铺ID',
    `status`        INT            DEFAULT 0 COMMENT '状态: 0=待支付, 1=已支付, 2=已取消, 3=已完成',
    `receiver`      VARCHAR(50)    DEFAULT NULL COMMENT '收货人',
    `address`       VARCHAR(255)   DEFAULT NULL COMMENT '收货地址',
    `remark`        VARCHAR(255)   DEFAULT NULL COMMENT '备注',
    `items`         TEXT           DEFAULT NULL COMMENT '订单菜品明细JSON',
    `create_time`   DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `pay_time`      DATETIME       DEFAULT NULL COMMENT '支付时间',
    `cancel_time`   DATETIME       DEFAULT NULL COMMENT '取消时间',
    `cancel_reason` VARCHAR(255)   DEFAULT NULL COMMENT '取消原因',
    UNIQUE INDEX `uk_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_shop_id` (`shop_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ===== 积分日志表 =====
CREATE TABLE IF NOT EXISTS `points_log` (
    `id`          BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `order_id`    BIGINT       DEFAULT NULL COMMENT '关联订单ID',
    `order_no`    VARCHAR(50)  DEFAULT NULL COMMENT '订单号',
    `points`      INT          NOT NULL COMMENT '积分变动(正=获得,负=扣除)',
    `type`        VARCHAR(50)  NOT NULL COMMENT '类型: ORDER_PAID/ORDER_CANCELLED',
    `remark`      VARCHAR(255) DEFAULT NULL COMMENT '说明',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分日志表';

-- =========================================
-- 演示数据
-- =========================================

-- 管理员账号（密码: admin123）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`)
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 'admin', '13800000000')
ON DUPLICATE KEY UPDATE `role` = VALUES(`role`), `nickname` = VALUES(`nickname`);

-- 商家账号（密码: merchant123）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `phone`)
VALUES ('merchant', 'a52f2c0dbf38ade4f715e02c7124046e', '美食商家', 'merchant', '13900000001')
ON DUPLICATE KEY UPDATE `role` = VALUES(`role`), `nickname` = VALUES(`nickname`);

-- 商家店铺
INSERT INTO `shop` (`name`, `phone`, `address`, `description`, `user_id`, `status`)
SELECT '美食商家总店', '13900000001', '北京市朝阳区xxx路88号', '提供各类美食外卖服务', id, 1
FROM `user` WHERE username = 'merchant'
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
