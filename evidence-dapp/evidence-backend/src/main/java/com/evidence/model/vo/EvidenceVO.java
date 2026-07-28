package com.evidence.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvidenceVO {

    private Long id;
    private String evidenceNo;
    private String workTitle;
    private String workDesc;
    private String workCategory;
    private String workHash;
    private String txHash;
    private Long blockNumber;
    private LocalDateTime evidenceTime;
    private Integer verifyCount;
    private Integer status;
    private String userName;
    private Integer fileCount;
}
