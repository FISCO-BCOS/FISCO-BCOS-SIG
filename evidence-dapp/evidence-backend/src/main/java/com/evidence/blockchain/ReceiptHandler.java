package com.evidence.blockchain;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

public class ReceiptHandler {

    public static ChainResult handle(String result) {
        JSONObject resultJson = JSONUtil.parseObj(result);
        Boolean statusOK = resultJson.getBool("statusOK");
        if (Boolean.TRUE.equals(statusOK)) {
            String txHash = resultJson.getStr("transactionHash");
            Long blockNumber = resultJson.getLong("blockNumber");
            return new ChainResult(true, "success", txHash, blockNumber, resultJson.getStr("output"));
        } else {
            String statusMsg = resultJson.getStr("statusMsg");
            if (statusMsg == null || statusMsg.isEmpty()) {
                statusMsg = "区块链服务返回异常";
            }
            return new ChainResult(false, statusMsg, null, null, null);
        }
    }
}