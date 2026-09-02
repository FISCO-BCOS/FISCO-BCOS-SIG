package com.points.service;

import com.points.dto.request.TransferRequest;
import com.points.dto.response.R;
import java.util.Map;

public interface PointsTransferService {
    R<Map<String, Object>> transfer(TransferRequest request, Long operatorId);
}
