package com.points.enums;

import lombok.Getter;

@Getter
public enum TxTypeEnum {
    ISSUE(1, "发行"),
    TRANSFER_OUT(2, "转出"),
    TRANSFER_IN(3, "转入"),
    CONSUME(4, "消费");

    private final int code;
    private final String desc;

    TxTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}