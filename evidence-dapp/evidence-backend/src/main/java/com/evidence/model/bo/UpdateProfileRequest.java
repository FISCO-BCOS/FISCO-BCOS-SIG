package com.evidence.model.bo;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String realName;
    private String phone;
    private String email;
    private String address;
}
