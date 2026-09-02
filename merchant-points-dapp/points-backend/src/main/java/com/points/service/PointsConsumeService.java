package com.points.service;

import com.points.dto.request.ConsumeRequest;
import com.points.dto.response.R;
import java.util.Map;

public interface PointsConsumeService {
    R<Map<String, Object>> consume(ConsumeRequest request, Long operatorId);
}
