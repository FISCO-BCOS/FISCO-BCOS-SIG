package com.trace.model.bo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TraceProcessingDTO {
    private Long productId;
    private Long processorId;
    private String inputBatchNo;
    private String outputBatchNo;
    private List<Map<String, Object>> processSteps;
}