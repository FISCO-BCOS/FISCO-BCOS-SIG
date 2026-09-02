package com.evidence.model.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserInfo user;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String avatarUrl;
        private Integer userType;
    }
}
