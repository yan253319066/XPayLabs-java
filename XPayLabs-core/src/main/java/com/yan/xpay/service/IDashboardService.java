package com.yan.xpay.service;

import com.yan.xpay.domain.vo.DashboardAdminOverviewVo;
import com.yan.xpay.domain.vo.DashboardStatsVo;

public interface IDashboardService {
    DashboardAdminOverviewVo adminOverview();
    DashboardStatsVo stats(String range, Long merchantIdFilter, boolean admin, Long selfMerchantId);
}
