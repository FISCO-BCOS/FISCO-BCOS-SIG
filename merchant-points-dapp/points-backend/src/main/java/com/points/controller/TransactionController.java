package com.points.controller;

import com.points.dto.response.R;
import com.points.service.TransactionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "交易流水")
@RestController
@RequestMapping("/api/points")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @ApiOperation("交易流水列表")
    @GetMapping("/transactions")
    public R<Map<String, Object>> listTransactions(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("交易类型") @RequestParam(required = false) Integer txType,
            @ApiParam("商户ID") @RequestParam(required = false) Long merchantId,
            @ApiParam("开始日期") @RequestParam(required = false) String dateFrom,
            @ApiParam("结束日期") @RequestParam(required = false) String dateTo,
            @ApiParam("排序字段") @RequestParam(required = false) String sortBy,
            @ApiParam("排序方式") @RequestParam(required = false) String sortOrder) {
        return transactionService.listTransactions(pageNum, pageSize, txType, merchantId, dateFrom, dateTo, sortBy, sortOrder);
    }
}
