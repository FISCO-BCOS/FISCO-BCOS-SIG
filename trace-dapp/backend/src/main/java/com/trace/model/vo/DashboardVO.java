package com.trace.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class DashboardVO {
    private Integer totalProducts;
    private Integer todayNew;
    private Integer pendingRecords;
    private Integer totalScans;
    private List<ProductVO> latestProducts;
}