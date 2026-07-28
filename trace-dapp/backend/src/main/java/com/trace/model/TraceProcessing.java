package com.trace.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class TraceProcessing implements Serializable {
    private Long id;
    private String processingNo;
    private Long productId;
    private Long processorId;
    private String inputBatchNo;
    private String outputBatchNo;
    private String processSteps;
    private String txHash;
    private BigInteger blockNumber;
    private Integer chainStatus;
    private LocalDateTime createTime;
}