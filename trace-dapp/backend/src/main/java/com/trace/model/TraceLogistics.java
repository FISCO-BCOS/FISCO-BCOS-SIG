package com.trace.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class TraceLogistics implements Serializable {
    private Long id;
    private String logisticsNo;
    private Long productId;
    private Long logisticsProviderId;
    private String transportMode;
    private String vehicleInfo;
    private String logisticsNodes;
    private String txHash;
    private BigInteger blockNumber;
    private Integer chainStatus;
    private LocalDateTime createTime;
}