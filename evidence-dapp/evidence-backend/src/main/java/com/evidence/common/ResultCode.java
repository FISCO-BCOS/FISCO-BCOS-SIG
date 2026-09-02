package com.evidence.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "账户禁用"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "资源已存在"),
    PAYLOAD_TOO_LARGE(413, "文件过大"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    CHAIN_UNAVAILABLE(503, "区块链服务不可用");

    private final int code;
    private final String message;
}
