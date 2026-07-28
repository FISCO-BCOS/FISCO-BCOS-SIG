package com.trace.model.vo;

import lombok.Data;

@Data
public class CreateProductResultVO {
    private Long productId;
    private String productNo;
    private String traceQrcode;
    private String qrcodeBase64;
}