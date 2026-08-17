package com.yan.xpay.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardSymbolStatVo {
    private String symbol;
    private String orderType; // COLLECTION / PAYOUT / RECHARGE / WITHDRAW
    private Long successCount;
    private BigDecimal successAmount;
}
