package com.yan.xpay.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardTradeBucketVo {
    private Long totalCount;
    private Long successCount;
    private BigDecimal successAmount;
    private String successRate; // e.g. "98.00%"
}
