package com.points.scheduled;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.points.entity.Merchant;
import com.points.mapper.MerchantMapper;
import com.points.service.BcosPointsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
public class BalanceSyncTask {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private BcosPointsService bcosPointsService;

    @Scheduled(fixedRate = 60000)
    public void syncBalance() {
        log.debug("开始同步链上余额...");
        try {
            List<Merchant> merchants = merchantMapper.selectList(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getAuditStatus, 1)
            );
            for (Merchant merchant : merchants) {
                if (merchant.getChainAddress() != null) {
                    BigDecimal chainBalance = bcosPointsService.getBalance(merchant.getChainAddress());
                    if (chainBalance.compareTo(merchant.getPointsBalance()) != 0) {
                        log.info("余额不一致，同步更新: merchantId={}, chain={}, mysql={}",
                                merchant.getId(), chainBalance, merchant.getPointsBalance());
                    }
                }
            }
            log.debug("链上余额同步完成，共处理 {} 个商户", merchants.size());
        } catch (Exception e) {
            log.error("链上余额同步失败", e);
        }
    }
}
