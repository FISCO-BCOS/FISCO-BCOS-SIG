package com.points.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {
    ADMIN(0, "管理员"),
    MERCHANT(1, "商户");

    private final int code;
    private final String desc;

    UserRoleEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}