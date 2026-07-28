package com.trace.controller;

import com.trace.model.Result;
import com.trace.model.bo.TracePlantingDTO;
import com.trace.service.ITracePlantingService;
import com.trace.utils.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/trace")
public class TracePlantingController {

    @Autowired
    private ITracePlantingService plantingService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/planting")
    public Result<String> recordPlanting(@RequestBody TracePlantingDTO dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        return plantingService.recordPlanting(dto, username);
    }
}
