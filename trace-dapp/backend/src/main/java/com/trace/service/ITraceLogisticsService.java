package com.trace.service;

import com.trace.model.Result;
import com.trace.model.bo.TraceLogisticsDTO;

public interface ITraceLogisticsService {
    Result<String> recordLogistics(TraceLogisticsDTO dto, String username);
}