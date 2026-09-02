package com.points.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private Integer role;
    private Long merchantId;
    private String merchantName;
    private String blockchainAddress;
}
