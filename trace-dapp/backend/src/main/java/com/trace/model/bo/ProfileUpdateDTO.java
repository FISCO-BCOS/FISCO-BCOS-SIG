package com.trace.model.bo;

import lombok.Data;

@Data
public class ProfileUpdateDTO {
    private String realName;
    private String phone;
    private String organization;
}