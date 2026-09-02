CREATE TABLE IF NOT EXISTS chain_approval_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_no VARCHAR(64) DEFAULT '' COMMENT '产品编号',
    user_id BIGINT DEFAULT NULL COMMENT '申请人用户ID',
    user_role INT DEFAULT 1 COMMENT '申请人角色:0管理员1农户2加工商3检测4物流',
    user_name VARCHAR(64) DEFAULT '' COMMENT '申请人用户名',
    status TINYINT DEFAULT 0 COMMENT '状态:0待审批1已通过2已拒绝',
    reject_reason VARCHAR(512) DEFAULT '' COMMENT '拒绝原因',
    validation_msg VARCHAR(1024) DEFAULT '' COMMENT '角色校验结果信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    approve_time DATETIME DEFAULT NULL COMMENT '审批时间',
    approver_id BIGINT DEFAULT NULL COMMENT '审批人ID',
    INDEX idx_product_id (product_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上链审批请求表';
