package com.evidence.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserTypeEnum {

    PERSONAL(0, "个人"),
    ENTERPRISE(1, "企业"),
    INSTITUTION(2, "机构");

    private final int code;
    private final String desc;
}
