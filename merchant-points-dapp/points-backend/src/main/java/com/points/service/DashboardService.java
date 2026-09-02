package com.points.service;

import com.points.dto.response.R;
import java.util.Map;

public interface DashboardService {
    R<Map<String, Object>> getDashboardData();
}
