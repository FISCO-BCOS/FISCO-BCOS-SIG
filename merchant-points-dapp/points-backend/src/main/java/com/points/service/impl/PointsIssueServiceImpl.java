package com.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.points.dto.request.IssueRequest;
import com.points.dto.response.R;
import com.points.entity.Merchant;
import com.points.entity.PointsIssue;
import com.points.entity.PointsTransaction;
import com.points.enums.AuditStatusEnum;
import com.points.enums.ErrorCodeEnum;
import com.points.enums.TxTypeEnum;
import com.points.exception.BusinessException;
import com.points.mapper.MerchantMapper;
import com.points.mapper.PointsIssueMapper;
import com.points.mapper.PointsTransactionMapper;
import com.points.service.BcosPointsService;
import com.points.service.PointsIssueService;
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
public class PointsIssueServiceImpl implements PointsIssueService {

    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private PointsIssueMapper pointsIssueMapper;
    @Autowired
    private PointsTransactionMapper pointsTransactionMapper;
    @Autowired
    private BcosPointsService bcosPointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> issuePoints(IssueRequest request, Long operatorId) {
        Merchant merchant = merchantMapper.selectById(request.getMerchantId());
        if (merchant == null) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_NOT_FOUND);
        }
        if (merchant.getAuditStatus() != AuditStatusEnum.APPROVED.getCode()) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_NOT_AUDITED);
        }

        Map<String, Object> chainResult = bcosPointsService.issuePoints(
                merchant.getChainAddress(), request.getAmount(), request.getReason());
        String txHash = (String) chainResult.get("txHash");
        Long blockNumber = ((Number) chainResult.get("blockNumber")).longValue();

        PointsIssue issue = new PointsIssue();
        issue.setIssueNo(NoGenerator.generateIssueNo());
        issue.setMerchantId(request.getMerchantId());
        issue.setAmount(request.getAmount());
        issue.setReason(request.getReason());
        issue.setApproverId(operatorId);
        issue.setTxHash(txHash);
        issue.setBlockNumber(blockNumber);
        issue.setStatus(1);
        pointsIssueMapper.insert(issue);

        BigDecimal newBalance = merchant.getPointsBalance().add(request.getAmount());
        merchant.setPointsBalance(newBalance);
        merchant.setTotalIssued(merchant.getTotalIssued().add(request.getAmount()));
        merchantMapper.updateById(merchant);

        PointsTransaction tx = new PointsTransaction();
        tx.setTxNo(NoGenerator.generateTxNo());
        tx.setTxType(TxTypeEnum.ISSUE.getCode());
        tx.setToMerchantId(request.getMerchantId());
        tx.setAmount(request.getAmount());
        tx.setBalanceAfter(newBalance);
        tx.setRemark(request.getReason());
        tx.setTxHash(txHash);
        tx.setBlockNumber(blockNumber);
        tx.setChainStatus(1);
        tx.setOperatorId(operatorId);
        pointsTransactionMapper.insert(tx);

        Map<String, Object> result = new HashMap<>();
        result.put("issueId", issue.getId());
        result.put("issueNo", issue.getIssueNo());
        result.put("merchantId", request.getMerchantId());
        result.put("amount", request.getAmount());
        result.put("txHash", txHash);
        result.put("blockNumber", blockNumber);

        log.info("积分发行成功: merchantId={}, amount={}", request.getMerchantId(), request.getAmount());
        return R.ok(result);
    }
}
