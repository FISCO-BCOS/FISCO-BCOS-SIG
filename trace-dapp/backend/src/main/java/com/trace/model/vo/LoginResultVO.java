package com.trace.model.vo;

import lombok.Data;

@Data
public class LoginResultVO {
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserVO user;
}