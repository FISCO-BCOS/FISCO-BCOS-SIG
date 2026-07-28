package com.trace.controller;

import com.trace.model.Result;
import com.trace.service.ITraceTestingService;
import com.trace.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/trace")
public class TraceTestingController {

    @Autowired
    private ITraceTestingService testingService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/testing")
    public Result<String> recordTesting(
            @RequestParam("productId") Long productId,
            @RequestParam("testItems") String testItems,
            @RequestParam("testResult") Integer testResult,
            @RequestParam("testDate") String testDate,
            @RequestParam(value = "reportFile", required = false) MultipartFile reportFile,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        return testingService.recordTesting(productId, testItems, testResult, testDate, reportFile, username);
    }
}
