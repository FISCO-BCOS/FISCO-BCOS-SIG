package com.points.controller;

import com.points.dto.response.R;
import com.points.service.MerchantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "商户管理")
@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @ApiOperation("商户列表查询")
    @GetMapping
    public R<Map<String, Object>> listMerchants(
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @ApiParam("关键词") @RequestParam(required = false) String keyword,
            @ApiParam("审核状态") @RequestParam(required = false) Integer auditStatus,
            @ApiParam("营业状态") @RequestParam(required = false) Integer status,
            @ApiParam("经营类型") @RequestParam(required = false) String businessType) {
        return merchantService.listMerchants(pageNum, pageSize, keyword, auditStatus, status, businessType);
    }

    @ApiOperation("商户详情")
    @GetMapping("/{id}")
    public R<Map<String, Object>> getMerchantDetail(@PathVariable Long id) {
        return merchantService.getMerchantDetail(id);
    }

    @ApiOperation("商户审核")
    @PutMapping("/{id}/audit")
    public R<String> auditMerchant(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Integer auditStatus = (Integer) body.get("auditStatus");
        String rejectReason = (String) body.get("rejectReason");
        return merchantService.auditMerchant(id, auditStatus, rejectReason);
    }

    @ApiOperation("切换商户状态")
    @PutMapping("/{id}/status")
    public R<String> switchStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        return merchantService.switchMerchantStatus(id, body.get("status"));
    }
}
