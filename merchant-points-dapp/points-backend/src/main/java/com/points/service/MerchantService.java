package com.points.service;

import com.points.dto.response.R;

import java.util.Map;

public interface MerchantService {
    R<Map<String, Object>> listMerchants(Integer pageNum, Integer pageSize, String keyword, Integer auditStatus, Integer status, String businessType);
    R<Map<String, Object>> getMerchantDetail(Long id);
    R<String> auditMerchant(Long id, Integer auditStatus, String rejectReason);
    R<String> switchMerchantStatus(Long id, Integer status);
}
