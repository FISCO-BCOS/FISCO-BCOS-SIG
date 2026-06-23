package com.points.enums;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USER_NOT_FOUND(401001, "用户名或密码错误"),
    USER_DISABLED(403001, "账户已被禁用"),
    USER_PENDING(403002, "账户待审核"),
    USERNAME_EXISTS(409001, "用户名已存在"),
    PHONE_EXISTS(409002, "手机号已注册"),

    MERCHANT_NOT_FOUND(404001, "商户不存在"),
    MERCHANT_NOT_AUDITED(403003, "商户尚未通过审核"),
    MERCHANT_AUDITED(409003, "该商户已审核过"),

    INSUFFICIENT_BALANCE(409010, "积分余额不足"),
    TRANSFER_SELF(400010, "不能向自己转账"),
    ISSUE_AMOUNT_INVALID(400011, "发行金额不合法"),

    CHAIN_ERROR(500001, "链上操作失败"),
    CHAIN_TIMEOUT(500002, "链上操作超时");

    private final int code;
    private final String message;

    ErrorCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}