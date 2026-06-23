package com.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant")
public class Merchant implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String merchantName;
    private String merchantNo;
    private String contactPerson;
    private String contactPhone;
    private String address;
    private String businessType;
    private String description;
    private String logoUrl;
    private String chainAddress;
    private BigDecimal pointsBalance;
    private BigDecimal totalIssued;
    private BigDecimal totalConsumed;
    private Integer auditStatus;
    private Integer status;
    private String rejectReason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
