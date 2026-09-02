package com.evidence.model.vo;

import lombok.Data;

@Data
public class StatisticsVO {
    private Long totalEvidences;
    private Long monthlyNew;
    private Long totalVerifications;
    private Long latestBlockNumber;
}
