CREATE DATABASE IF NOT EXISTS evidence_dapp DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE evidence_dapp;

CREATE TABLE sys_user (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    id_card VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    address VARCHAR(255) DEFAULT NULL COMMENT '联系地址',
    avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    user_type TINYINT(4) NOT NULL DEFAULT 1 COMMENT '0=个人,1=企业,2=机构',
    status TINYINT(4) NOT NULL DEFAULT 1 COMMENT '0=禁用,1=正常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE evidence_record (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '存证记录ID',
    user_id BIGINT(20) NOT NULL COMMENT '存证人ID',
    evidence_no VARCHAR(32) NOT NULL COMMENT '存证编号',
    work_title VARCHAR(200) NOT NULL COMMENT '作品标题',
    work_desc TEXT DEFAULT NULL COMMENT '作品描述',
    work_category VARCHAR(20) NOT NULL COMMENT '作品分类',
    work_hash VARCHAR(64) NOT NULL COMMENT 'SHA256哈希值',
    tx_hash VARCHAR(66) DEFAULT NULL COMMENT '区块链交易Hash',
    block_number BIGINT(20) DEFAULT NULL COMMENT '区块高度',
    contract_address VARCHAR(42) DEFAULT NULL COMMENT '合约地址',
    evidence_time DATETIME NOT NULL COMMENT '存证时间',
    verify_count INT(11) NOT NULL DEFAULT 0 COMMENT '验证次数',
    status TINYINT(4) NOT NULL DEFAULT 0 COMMENT '0=待上链,1=已上链,2=失败',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_evidence_no (evidence_no),
    UNIQUE KEY uk_work_hash (work_hash),
    KEY idx_user_id (user_id),
    KEY idx_work_category (work_category),
    KEY idx_status (status),
    KEY idx_evidence_time (evidence_time),
    CONSTRAINT fk_evidence_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存证记录表';

CREATE TABLE work_file (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    evidence_id BIGINT(20) NOT NULL COMMENT '存证记录ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '服务器存储路径',
    file_size BIGINT(20) NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
    file_type VARCHAR(100) NOT NULL COMMENT 'MIME类型',
    file_hash VARCHAR(64) DEFAULT NULL COMMENT '文件SHA256哈希',
    upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (id),
    KEY idx_evidence_id (evidence_id),
    CONSTRAINT fk_file_evidence FOREIGN KEY (evidence_id) REFERENCES evidence_record(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作品文件表';

CREATE TABLE verify_record (
    id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '验证记录ID',
    evidence_id BIGINT(20) DEFAULT NULL COMMENT '存证记录ID',
    verifier_id BIGINT(20) NOT NULL COMMENT '验证人ID',
    verifier_name VARCHAR(50) DEFAULT NULL COMMENT '验证人名称',
    hash VARCHAR(64) NOT NULL COMMENT '验证的哈希值',
    is_exist TINYINT(1) DEFAULT 0 COMMENT '链上是否存在: 0=否,1=是',
    match_result VARCHAR(255) DEFAULT NULL COMMENT '匹配结果描述',
    verify_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '验证时间',
    PRIMARY KEY (id),
    KEY idx_evidence_id (evidence_id),
    KEY idx_verifier_id (verifier_id),
    KEY idx_verify_time (verify_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证记录表';
