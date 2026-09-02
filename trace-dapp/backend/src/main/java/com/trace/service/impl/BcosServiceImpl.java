package com.trace.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.trace.service.IBcosService;
import com.trace.utils.WeBASEUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BcosServiceImpl implements IBcosService {

    @Value("${system.contract.owner_address}")
    private String ownerAddress;

    private Map<String, Object> callContract(String userAddress, String funcName, List<Object> funcParam) {
        String result = WeBASEUtils.funcPost(userAddress, funcName, funcParam);
        return parseContractResult(result);
    }

    static Map<String, Object> parseContractResult(String result) {
        Map<String, Object> resultMap = new HashMap<>();
        if (result == null || result.trim().isEmpty()) {
            return resultMap;
        }

        String trimmed = result.trim();
        if (trimmed.startsWith("{")) {
            JSONObject respBody = JSONUtil.parseObj(trimmed);
            for (String key : respBody.keySet()) {
                resultMap.put(key, respBody.get(key));
            }
        } else {
            resultMap.put("value", trimmed);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> createProduct(String userAddress, String productNo) {
        List<Object> p = new ArrayList<>(); p.add(productNo);
        return callContract(userAddress, "createProduct", p);
    }

    @Override
    public Map<String, Object> recordPlanting(String userAddress, String plantingNo, String productNo, String dataHash, String username) {
        List<Object> p = new ArrayList<>(); p.add(plantingNo); p.add(productNo); p.add(dataHash); p.add(username);
        return callContract(userAddress, "recordPlanting", p);
    }

    @Override
    public Map<String, Object> recordProcessing(String userAddress, String processingNo, String productNo, String dataHash, String username) {
        List<Object> p = new ArrayList<>(); p.add(processingNo); p.add(productNo); p.add(dataHash); p.add(username);
        return callContract(userAddress, "recordProcessing", p);
    }

    @Override
    public Map<String, Object> recordTesting(String userAddress, String testingNo, String productNo, String dataHash, String username) {
        List<Object> p = new ArrayList<>(); p.add(testingNo); p.add(productNo); p.add(dataHash); p.add(username);
        return callContract(userAddress, "recordTesting", p);
    }

    @Override
    public Map<String, Object> recordLogistics(String userAddress, String logisticsNo, String productNo, String dataHash, String username) {
        List<Object> p = new ArrayList<>(); p.add(logisticsNo); p.add(productNo); p.add(dataHash); p.add(username);
        return callContract(userAddress, "recordLogistics", p);
    }

    @Override
    public Map<String, Object> recordScan(String userAddress, String productNo) {
        List<Object> p = new ArrayList<>(); p.add(productNo);
        return callContract(userAddress, "recordScan", p);
    }

    @Override
    public Map<String, Object> getTraceRecord(String productNo) {
        List<Object> p = new ArrayList<>(); p.add(productNo);
        return callContract(ownerAddress, "getTraceRecord", p);
    }

    @Override
    public Map<String, Object> getTraceCount(String productNo) {
        List<Object> p = new ArrayList<>(); p.add(productNo);
        return callContract(ownerAddress, "getTraceCount", p);
    }
}
