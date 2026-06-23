package com.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.points.dto.response.R;
import com.points.entity.PointsTransaction;
import com.points.mapper.PointsTransactionMapper;
import com.points.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private PointsTransactionMapper pointsTransactionMapper;

    @Override
    public R<Map<String, Object>> listTransactions(Integer pageNum, Integer pageSize, Integer txType,
                                                    Long merchantId, String dateFrom, String dateTo,
                                                    String sortBy, String sortOrder) {
        PageHelper.startPage(pageNum, pageSize);

        LambdaQueryWrapper<PointsTransaction> wrapper = new LambdaQueryWrapper<>();
        if (txType != null) {
            wrapper.eq(PointsTransaction::getTxType, txType);
        }
        if (merchantId != null) {
            wrapper.and(w -> w.eq(PointsTransaction::getToMerchantId, merchantId)
                    .or()
                    .eq(PointsTransaction::getFromMerchantId, merchantId));
        }
        if (StringUtils.hasText(dateFrom)) {
            wrapper.ge(PointsTransaction::getCreateTime, LocalDateTime.of(LocalDate.parse(dateFrom), LocalTime.MIN));
        }
        if (StringUtils.hasText(dateTo)) {
            wrapper.le(PointsTransaction::getCreateTime, LocalDateTime.of(LocalDate.parse(dateTo), LocalTime.MAX));
        }

        if ("amount".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), PointsTransaction::getAmount);
        } else {
            wrapper.orderByDesc(PointsTransaction::getCreateTime);
        }

        List<PointsTransaction> list = pointsTransactionMapper.selectList(wrapper);
        PageInfo<PointsTransaction> pageInfo = new PageInfo<>(list);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageInfo.getTotal());
        result.put("pageNum", pageInfo.getPageNum());
        result.put("pageSize", pageInfo.getPageSize());
        return R.ok(result);
    }
}
