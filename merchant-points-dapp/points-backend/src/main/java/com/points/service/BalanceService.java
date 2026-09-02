package com.points.service;

import com.points.dto.response.R;
import java.util.Map;

public interface BalanceService {
    R<Map<String, Object>> getBalance(Long merchantId);
}
