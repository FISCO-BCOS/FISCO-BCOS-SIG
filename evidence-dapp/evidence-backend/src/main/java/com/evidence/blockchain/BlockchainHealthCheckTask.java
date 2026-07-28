package com.evidence.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BlockchainHealthCheckTask {

    private static final Logger log = LoggerFactory.getLogger(BlockchainHealthCheckTask.class);

    private int failCount = 0;

    @Autowired
    BlockchainEvidenceService blockchainEvidenceService;

    @Scheduled(fixedRate = 60000)
    public void check() {
        try {
            long count = blockchainEvidenceService.getEvidenceCountFromChain();
            if (count >= 0) {
                failCount = 0;
                log.info("Blockchain health check: OK, evidence count: {}", count);
            } else {
                failCount++;
                log.error("Blockchain health check failed ({}/3): negative count returned", failCount);
            }
        } catch (Exception e) {
            failCount++;
            log.error("Blockchain health check failed ({}/3): {}", failCount, e.getMessage());
            if (failCount >= 3) {
                log.error("Blockchain node may be unreachable! 3 consecutive failures detected.");
            }
        }
    }
}
