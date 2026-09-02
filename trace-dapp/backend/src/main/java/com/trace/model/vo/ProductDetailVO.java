package com.trace.model.vo;

import lombok.Data;

@Data
public class ProductDetailVO {
    private ProductVO product;
    private Object planting;
    private Object processing;
    private Object testing;
    private Object logistics;
    private Integer scanCount;
    private Boolean isComplete;
}