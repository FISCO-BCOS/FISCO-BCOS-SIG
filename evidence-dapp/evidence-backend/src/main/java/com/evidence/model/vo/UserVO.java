package com.evidence.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String address;
    private String avatarUrl;
    private Integer userType;
    private Integer status;
    private LocalDateTime createTime;
}
