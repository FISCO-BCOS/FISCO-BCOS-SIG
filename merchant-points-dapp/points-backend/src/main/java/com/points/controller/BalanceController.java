package com.points.controller;

import com.points.dto.response.R;
import com.points.service.BalanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "余额查询")
@RestController
@RequestMapping("/api/points")
public class BalanceController {

    @Autowired
    private BalanceService balanceService;

    @ApiOperation("查询余额")
    @GetMapping("/balance/{merchantId}")
    public R<Map<String, Object>> getBalance(@PathVariable Long merchantId) {
        return balanceService.getBalance(merchantId);
    }
}
