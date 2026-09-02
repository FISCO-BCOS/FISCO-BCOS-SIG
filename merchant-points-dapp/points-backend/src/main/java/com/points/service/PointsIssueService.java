package com.points.service;

import com.points.dto.request.IssueRequest;
import com.points.dto.response.R;
import java.util.Map;

public interface PointsIssueService {
    R<Map<String, Object>> issuePoints(IssueRequest request, Long operatorId);
}
