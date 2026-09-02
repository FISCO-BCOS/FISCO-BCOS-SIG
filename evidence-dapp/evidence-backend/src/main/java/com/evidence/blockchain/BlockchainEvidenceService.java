package com.evidence.blockchain;

import cn.hutool.core.util.HexUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlockchainEvidenceService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainEvidenceService.class);

    @Autowired
    private WeBASEUtils weBASEUtils;

    @Value("${fisco.bcos.owner-address}")
    private String ownerAddress;

    public ChainResult saveEvidenceOnChain(String hash, String metadata) {
        try {
            List funcParam = new ArrayList();
            funcParam.add(hash);
            funcParam.add(metadata);

            String result = weBASEUtils.funcPost(ownerAddress, "saveEvidence", funcParam);

            if (result == null || result.isEmpty()) {
                return new ChainResult(false, "WeBASE returned empty response", null, null, null);
            }

            JSONObject respBody = JSONUtil.parseObj(result);
            String txHash = (String) respBody.get("transactionHash");
            String blockNumber = (String) respBody.get("blockNumber");
            Boolean statusOK = respBody.getBool("statusOK");
            String statusMsg = (String) respBody.get("statusMsg");
            String message = (String) respBody.get("message");

            if (txHash != null && !txHash.isEmpty()) {
                long blockNum = -1;
                try {
                    if (blockNumber != null && !blockNumber.isEmpty()) {
                        if (blockNumber.startsWith("0x")) {
                            blockNum = Long.parseLong(blockNumber.substring(2), 16);
                        } else {
                            blockNum = Long.parseLong(blockNumber);
                        }
                    }
                } catch (Exception ignored) {}

                if (Boolean.TRUE.equals(statusOK)) {
                    log.info("saveEvidence on-chain SUCCESS: hash={}, txHash={}, block={}", hash, txHash, blockNum);
                    return new ChainResult(true, "success", txHash, blockNum, null);
                } else {
                    log.warn("saveEvidence on-chain REVERTED: hash={}, txHash={}, block={}, msg={}, reason={}",
                            hash, txHash, blockNum, statusMsg, message);
                    return new ChainResult(false, "on-chain(reverted): " + statusMsg + "/" + message, txHash, blockNum, null);
                }
            }

            log.error("saveEvidence no txHash returned: {}", result);
            return new ChainResult(false, "no transaction hash: " + result, null, null, null);
        } catch (Exception e) {
            log.error("saveEvidence exception via WeBASE", e);
            return new ChainResult(false, e.getMessage(), null, null, null);
        }
    }

    public ChainResult verifyEvidenceExists(String hash) {
        try {
            List funcParam = new ArrayList();
            funcParam.add(hash);

            String result = weBASEUtils.funcPost(ownerAddress, "verifyEvidence", funcParam);

            if (result == null || result.isEmpty()) {
                return new ChainResult(false, "empty response", null, null, null);
            }

            String trimmed = result.trim();
            if (trimmed.startsWith("{")) {
                JSONObject receipt = JSONUtil.parseObj(trimmed);
                String txHash = receipt.getStr("transactionHash");
                String blockNumber = receipt.getStr("blockNumber");
                boolean statusOK = Boolean.TRUE.equals(receipt.getBool("statusOK"));
                long blockNum = parseBlockNumber(blockNumber);
                if (!statusOK) {
                    String statusMsg = receipt.getStr("statusMsg");
                    String message = receipt.getStr("message");
                    return new ChainResult(false, "verify reverted: " + statusMsg + "/" + message,
                            txHash, blockNum, null);
                }

                ChainEvidenceInfo info = getEvidenceFromChain(hash);
                String verifyCount = info == null ? null : info.getVerifyCount();
                return new ChainResult(true, "verified", txHash, blockNum, verifyCount);
            }

            JSONArray respArray = JSONUtil.parseArray(trimmed);
            if (respArray.size() >= 2) {
                boolean exists = Boolean.TRUE.equals(respArray.get(0));
                String countStr = String.valueOf(respArray.get(1));
                return new ChainResult(exists, exists ? "verified" : "not found", null, null, countStr);
            }
            return new ChainResult(false, "unexpected response format", null, null, null);
        } catch (Exception e) {
            log.error("verifyEvidence exception via WeBASE", e);
            return new ChainResult(false, e.getMessage(), null, null, null);
        }
    }

    public ChainEvidenceInfo getEvidenceFromChain(String hash) {
        try {
            List funcParam = new ArrayList();
            funcParam.add(hash);

            String result = weBASEUtils.funcPost(ownerAddress, "getEvidence", funcParam);

            if (result == null || result.isEmpty()) {
                return null;
            }

            JSONArray respArray = JSONUtil.parseArray(result);
            if (respArray.size() >= 4) {
                ChainEvidenceInfo info = new ChainEvidenceInfo();
                info.setHash(hash);
                info.setMetadata(String.valueOf(respArray.get(0)));
                info.setOwner(String.valueOf(respArray.get(1)));
                info.setTimestamp(String.valueOf(respArray.get(2)));
                info.setVerifyCount(String.valueOf(respArray.get(3)));
                return info;
            }
            return null;
        } catch (Exception e) {
            log.error("getEvidence exception via WeBASE", e);
            return null;
        }
    }

    public long getEvidenceCountFromChain() {
        try {
            String result = weBASEUtils.funcPost(ownerAddress, "getEvidenceCount", new ArrayList());

            if (result == null || result.isEmpty()) {
                return -1;
            }

            JSONArray respArray = JSONUtil.parseArray(result);
            if (respArray.size() > 0) {
                Object val = respArray.get(0);
                if (val instanceof Long) return (Long) val;
                if (val instanceof Integer) return ((Integer) val).longValue();
                if (val instanceof String) {
                    String s = (String) val;
                    if (s.startsWith("0x")) return Long.parseLong(s.substring(2), 16);
                    return Long.parseLong(s);
                }
            }
            return 0;
        } catch (Exception e) {
            log.error("getEvidenceCount exception via WeBASE", e);
            return -1;
        }
    }

    public long getLatestBlockNumber() {
        try {
            String result = weBASEUtils.funcPost(ownerAddress, "getBlockNumber", new ArrayList());
            if (result == null || result.isEmpty()) return -1;
            JSONObject resp = JSONUtil.parseObj(result);
            String blockNumStr = (String) resp.get("blockNumber");
            if (blockNumStr != null && !blockNumStr.isEmpty()) {
                if (blockNumStr.startsWith("0x")) {
                    return Long.parseLong(blockNumStr.substring(2), 16);
                }
                return Long.parseLong(blockNumStr);
            }
            return -1;
        } catch (Exception e) {
            log.error("getLatestBlockNumber exception via WeBASE", e);
            return -1;
        }
    }

    private long parseBlockNumber(String blockNumber) {
        if (blockNumber == null || blockNumber.isEmpty()) {
            return -1;
        }
        try {
            if (blockNumber.startsWith("0x")) {
                return Long.parseLong(blockNumber.substring(2), 16);
            }
            return Long.parseLong(blockNumber);
        } catch (NumberFormatException e) {
            log.warn("Unable to parse block number: {}", blockNumber);
            return -1;
        }
    }
}
