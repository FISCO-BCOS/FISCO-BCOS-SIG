package com.points.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("points_issue")
public class PointsIssue implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String issueNo;
    private Long merchantId;
    private BigDecimal amount;
    private String reason;
    private Long approverId;
    private String txHash;
    private Long blockNumber;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime approveTime;
}
