package com.evidence.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evidence_record")
public class EvidenceRecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String evidenceNo;
    private String workTitle;
    private String workDesc;
    private String workCategory;
    private String workHash;
    private String txHash;
    private Long blockNumber;
    private String contractAddress;
    private LocalDateTime evidenceTime;
    private Integer verifyCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
