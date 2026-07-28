package com.trace.controller;

import com.trace.model.Result;
import com.trace.model.bo.TraceLogisticsDTO;
import com.trace.service.ITraceLogisticsService;
import com.trace.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/trace")
public class TraceLogisticsController {

    @Autowired
    private ITraceLogisticsService logisticsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/logistics")
    public Result<String> recordLogistics(@RequestBody TraceLogisticsDTO dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        return logisticsService.recordLogistics(dto, username);
    }
}
