package com.points.enums;

import lombok.Getter;

@Getter
public enum AuditStatusEnum {
    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final int code;
    private final String desc;

    AuditStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}