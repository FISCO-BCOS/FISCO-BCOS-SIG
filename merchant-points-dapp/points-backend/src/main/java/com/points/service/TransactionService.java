package com.points.service;

import com.points.dto.response.R;
import java.util.Map;

public interface TransactionService {
    R<Map<String, Object>> listTransactions(Integer pageNum, Integer pageSize, Integer txType,
                                              Long merchantId, String dateFrom, String dateTo,
                                              String sortBy, String sortOrder);
}
