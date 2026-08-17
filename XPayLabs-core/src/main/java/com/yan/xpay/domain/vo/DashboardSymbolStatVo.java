package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardSymbolStatVo {
    private String symbol;
    private String orderType; // COLLECTION / PAYOUT / RECHARGE / WITHDRAW
    private Long successCount;
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal successAmount;
}
