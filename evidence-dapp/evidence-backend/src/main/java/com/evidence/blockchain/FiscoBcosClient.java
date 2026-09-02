package com.evidence.blockchain;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.transaction.model.dto.CallResponse;
import org.fisco.bcos.sdk.transaction.model.dto.TransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Service
public class FiscoBcosClient {

    private static final Logger log = LoggerFactory.getLogger(FiscoBcosClient.class);

    @Autowired
    private Client client;

    @Value("${fisco.bcos.contract.evidence-address}")
    private String contractAddress;

    private CryptoKeyPair keyPair;
    private AssembleTransactionProcessor txProcessor;
    private String abi;

    @PostConstruct
    public void init() throws Exception {
        abi = com.evidence.utils.IOUtil.readResourceAsString("abi/EvidenceContract.abi");
        keyPair = client.getCryptoSuite().createKeyPair();
        txProcessor = TransactionProcessorFactory
                .createAssembleTransactionProcessor(client, keyPair);
        log.info("FiscoBcosClient initialized, account={}, contract={}", keyPair.getAddress(), contractAddress);
    }

    public ChainResult saveEvidenceOnChain(String hash, String metadata) {
        try {
            TransactionResponse response = txProcessor.sendTransactionAndGetResponse(
                    contractAddress, abi, "saveEvidence",
                    Arrays.asList(hash, metadata));

            TransactionReceipt receipt = response.getTransactionReceipt();
            if (receipt != null && receipt.isStatusOK()) {
                log.info("saveEvidence on-chain success, hash={}, txHash={}, blockNumber={}",
                        hash, receipt.getTransactionHash(), receipt.getBlockNumber());
                return new ChainResult(true, "success",
                        receipt.getTransactionHash(),
                        Long.parseLong(receipt.getBlockNumber().substring(2), 16),
                        null);
            } else {
                String msg = receipt != null ? receipt.getMessage() : "unknown error";
                log.error("saveEvidence on-chain failed: {}", msg);
                return new ChainResult(false, msg, null, null, null);
            }
        } catch (Exception e) {
            log.error("saveEvidence on-chain exception", e);
            return new ChainResult(false, e.getMessage(), null, null, null);
        }
    }

    public ChainResult verifyEvidenceOnChain(String hash) {
        try {
            CallResponse response = txProcessor.sendCall(
                    keyPair.getAddress(), contractAddress, abi,
                    "verifyEvidence", Arrays.asList(hash));

            List<Object> results = response.getReturnObject();
            boolean exists = results.size() > 0 && Boolean.TRUE.equals(results.get(0));
            return new ChainResult(exists, exists ? "verified" : "not found",
                    null, null,
                    results.size() > 1 ? String.valueOf(results.get(1)) : null);
        } catch (Exception e) {
            log.error("verifyEvidence on-chain exception", e);
            return new ChainResult(false, e.getMessage(), null, null, null);
        }
    }

    public ChainEvidenceInfo getEvidenceFromChain(String hash) {
        try {
            CallResponse response = txProcessor.sendCall(
                    keyPair.getAddress(), contractAddress, abi,
                    "getEvidence", Arrays.asList(hash));

            List<Object> results = response.getReturnObject();
            if (results.size() >= 4) {
                ChainEvidenceInfo info = new ChainEvidenceInfo();
                info.setHash(hash);
                info.setMetadata(String.valueOf(results.get(0)));
                info.setOwner(String.valueOf(results.get(1)));
                info.setTimestamp(String.valueOf(results.get(2)));
                info.setVerifyCount(String.valueOf(results.get(3)));
                return info;
            }
            return null;
        } catch (Exception e) {
            log.error("getEvidence on-chain exception", e);
            return null;
        }
    }

    public long getEvidenceCountFromChain() {
        try {
            CallResponse response = txProcessor.sendCall(
                    keyPair.getAddress(), contractAddress, abi,
                    "getEvidenceCount", Arrays.asList());

            List<Object> results = response.getReturnObject();
            if (results.size() > 0) {
                Object value = results.get(0);
                if (value instanceof Long) return (Long) value;
                if (value instanceof Integer) return ((Integer) value).longValue();
            }
            return 0;
        } catch (Exception e) {
            log.error("getEvidenceCount on-chain exception", e);
            return -1;
        }
    }
}