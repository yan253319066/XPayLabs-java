package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardAdminOverviewVo {
    private Long merchantTotal;
    private Long activeMerchantToday;
    private Long todayCollectionCount;
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal todayCollectionAmount;
    private Long todayPayoutCount;
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal todayPayoutAmount;
    private Long callbackFailCount;
    private Long errorBlockCount;
    private List<DashboardStaleTrackerVo> staleTrackers = new ArrayList<>();
}
