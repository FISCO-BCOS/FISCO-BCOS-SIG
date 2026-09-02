package com.points.service.impl;

import com.points.dto.response.R;
import com.points.entity.Merchant;
import com.points.enums.ErrorCodeEnum;
import com.points.exception.BusinessException;
import com.points.mapper.MerchantMapper;
import com.points.service.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BalanceServiceImpl implements BalanceService {

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public R<Map<String, Object>> getBalance(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorCodeEnum.MERCHANT_NOT_FOUND);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("merchantId", merchant.getId());
        result.put("merchantName", merchant.getMerchantName());
        result.put("chainAddress", merchant.getChainAddress());
        result.put("balance", merchant.getPointsBalance());
        result.put("totalIssued", merchant.getTotalIssued());
        result.put("totalConsumed", merchant.getTotalConsumed());
        return R.ok(result);
    }
}
