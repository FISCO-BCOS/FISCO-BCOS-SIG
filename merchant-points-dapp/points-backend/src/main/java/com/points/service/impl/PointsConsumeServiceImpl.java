package com.points.service.impl;

import com.points.dto.request.ConsumeRequest;
import com.points.dto.response.R;
import com.points.entity.Merchant;
import com.points.entity.PointsTransaction;
import com.points.enums.ErrorCodeEnum;
import com.points.enums.TxTypeEnum;
import com.points.exception.BusinessException;
import com.points.exception.InsufficientBalanceException;
import com.points.mapper.MerchantMapper;
import com.points.mapper.PointsTransactionMapper;
import com.points.service.BcosPointsService;
import com.points.service.PointsConsumeService;
import com.points.utils.NoGenerator;
import com.points.utils.PhoneDesensitizeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PointsConsumeServiceImpl implements PointsConsumeService {

    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private PointsTransactionMapper pointsTransactionMapper;
    @Autowired
    private BcosPointsService bcosPointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> consume(ConsumeRequest request, Long operatorId) {
        Merchant merchant = merchantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, operatorId));
        if (merchant == null) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_NOT_FOUND);
        }
        if (merchant.getPointsBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }

        Map<String, Object> chainResult = bcosPointsService.consume(request.getAmount(), request.getRemark());
        String txHash = (String) chainResult.get("txHash");
        Long blockNumber = ((Number) chainResult.get("blockNumber")).longValue();

        BigDecimal newBalance = merchant.getPointsBalance().subtract(request.getAmount());
        merchant.setPointsBalance(newBalance);
        merchant.setTotalConsumed(merchant.getTotalConsumed().add(request.getAmount()));
        merchantMapper.updateById(merchant);

        PointsTransaction tx = new PointsTransaction();
        tx.setTxNo(NoGenerator.generateTxNo());
        tx.setTxType(TxTypeEnum.CONSUME.getCode());
        tx.setFromMerchantId(merchant.getId());
        tx.setToMerchantId(merchant.getId());
        tx.setAmount(request.getAmount());
        tx.setBalanceAfter(newBalance);
        tx.setRemark("消费扣减 - " + request.getRemark());
        tx.setTxHash(txHash);
        tx.setBlockNumber(blockNumber);
        tx.setChainStatus(1);
        tx.setOperatorId(operatorId);
        pointsTransactionMapper.insert(tx);

        Map<String, Object> result = new HashMap<>();
        result.put("txNo", tx.getTxNo());
        result.put("amount", request.getAmount());
        result.put("balanceAfter", newBalance);
        result.put("consumerPhone", PhoneDesensitizeUtil.desensitize(request.getConsumerPhone()));
        result.put("txHash", txHash);

        log.info("积分消费成功: merchantId={}, amount={}", merchant.getId(), request.getAmount());
        return R.ok(result);
    }
}
