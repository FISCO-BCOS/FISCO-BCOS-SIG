package com.trace.controller;

import cn.hutool.json.JSONUtil;
import com.trace.model.Result;
import com.trace.model.vo.ResultVO;
import com.trace.service.IBcosService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/contract/test")
public class ContractTestController {

    @Autowired
    private IBcosService bcosService;

    @Value("${system.contract.owner_address}")
    private String ownerAddress;

    @Value("${system.contract.traceAddress}")
    private String contractAddress;

    @Value("${project.webase-url}")
    private String webaseUrl;

    @GetMapping("/connectivity")
    public Result<Map<String, Object>> testConnectivity() {
        Map<String, Object> info = new HashMap<>();
        info.put("webaseUrl", webaseUrl);
        info.put("contractAddress", contractAddress);
        info.put("ownerAddress", ownerAddress);
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> result = bcosService.getTraceCount("TEST_NONEXISTENT");
            long elapsed = System.currentTimeMillis() - start;
            info.put("connected", true);
            info.put("responseTimeMs", elapsed);
            info.put("rawResponse", result);
            return Result.success(info);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            info.put("connected", false);
            info.put("responseTimeMs", elapsed);
            info.put("error", e.getMessage());
            return Result.success(info);
        }
    }

    @GetMapping("/productExists/{productNo}")
    public Result<Map<String, Object>> testProductExists(@PathVariable String productNo) {
        Map<String, Object> result = new HashMap<>();
        result.put("productNo", productNo);
        try {
            Map<String, Object> chainResult = bcosService.getTraceCount(productNo);
            result.put("chainResponse", chainResult);
            return Result.success(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return Result.success(result);
        }
    }

    @PostMapping("/createProduct")
    public Result<Map<String, Object>> testCreateProduct(@RequestBody Map<String, String> body) {
        String productNo = body.getOrDefault("productNo", "TEST_" + System.currentTimeMillis());
        Map<String, Object> result = new HashMap<>();
        result.put("productNo", productNo);
        try {
            Map<String, Object> chainResult = bcosService.createProduct(ownerAddress, productNo);
            result.put("chainResponse", chainResult);
            result.put("status", "SUCCESS");
            log.info("testCreateProduct success: {}", chainResult);
            return Result.success(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", "FAILED");
            log.error("testCreateProduct failed: {}", e.getMessage());
            return Result.success(result);
        }
    }

    @PostMapping("/recordPlanting")
    public Result<Map<String, Object>> testRecordPlanting(@RequestBody Map<String, String> body) {
        String productNo = body.getOrDefault("productNo", "P202605240001");
        String plantingNo = body.getOrDefault("plantingNo", "PL_TEST_001");
        String dataHash = body.getOrDefault("dataHash", "0x" + Integer.toHexString(productNo.hashCode()));
        Map<String, Object> result = new HashMap<>();
        result.put("productNo", productNo);
        result.put("plantingNo", plantingNo);
        result.put("dataHash", dataHash);
        try {
            Map<String, Object> chainResult = bcosService.recordPlanting(ownerAddress, plantingNo, productNo, dataHash, "testUser");
            result.put("chainResponse", chainResult);
            result.put("status", "SUCCESS");
            log.info("testRecordPlanting success: {}", chainResult);
            return Result.success(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", "FAILED");
            log.error("testRecordPlanting failed: {}", e.getMessage());
            return Result.success(result);
        }
    }

    @PostMapping("/recordProcessing")
    public Result<Map<String, Object>> testRecordProcessing(@RequestBody Map<String, String> body) {
        String productNo = body.getOrDefault("productNo", "P202605240001");
        String processingNo = body.getOrDefault("processingNo", "PR_TEST_001");
        String dataHash = body.getOrDefault("dataHash", "0x" + Integer.toHexString(processingNo.hashCode()));
        Map<String, Object> result = new HashMap<>();
        result.put("productNo", productNo);
        result.put("processingNo", processingNo);
        try {
            Map<String, Object> chainResult = bcosService.recordProcessing(ownerAddress, processingNo, productNo, dataHash, "testUser");
            result.put("chainResponse", chainResult);
            result.put("status", "SUCCESS");
            return Result.success(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", "FAILED");
            return Result.success(result);
        }
    }

    @PostMapping("/recordTesting")
    public Result<Map<String, Object>> testRecordTesting(@RequestBody Map<String, String> body) {
        String productNo = body.getOrDefault("productNo", "P202605240001");
        String testingNo = body.getOrDefault("testingNo", "TE_TEST_001");
        String dataHash = body.getOrDefault("dataHash", "0x" + Integer.toHexString(testingNo.hashCode()));
        Map<String, Object> result = new HashMap<>();
        result.put("productNo", productNo);
        result.put("testingNo", testingNo);
        try {
            Map<String, Object> chainResult = bcosService.recordTesting(ownerAddress, testingNo, productNo, dataHash, "testUser");
            result.put("chainResponse", chainResult);
            result.put("status", "SUCCESS");
            return Result.success(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", "FAILED");
            return Result.success(result);
        }
    }

    @PostMapping("/recordLogistics")
    public Result<Map<String, Object>> testRecordLogistics(@RequestBody Map<String, String> body) {
        String productNo = body.getOrDefault("productNo", "P202605240001");
        String logisticsNo = body.getOrDefault("logisticsNo", "LO_TEST_001");
        String dataHash = body.getOrDefault("dataHash", "0x" + Integer.toHexString(logisticsNo.hashCode()));
        Map<String, Object> result = new HashMap<>();
        result.put("productNo", productNo);
        result.put("logisticsNo", logisticsNo);
        try {
            Map<String, Object> chainResult = bcosService.recordLogistics(ownerAddress, logisticsNo, productNo, dataHash, "testUser");
            result.put("chainResponse", chainResult);
            result.put("status", "SUCCESS");
            return Result.success(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", "FAILED");
            return Result.success(result);
        }
    }

    @GetMapping("/getTraceRecord/{productNo}")
    public Result<Map<String, Object>> testGetTraceRecord(@PathVariable String productNo) {
        Map<String, Object> result = new HashMap<>();
        result.put("productNo", productNo);
        try {
            Map<String, Object> chainResult = bcosService.getTraceRecord(productNo);
            result.put("chainResponse", chainResult);
            return Result.success(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            return Result.success(result);
        }
    }

    @PostMapping("/e2e")
    public Result<Map<String, Object>> testE2E() {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> steps = new ArrayList<>();

        String testProductNo = "E2E_" + System.currentTimeMillis();

        // Step 1: createProduct
        Map<String, Object> step1 = new HashMap<>(); step1.put("name", "createProduct"); step1.put("productNo", testProductNo);
        try {
            Map<String, Object> r = bcosService.createProduct(ownerAddress, testProductNo);
            step1.put("result", r); step1.put("status", "PASS");
        } catch (Exception e) { step1.put("error", e.getMessage()); step1.put("status", "FAIL"); }
        steps.add(step1);

        // Step 2: recordPlanting
        Map<String, Object> step2 = new HashMap<>(); step2.put("name", "recordPlanting");
        try {
            Map<String, Object> r = bcosService.recordPlanting(ownerAddress, "PL_E2E_001", testProductNo, "hash_planting", "admin");
            step2.put("result", r); step2.put("status", "PASS");
        } catch (Exception e) { step2.put("error", e.getMessage()); step2.put("status", "FAIL"); }
        steps.add(step2);

        // Step 3: recordProcessing
        Map<String, Object> step3 = new HashMap<>(); step3.put("name", "recordProcessing");
        try {
            Map<String, Object> r = bcosService.recordProcessing(ownerAddress, "PR_E2E_001", testProductNo, "hash_processing", "admin");
            step3.put("result", r); step3.put("status", "PASS");
        } catch (Exception e) { step3.put("error", e.getMessage()); step3.put("status", "FAIL"); }
        steps.add(step3);

        // Step 4: recordTesting
        Map<String, Object> step4 = new HashMap<>(); step4.put("name", "recordTesting");
        try {
            Map<String, Object> r = bcosService.recordTesting(ownerAddress, "TE_E2E_001", testProductNo, "hash_testing", "admin");
            step4.put("result", r); step4.put("status", "PASS");
        } catch (Exception e) { step4.put("error", e.getMessage()); step4.put("status", "FAIL"); }
        steps.add(step4);

        // Step 5: recordLogistics
        Map<String, Object> step5 = new HashMap<>(); step5.put("name", "recordLogistics");
        try {
            Map<String, Object> r = bcosService.recordLogistics(ownerAddress, "LO_E2E_001", testProductNo, "hash_logistics", "admin");
            step5.put("result", r); step5.put("status", "PASS");
        } catch (Exception e) { step5.put("error", e.getMessage()); step5.put("status", "FAIL"); }
        steps.add(step5);

        // Step 6: getTraceRecord
        Map<String, Object> step6 = new HashMap<>(); step6.put("name", "getTraceRecord");
        try {
            Map<String, Object> r = bcosService.getTraceRecord(testProductNo);
            step6.put("result", r); step6.put("status", "PASS");
        } catch (Exception e) { step6.put("error", e.getMessage()); step6.put("status", "FAIL"); }
        steps.add(step6);

        // Step 7: recordScan
        Map<String, Object> step7 = new HashMap<>(); step7.put("name", "recordScan");
        try {
            Map<String, Object> r = bcosService.recordScan(ownerAddress, testProductNo);
            step7.put("result", r); step7.put("status", "PASS");
        } catch (Exception e) { step7.put("error", e.getMessage()); step7.put("status", "FAIL"); }
        steps.add(step7);

        int passCount = 0; int failCount = 0;
        for (Map<String, Object> s : steps) {
            if ("PASS".equals(s.get("status"))) passCount++; else failCount++;
        }

        report.put("testProductNo", testProductNo);
        report.put("steps", steps);
        report.put("passCount", passCount);
        report.put("failCount", failCount);
        report.put("overallStatus", failCount == 0 ? "ALL PASSED" : "SOME FAILED");

        log.info("Contract E2E Test Report: {}", JSONUtil.toJsonStr(report));
        return Result.success(report);
    }

    /** 调试：查看WeBASE原始返回和标准化后的内容 */
    @GetMapping("/debug/raw/{productNo}")
    public Result<Map<String, Object>> debugRawResponse(@PathVariable String productNo) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 直接调WeBASE看原始返回
            Map<String, Object> rawCall = bcosService.getTraceRecord(productNo);
            result.put("parsed", rawCall);
            return Result.success(result);
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorClass", e.getClass().getName());
            if (e.getCause() != null) {
                result.put("cause", e.getCause().getMessage());
            }
            return Result.success(result);
        }
    }
}