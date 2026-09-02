package com.points.service.impl;

import com.points.dto.request.TransferRequest;
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
import com.points.service.PointsTransferService;
import com.points.utils.NoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PointsTransferServiceImpl implements PointsTransferService {

    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private PointsTransactionMapper pointsTransactionMapper;
    @Autowired
    private BcosPointsService bcosPointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> transfer(TransferRequest request, Long operatorId) {
        Merchant fromMerchant = merchantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, operatorId));
        if (fromMerchant == null) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_NOT_FOUND);
        }
        if (fromMerchant.getId().equals(request.getToMerchantId())) {
            throw new BusinessException(ErrorCodeEnum.TRANSFER_SELF);
        }
        if (fromMerchant.getPointsBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }

        Merchant toMerchant = merchantMapper.selectById(request.getToMerchantId());
        if (toMerchant == null) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_NOT_FOUND);
        }

        Map<String, Object> chainResult = bcosPointsService.transfer(
                toMerchant.getChainAddress(), request.getAmount());
        String txHash = (String) chainResult.get("txHash");
        Long blockNumber = ((Number) chainResult.get("blockNumber")).longValue();

        String txNo = NoGenerator.generateTxNo();

        BigDecimal fromNewBalance = fromMerchant.getPointsBalance().subtract(request.getAmount());
        fromMerchant.setPointsBalance(fromNewBalance);
        merchantMapper.updateById(fromMerchant);

        PointsTransaction outTx = new PointsTransaction();
        outTx.setTxNo(txNo);
        outTx.setTxType(TxTypeEnum.TRANSFER_OUT.getCode());
        outTx.setFromMerchantId(fromMerchant.getId());
        outTx.setToMerchantId(request.getToMerchantId());
        outTx.setAmount(request.getAmount());
        outTx.setBalanceAfter(fromNewBalance);
        outTx.setRemark(request.getRemark());
        outTx.setTxHash(txHash);
        outTx.setBlockNumber(blockNumber);
        outTx.setChainStatus(1);
        outTx.setOperatorId(operatorId);
        pointsTransactionMapper.insert(outTx);

        BigDecimal toNewBalance = toMerchant.getPointsBalance().add(request.getAmount());
        toMerchant.setPointsBalance(toNewBalance);
        merchantMapper.updateById(toMerchant);

        PointsTransaction inTx = new PointsTransaction();
        inTx.setTxNo(txNo + "_IN");
        inTx.setTxType(TxTypeEnum.TRANSFER_IN.getCode());
        inTx.setFromMerchantId(fromMerchant.getId());
        inTx.setToMerchantId(request.getToMerchantId());
        inTx.setAmount(request.getAmount());
        inTx.setBalanceAfter(toNewBalance);
        inTx.setRemark(request.getRemark());
        inTx.setTxHash(txHash);
        inTx.setBlockNumber(blockNumber);
        inTx.setChainStatus(1);
        inTx.setOperatorId(operatorId);
        pointsTransactionMapper.insert(inTx);

        Map<String, Object> result = new HashMap<>();
        result.put("txNo", txNo);
        result.put("fromMerchantId", fromMerchant.getId());
        result.put("toMerchantId", request.getToMerchantId());
        result.put("amount", request.getAmount());
        result.put("balanceAfter", fromNewBalance);
        result.put("txHash", txHash);

        log.info("积分转账成功: from={}, to={}, amount={}", fromMerchant.getId(), request.getToMerchantId(), request.getAmount());
        return R.ok(result);
    }
}
