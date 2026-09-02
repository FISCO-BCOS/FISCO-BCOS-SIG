package com.trace.controller;

import com.trace.model.Result;
import com.trace.model.bo.TraceProcessingDTO;
import com.trace.service.ITraceProcessingService;
import com.trace.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/trace")
public class TraceProcessingController {

    @Autowired
    private ITraceProcessingService processingService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/processing")
    public Result<String> recordProcessing(@RequestBody TraceProcessingDTO dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        return processingService.recordProcessing(dto, username);
    }
}
