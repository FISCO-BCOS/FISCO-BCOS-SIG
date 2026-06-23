package com.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.points.dto.response.R;
import com.points.entity.Merchant;
import com.points.entity.PointsTransaction;
import com.points.enums.AuditStatusEnum;
import com.points.enums.ErrorCodeEnum;
import com.points.enums.TxTypeEnum;
import com.points.exception.BusinessException;
import com.points.mapper.MerchantMapper;
import com.points.mapper.PointsTransactionMapper;
import com.points.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private PointsTransactionMapper pointsTransactionMapper;

    @Override
    public R<Map<String, Object>> listMerchants(Integer pageNum, Integer pageSize, String keyword,
                                                  Integer auditStatus, Integer status, String businessType) {
        PageHelper.startPage(pageNum, pageSize);

        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Merchant::getMerchantName, keyword)
                    .or().like(Merchant::getMerchantNo, keyword)
                    .or().like(Merchant::getContactPhone, keyword));
        }
        if (auditStatus != null) {
            wrapper.eq(Merchant::getAuditStatus, auditStatus);
        }
        if (status != null) {
            wrapper.eq(Merchant::getStatus, status);
        }
        if (StringUtils.hasText(businessType)) {
            wrapper.eq(Merchant::getBusinessType, businessType);
        }
        wrapper.orderByDesc(Merchant::getCreateTime);

        List<Merchant> list = merchantMapper.selectList(wrapper);
        PageInfo<Merchant> pageInfo = new PageInfo<>(list);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageInfo.getTotal());
        result.put("pageNum", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());
        result.put("pages", pageInfo.getPages());

        return R.ok(result);
    }

    @Override
    public R<Map<String, Object>> getMerchantDetail(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_NOT_FOUND);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("merchant", merchant);

        LambdaQueryWrapper<PointsTransaction> txWrapper = new LambdaQueryWrapper<>();
        txWrapper.eq(PointsTransaction::getToMerchantId, id)
                .or()
                .eq(PointsTransaction::getFromMerchantId, id);
        txWrapper.orderByDesc(PointsTransaction::getCreateTime);
        txWrapper.last("LIMIT 10");
        List<PointsTransaction> recentTransactions = pointsTransactionMapper.selectList(txWrapper);
        result.put("recentTransactions", recentTransactions);

        return R.ok(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> auditMerchant(Long id, Integer auditStatus, String rejectReason) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_NOT_FOUND);
        }
        if (merchant.getAuditStatus() != AuditStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_AUDITED);
        }

        merchant.setAuditStatus(auditStatus);
        if (auditStatus == AuditStatusEnum.APPROVED.getCode()) {
            merchant.setStatus(1);
            merchant.setChainAddress("0x" + UUID.randomUUID().toString().replace("-", "").substring(0, 40));
        } else if (auditStatus == AuditStatusEnum.REJECTED.getCode()) {
            merchant.setRejectReason(rejectReason);
        }
        merchantMapper.updateById(merchant);

        log.info("商户审核完成: merchantId={}, status={}", id, auditStatus);
        return R.ok("审核操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> switchMerchantStatus(Long id, Integer status) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_NOT_FOUND);
        }
        merchant.setStatus(status);
        merchantMapper.updateById(merchant);
        return R.ok("状态切换成功");
    }
}
