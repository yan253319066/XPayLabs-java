package com.yan.xpay.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardAdminOverviewVo {
    private Long merchantTotal;
    private Long activeMerchantToday;
    private Long todayCollectionCount;
    private BigDecimal todayCollectionAmount;
    private Long todayPayoutCount;
    private BigDecimal todayPayoutAmount;
    private Long callbackFailCount;
    private Long errorBlockCount;
    private List<DashboardStaleTrackerVo> staleTrackers = new ArrayList<>();
}
