package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardTradeBucketVo {
    private Long totalCount;
    private Long successCount;
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal successAmount;
    private String successRate; // e.g. "98.00%"
}
