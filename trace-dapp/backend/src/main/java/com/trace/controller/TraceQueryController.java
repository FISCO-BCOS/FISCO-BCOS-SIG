package com.trace.controller;

import com.trace.model.Result;
import com.trace.model.vo.TraceQueryResultVO;
import com.trace.service.ITraceQueryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trace")
public class TraceQueryController {

    @Autowired
    private ITraceQueryService queryService;

    @GetMapping("/query/{productNoOrQrcode}")
    public Result<TraceQueryResultVO> query(@PathVariable String productNoOrQrcode) {
        return queryService.queryByProductNo(productNoOrQrcode);
    }
}