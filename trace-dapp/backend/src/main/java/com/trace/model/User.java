package com.trace.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Integer role;
    private String organization;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}