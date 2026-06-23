package com.points.controller;

import com.points.dto.request.IssueRequest;
import com.points.dto.response.R;
import com.points.service.PointsIssueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@Api(tags = "积分发行")
@RestController
@RequestMapping("/api/points")
public class IssueController {

    @Autowired
    private PointsIssueService pointsIssueService;

    @ApiOperation("积分发行")
    @PostMapping("/issue")
    public R<Map<String, Object>> issuePoints(@Valid @RequestBody IssueRequest request, HttpServletRequest httpRequest) {
        Long operatorId = (Long) httpRequest.getAttribute("userId");
        return pointsIssueService.issuePoints(request, operatorId);
    }
}
