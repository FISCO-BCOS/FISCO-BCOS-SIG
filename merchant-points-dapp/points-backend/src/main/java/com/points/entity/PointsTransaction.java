package com.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("points_transaction")
public class PointsTransaction implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String txNo;
    private Integer txType;
    private Long fromMerchantId;
    private Long toMerchantId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String remark;
    private String txHash;
    private Long blockNumber;
    private Integer chainStatus;
    private Long operatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
