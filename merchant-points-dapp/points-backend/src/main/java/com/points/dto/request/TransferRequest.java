package com.points.dto.request;

import lombok.Data;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull(message = "收款商户ID不能为空")
    private Long toMerchantId;
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额最小0.01")
    private BigDecimal amount;
    private String remark;
}
