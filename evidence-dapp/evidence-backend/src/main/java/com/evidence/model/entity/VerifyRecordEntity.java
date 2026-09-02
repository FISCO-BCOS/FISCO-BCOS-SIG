package com.evidence.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("verify_record")
public class VerifyRecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long evidenceId;
    private Long verifierId;
    private String verifierName;
    private String hash;
    private Boolean isExist;
    private String matchResult;
    private LocalDateTime verifyTime;
}
