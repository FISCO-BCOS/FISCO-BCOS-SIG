package com.points.service.impl;

import com.points.dto.response.R;
import com.points.mapper.MerchantMapper;
import com.points.mapper.PointsTransactionMapper;
import com.points.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private MerchantMapper merchantMapper;
    @Autowired
    private PointsTransactionMapper pointsTransactionMapper;

    @Override
    public R<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalMerchants", merchantMapper.countActive());
        data.put("pendingAudit", merchantMapper.countPendingAudit());
        data.put("todayTransactions", pointsTransactionMapper.countTodayTransactions());
        data.put("todayIssued", pointsTransactionMapper.sumTodayIssued());
        data.put("todayConsumed", pointsTransactionMapper.sumTodayConsumed());
        data.put("totalIssued", merchantMapper.sumTotalIssued());
        data.put("totalConsumed", merchantMapper.sumTotalConsumed());
        return R.ok(data);
    }
}
