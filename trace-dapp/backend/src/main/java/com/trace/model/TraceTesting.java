package com.trace.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class TraceTesting implements Serializable {
    private Long id;
    private String testingNo;
    private Long productId;
    private Long testerId;
    private String testItems;
    private Integer testResult;
    private String reportFileUrl;
    private String testDate;
    private String txHash;
    private BigInteger blockNumber;
    private Integer chainStatus;
    private LocalDateTime createTime;
}