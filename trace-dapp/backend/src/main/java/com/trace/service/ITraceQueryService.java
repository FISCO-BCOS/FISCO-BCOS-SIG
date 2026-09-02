package com.trace.service;

import com.trace.model.Result;
import com.trace.model.vo.TraceQueryResultVO;

public interface ITraceQueryService {
    Result<TraceQueryResultVO> queryByProductNo(String productNoOrQrcode);
    Result<TraceQueryResultVO> queryById(Long productId);
}