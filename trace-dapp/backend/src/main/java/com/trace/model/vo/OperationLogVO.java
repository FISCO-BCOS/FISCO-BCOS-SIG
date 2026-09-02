package com.trace.model.vo;

import lombok.Data;

@Data
public class OperationLogVO {
    private String time;
    private String action;
    private String detail;
}