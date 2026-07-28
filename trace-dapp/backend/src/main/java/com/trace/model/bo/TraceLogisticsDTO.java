package com.trace.model.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TraceLogisticsDTO extends TraceRecordDTO {

    private String transportMode;

    private String vehicleInfo;

    private String logisticsNodes;
}