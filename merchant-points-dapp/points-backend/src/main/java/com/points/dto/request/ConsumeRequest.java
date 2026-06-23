package com.points.dto.request;

import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
public class ConsumeRequest {
    @NotBlank(message = "消费者手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String consumerPhone;
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额最小0.01")
    private BigDecimal amount;
    private String remark;
}
