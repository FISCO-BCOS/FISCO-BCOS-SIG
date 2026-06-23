package com.points.controller;

import com.points.dto.request.TransferRequest;
import com.points.dto.response.R;
import com.points.service.PointsTransferService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@Api(tags = "积分转账")
@RestController
@RequestMapping("/api/points")
public class TransferController {

    @Autowired
    private PointsTransferService pointsTransferService;

    @ApiOperation("积分转账")
    @PostMapping("/transfer")
    public R<Map<String, Object>> transfer(@Valid @RequestBody TransferRequest request, HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        return pointsTransferService.transfer(request, operatorId);
    }
}
