package com.evidence.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerifyVO {

    private Boolean isExist;
    private String matchResult;
    private LocalDateTime verifyTime;
    private EvidenceVO evidenceInfo;
}
