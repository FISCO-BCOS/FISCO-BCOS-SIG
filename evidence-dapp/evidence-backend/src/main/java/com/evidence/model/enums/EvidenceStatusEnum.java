package com.evidence.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EvidenceStatusEnum {

    PENDING(0, "待上链"),
    ON_CHAIN(1, "已上链"),
    FAILED(2, "失败");

    private final int code;
    private final String desc;
}
