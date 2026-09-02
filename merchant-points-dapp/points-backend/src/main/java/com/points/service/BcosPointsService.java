package com.points.service;

import cn.hutool.json.JSONObject;
import com.points.config.BcosConfig;
import com.points.utils.WeBASEUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BcosPointsService {

    @Autowired
    private BcosConfig bcosConfig;

    @Autowired
    private WeBASEUtils weBASEUtils;

    public Map<String, Object> issuePoints(String toAddress, BigDecimal amount, String metadata) {
        log.info("链上发行积分: to={}, amount={}", toAddress, amount);

        List<Object> params = new ArrayList<>();
        params.add(toAddress);
        params.add(amount.toBigInteger().toString());
        params.add(metadata);

        JSONObject resp = weBASEUtils.callContract(
                bcosConfig.getOwnerAddress(), "issuePoints", params);

        Map<String, Object> result = buildResult(resp);
        if (weBASEUtils.isSuccess(resp)) {
            result.put("status", "success");
            log.info("积分发行成功: txHash={}", result.get("txHash"));
        } else {
            result.put("status", "failed");
            log.warn("积分发行失败: {}", weBASEUtils.getMessage(resp));
        }
        return result;
    }

    public Map<String, Object> transfer(String toAddress, BigDecimal amount) {
        log.info("链上转账: to={}, amount={}", toAddress, amount);

        List<Object> params = new ArrayList<>();
        params.add(toAddress);
        params.add(amount.toBigInteger().toString());

        JSONObject resp = weBASEUtils.callContract(
                bcosConfig.getOwnerAddress(), "transfer", params);

        Map<String, Object> result = buildResult(resp);
        if (weBASEUtils.isSuccess(resp)) {
            result.put("status", "success");
            log.info("转账成功: txHash={}", result.get("txHash"));
        } else {
            result.put("status", "failed");
            log.warn("转账失败: {}", weBASEUtils.getMessage(resp));
        }
        return result;
    }

    public Map<String, Object> consume(BigDecimal amount, String metadata) {
        log.info("链上消费: amount={}", amount);

        List<Object> params = new ArrayList<>();
        params.add(amount.toBigInteger().toString());
        params.add(metadata);

        JSONObject resp = weBASEUtils.callContract(
                bcosConfig.getOwnerAddress(), "consume", params);

        Map<String, Object> result = buildResult(resp);
        if (weBASEUtils.isSuccess(resp)) {
            result.put("status", "success");
            log.info("消费成功: txHash={}", result.get("txHash"));
        } else {
            result.put("status", "failed");
            log.warn("消费失败: {}", weBASEUtils.getMessage(resp));
        }
        return result;
    }

    public BigDecimal getBalance(String address) {
        log.debug("查询链上余额: address={}", address);

        List<Object> params = new ArrayList<>();
        params.add(address);

        JSONObject resp = weBASEUtils.callContract(
                bcosConfig.getOwnerAddress(), "getBalance", params);

        if (weBASEUtils.isSuccess(resp)) {
            String output = weBASEUtils.getOutput(resp);
            if (output != null && output.length() > 0 && !"0x".equals(output)) {
                try {
                    if (output.startsWith("0x") || output.startsWith("0X")) {
                        String hexValue = output.substring(2);
                        return new BigDecimal(new java.math.BigInteger(hexValue, 16));
                    }
                    return new BigDecimal(new java.math.BigInteger(output));
                } catch (Exception e) {
                    log.warn("余额解析失败: output={}", output);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public Map<String, Object> syncBalanceFromChain(String address) {
        log.info("同步链上余额: address={}", address);
        BigDecimal balance = getBalance(address);
        Map<String, Object> result = new HashMap<>();
        result.put("address", address);
        result.put("balance", balance);
        return result;
    }

    private Map<String, Object> buildResult(JSONObject resp) {
        Map<String, Object> result = new HashMap<>();
        result.put("txHash", weBASEUtils.getTxHash(resp));
        result.put("blockNumber", weBASEUtils.getBlockNumber(resp));
        result.put("message", weBASEUtils.getMessage(resp));
        return result;
    }
}