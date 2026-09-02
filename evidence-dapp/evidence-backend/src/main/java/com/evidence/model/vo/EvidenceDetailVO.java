package com.evidence.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EvidenceDetailVO {

    private Long id;
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
    private UserVO user;
    private List<WorkFileVO> files;
    private List<VerifyRecordVO> verifyRecords;

    @Data
    public static class WorkFileVO {
        private Long id;
        private String fileName;
        private String filePath;
        private Long fileSize;
        private String fileType;
        private String fileHash;
        private LocalDateTime uploadTime;
    }

    @Data
    public static class VerifyRecordVO {
        private Long id;
        private String verifierName;
        private Boolean isExist;
        private String matchResult;
        private LocalDateTime verifyTime;
    }
}
