package com.points.controller;

import com.points.dto.request.ConsumeRequest;
import com.points.dto.response.R;
import com.points.service.PointsConsumeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@Api(tags = "积分消费")
@RestController
@RequestMapping("/api/points")
public class ConsumeController {

    @Autowired
    private PointsConsumeService pointsConsumeService;

    @ApiOperation("积分消费")
    @PostMapping("/consume")
    public R<Map<String, Object>> consume(@Valid @RequestBody ConsumeRequest request, HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        return pointsConsumeService.consume(request, operatorId);
    }
}
