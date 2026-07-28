package com.trace.model;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class TraceScanRecord implements Serializable {
    private Long id;
    private Long productId;
    private Integer scanType;
    private String scannerInfo;
    private String scanTime;
    private Integer verifyResult;
    private LocalDateTime createTime;
}