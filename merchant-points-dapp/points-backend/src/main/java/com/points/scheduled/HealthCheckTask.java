package com.points.scheduled;

import com.points.config.BcosConfig;
import com.points.service.BcosPointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HealthCheckTask {

    @Autowired
    private BcosConfig bcosConfig;

    @Autowired
    private BcosPointsService bcosPointsService;

    @Scheduled(fixedRate = 60000)
    public void checkHealth() {
        try {
            bcosPointsService.syncBalanceFromChain(bcosConfig.getOwnerAddress());
            log.debug("FISCO-BCOS 节点连接正常");
        } catch (Exception e) {
            log.warn("FISCO-BCOS 节点调用失败: {}", e.getMessage());
        }
    }
}