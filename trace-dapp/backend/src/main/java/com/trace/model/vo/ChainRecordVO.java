package com.trace.model.vo;

import lombok.Data;

@Data
public class ChainRecordVO {
    private String recordType;
    private String recordNo;
    private String txHash;
    private Integer chainStatus;
    private String createTime;
}