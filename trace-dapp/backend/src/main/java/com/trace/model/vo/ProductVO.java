package com.trace.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
public class ProductVO {
    private Long id;
    private String productNo;
    private String productName;
    private String category;
    private BigDecimal price;
    private String unit;
    private String originAddress;
    private Integer status;
    private String statusName;
    private Integer scanCount;
    private String createTime;
    /** 创建者用户ID（用于区分我的/全部） */
    private Long farmerId;
    /** 链上交易哈希 */
    private String txHash;
    /** 链上区块号 */
    private BigInteger blockNumber;

    public String getStatusName() {
        if (status == null) return "";
        switch (status) {
            case 0: return "草稿";
            case 1: return "已上架";
            case 2: return "已下架";
            default: return "未知";
        }
    }
}