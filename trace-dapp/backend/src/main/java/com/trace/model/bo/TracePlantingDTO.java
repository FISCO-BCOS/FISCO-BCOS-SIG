package com.trace.model.bo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class TracePlantingDTO {
    private Long productId;
    private Long farmerId;
    private String baseName;
    private String baseAddress;
    private BigDecimal areaSize;
    private String cropVariety;
    private String sowDate;
    private BigDecimal expectedYield;
    private List<Map<String, Object>> farmOps;
}