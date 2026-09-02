package com.evidence.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private long total;
    private long pages;
    private long pageNum;
    private long pageSize;
    private List<T> list;

    public PageResult(long total, long pageNum, long pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
        this.pages = pageSize > 0 ? (total + pageSize - 1) / pageSize : 0;
    }
}
