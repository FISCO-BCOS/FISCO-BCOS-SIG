package com.trace.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class ChainApprovalRequest implements Serializable {
    private Long id;
    private Long productId;
    private String productNo;
    private Long userId;
    private Integer userRole;
    private String userName;
    /** 0=待审批, 1=已通过, 2=已拒绝 */
    private Integer status;
    private String rejectReason;
    /** 角色前置校验结果 */
    private String validationMsg;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime approveTime;
    private Long approverId;
}
