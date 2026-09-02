package com.trace.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class TracePlanting implements Serializable {
    private Long id;
    private String plantingNo;
    private Long productId;
    private Long farmerId;
    private String baseName;
    private String baseAddress;
    private BigDecimal areaSize;
    private String cropVariety;
    private String sowDate;
    private BigDecimal expectedYield;
    private String farmOps;
    private String txHash;
    private BigInteger blockNumber;
    private Integer chainStatus;
    private LocalDateTime createTime;
}