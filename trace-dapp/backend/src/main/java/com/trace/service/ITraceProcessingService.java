package com.trace.service;

import com.trace.model.Result;
import com.trace.model.bo.TraceProcessingDTO;

public interface ITraceProcessingService {
    Result<String> recordProcessing(TraceProcessingDTO dto, String username);
}