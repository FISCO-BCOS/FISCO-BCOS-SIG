package com.trace.service;

import com.trace.model.Result;
import org.springframework.web.multipart.MultipartFile;

public interface ITraceTestingService {
    Result<String> recordTesting(Long productId, String testItems, Integer testResult, String testDate, MultipartFile reportFile, String username);
}