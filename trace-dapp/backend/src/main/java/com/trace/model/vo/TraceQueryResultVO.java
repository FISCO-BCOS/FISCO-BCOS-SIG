package com.trace.model.vo;

import com.trace.model.*;
import lombok.Data;
import java.util.List;

@Data
public class TraceQueryResultVO {
    private ProductVO product;
    private TracePlanting planting;
    private TraceProcessing processing;
    private TraceTesting testing;
    private TraceLogistics logistics;
    private Object chainData;
    private Boolean chainVerified;
    private Integer scanCount;
    private List<TraceScanRecord> scanRecords;
}