package com.trace.model;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class Product implements Serializable {
    private Long id;
    private String productNo;
    private String productName;
    private String category;
    private String description;
    private String images;
    private BigDecimal price;
    private String unit;
    private String originAddress;
    private Long farmerId;
    private Long processorId;
    private Long testerId;
    private Long logisticsId;
    private String traceQrcode;
    private String txHash;
    private BigInteger blockNumber;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}