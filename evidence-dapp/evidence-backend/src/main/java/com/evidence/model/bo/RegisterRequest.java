package com.evidence.model.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{6,20}$", message = "密码需含字母和数字，6-20位")
    private String password;

    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    private String realName;
    private String phone;
    private String email;

    @NotNull(message = "用户类型不能为空")
    private Integer userType;
}
