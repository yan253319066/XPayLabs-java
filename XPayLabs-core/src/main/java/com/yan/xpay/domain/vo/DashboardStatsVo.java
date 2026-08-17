package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardStatsVo {
    private Trade trade = new Trade();
    private Fund fund = new Fund();
    private Health health = new Health();

    @Data
    public static class Trade {
        private DashboardTradeBucketVo collection = emptyBucket();
        private DashboardTradeBucketVo payout = emptyBucket();
        private List<DashboardSymbolStatVo> bySymbol = new ArrayList<>();
    }

    @Data
    public static class Fund {
        private DashboardTradeBucketVo recharge = emptyBucket();
        private DashboardTradeBucketVo withdraw = emptyBucket();
        @JsonSerialize(using = BigDecimalStringSerializer.class)
        private BigDecimal feeTotal = BigDecimal.ZERO;
        private List<DashboardSymbolStatVo> bySymbol = new ArrayList<>();
    }

    @Data
    public static class Health {
        private Long failedOrderCount = 0L;
        private Long expiredOrderCount = 0L;
        private Long callbackFailCount = 0L;
        private Long pendingConfirmCount = 0L;
        /** admin only; merchant 响应保持 null */
        private Long errorBlockCount;
        private List<DashboardStaleTrackerVo> staleTrackers;
    }

    private static DashboardTradeBucketVo emptyBucket() {
        DashboardTradeBucketVo b = new DashboardTradeBucketVo();
        b.setTotalCount(0L);
        b.setSuccessCount(0L);
        b.setSuccessAmount(BigDecimal.ZERO);
        b.setSuccessRate("0.00%");
        return b;
    }
}
