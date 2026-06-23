CREATE DATABASE IF NOT EXISTS `points_dapp` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `points_dapp`;

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '角色: 0=管理员 1=商户',
    `blockchain_address` VARCHAR(64) DEFAULT NULL COMMENT '区块链账户地址',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=待审核 1=正常 2=禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户表';

DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
    `merchant_name` VARCHAR(100) NOT NULL COMMENT '商户名称',
    `merchant_no` VARCHAR(32) NOT NULL COMMENT '商户编号(M前缀+日期+序号)',
    `contact_person` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '商户地址',
    `business_type` VARCHAR(30) DEFAULT NULL COMMENT 'catering/retail/service/other',
    `description` TEXT DEFAULT NULL COMMENT '商户描述',
    `logo_url` VARCHAR(500) DEFAULT NULL COMMENT 'Logo图片URL',
    `chain_address` VARCHAR(64) DEFAULT NULL COMMENT '链上积分账户地址',
    `points_balance` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT 'MySQL缓存余额',
    `total_issued` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '累计发行积分总额',
    `total_consumed` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费积分总额',
    `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态: 0待审 1通过 2拒绝',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '营业状态: 0停用 1营业',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_merchant_no` (`merchant_no`),
    KEY `idx_merchant_name` (`merchant_name`),
    KEY `idx_chain_address` (`chain_address`),
    CONSTRAINT `fk_merchant_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商户信息表';

DROP TABLE IF EXISTS `points_transaction`;
CREATE TABLE `points_transaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `tx_no` VARCHAR(40) NOT NULL COMMENT 'TX前缀+时间戳+随机码',
    `tx_type` TINYINT NOT NULL COMMENT '交易类型: 1发行 2转出 3转入 4消费',
    `from_merchant_id` BIGINT DEFAULT NULL COMMENT '转出方商户ID(发行时NULL)',
    `to_merchant_id` BIGINT NOT NULL COMMENT '接收方商户ID',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '交易金额(正数)',
    `balance_after` DECIMAL(18,2) DEFAULT NULL COMMENT '交易后余额快照',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `tx_hash` VARCHAR(128) DEFAULT NULL COMMENT '链上交易Hash',
    `block_number` BIGINT DEFAULT NULL COMMENT '区块高度',
    `chain_status` TINYINT NOT NULL DEFAULT 0 COMMENT '链状态: 0待上链 1已确认 2失败',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tx_no` (`tx_no`),
    KEY `idx_from_merchant` (`from_merchant_id`),
    KEY `idx_to_merchant` (`to_merchant_id`),
    KEY `idx_tx_hash` (`tx_hash`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_tx_type` (`tx_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='积分交易流水表';

DROP TABLE IF EXISTS `points_issue`;
CREATE TABLE `points_issue` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `issue_no` VARCHAR(40) NOT NULL COMMENT 'IS前缀+时间戳+随机码',
    `merchant_id` BIGINT NOT NULL COMMENT '目标商户ID',
    `amount` DECIMAL(18,2) NOT NULL COMMENT '发行金额',
    `reason` VARCHAR(500) NOT NULL COMMENT '发行事由',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审批管理员ID',
    `tx_hash` VARCHAR(128) DEFAULT NULL COMMENT '链上交易Hash',
    `block_number` BIGINT DEFAULT NULL COMMENT '区块高度',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待审批 1已发 2已拒',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_issue_no` (`issue_no`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='积分发行记录表';

INSERT INTO `sys_user` (`username`, `password`, `real_name`, `phone`, `email`, `role`, `status`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800138000', 'admin@points-dapp.com', 0, 1);