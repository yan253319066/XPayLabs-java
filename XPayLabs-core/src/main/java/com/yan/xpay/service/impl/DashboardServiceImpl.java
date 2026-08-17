package com.yan.xpay.service.impl;

import com.yan.xpay.domain.vo.DashboardAdminOverviewVo;
import com.yan.xpay.domain.vo.DashboardStatsVo;
import com.yan.xpay.domain.vo.DashboardTradeBucketVo;
import com.yan.xpay.enums.OrderStatus;
import com.yan.xpay.enums.OrderType;
import com.yan.xpay.enums.RechargeWithdraw;
import com.yan.xpay.mapper.DashboardMapper;
import com.yan.xpay.service.IDashboardService;
import com.yan.xpay.utils.DashboardRangeUtils;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DashboardServiceImpl implements IDashboardService {

    private static final long STALE_TRACKER_MS = 30 * 60 * 1000L;

    private final DashboardMapper dashboardMapper;

    @Override
    public DashboardAdminOverviewVo adminOverview() {
        Date[] r = resolveRange("today");
        Date start = r[0];
        Date end = r[1];

        Map<String, Object> collection = dashboardMapper.aggregateOrders(OrderType.COLLECTION.name(), start, end, null);
        Map<String, Object> payout = dashboardMapper.aggregateOrders(OrderType.PAYOUT.name(), start, end, null);

        DashboardAdminOverviewVo vo = new DashboardAdminOverviewVo();
        vo.setMerchantTotal(nz(dashboardMapper.countMerchants()));
        vo.setActiveMerchantToday(nz(dashboardMapper.countActiveMerchants(start, end)));
        vo.setTodayCollectionCount(toLong(mapVal(collection, "totalCount")));
        vo.setTodayCollectionAmount(toDecimal(mapVal(collection, "successAmount")));
        vo.setTodayPayoutCount(toLong(mapVal(payout, "totalCount")));
        vo.setTodayPayoutAmount(toDecimal(mapVal(payout, "successAmount")));
        vo.setCallbackFailCount(nz(dashboardMapper.countCallbackFail(null, null, null)));
        vo.setErrorBlockCount(nz(dashboardMapper.countErrorBlocks()));
        vo.setStaleTrackers(emptyIfNull(dashboardMapper.listStaleTrackers(staleThreshold())));
        return vo;
    }

    @Override
    public DashboardStatsVo stats(String range, Long merchantIdFilter, boolean admin, Long selfMerchantId) {
        Long scopeMerchantId = admin ? merchantIdFilter : selfMerchantId;
        Date[] r = resolveRange(range);
        Date start = r[0];
        Date end = r[1];

        DashboardStatsVo vo = new DashboardStatsVo();

        DashboardStatsVo.Trade trade = vo.getTrade();
        trade.setCollection(toOrderBucket(dashboardMapper.aggregateOrders(OrderType.COLLECTION.name(), start, end, scopeMerchantId)));
        trade.setPayout(toOrderBucket(dashboardMapper.aggregateOrders(OrderType.PAYOUT.name(), start, end, scopeMerchantId)));
        trade.setBySymbol(emptyIfNull(dashboardMapper.groupOrderSuccessBySymbol(start, end, scopeMerchantId)));

        DashboardStatsVo.Fund fund = vo.getFund();
        fund.setRecharge(toRwBucket(dashboardMapper.aggregateRechargeWithdraw(RechargeWithdraw.RECHARGE.name(), start, end, scopeMerchantId)));
        fund.setWithdraw(toRwBucket(dashboardMapper.aggregateRechargeWithdraw(RechargeWithdraw.WITHDRAW.name(), start, end, scopeMerchantId)));
        fund.setFeeTotal(toDecimal(dashboardMapper.sumOrderFee(start, end, scopeMerchantId))
            .add(toDecimal(dashboardMapper.sumRwFee(start, end, scopeMerchantId))));
        fund.setBySymbol(emptyIfNull(dashboardMapper.groupRwSuccessBySymbol(start, end, scopeMerchantId)));

        DashboardStatsVo.Health health = vo.getHealth();
        health.setFailedOrderCount(nz(dashboardMapper.countOrdersByStatus(OrderStatus.FAILED.name(), start, end, scopeMerchantId)));
        health.setExpiredOrderCount(nz(dashboardMapper.countOrdersByStatus(OrderStatus.EXPIRED.name(), start, end, scopeMerchantId)));
        health.setCallbackFailCount(nz(dashboardMapper.countCallbackFail(start, end, scopeMerchantId)));
        health.setPendingConfirmCount(nz(dashboardMapper.countPendingConfirm(scopeMerchantId)));
        if (admin) {
            health.setErrorBlockCount(nz(dashboardMapper.countErrorBlocksInRange(start, end)));
            health.setStaleTrackers(emptyIfNull(dashboardMapper.listStaleTrackers(staleThreshold())));
        }

        return vo;
    }

    private Date[] resolveRange(String range) {
        try {
            return DashboardRangeUtils.resolve(range);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(e.getMessage());
        }
    }

    private static Date staleThreshold() {
        return new Date(System.currentTimeMillis() - STALE_TRACKER_MS);
    }

    private static DashboardTradeBucketVo toOrderBucket(Map<String, Object> row) {
        long total = toLong(mapVal(row, "totalCount"));
        long success = toLong(mapVal(row, "successCount"));
        return bucket(total, success, toDecimal(mapVal(row, "successAmount")));
    }

    /** SUCCESS-only RW rows: totalCount = successCount, rate 100% when any data. */
    private static DashboardTradeBucketVo toRwBucket(Map<String, Object> row) {
        long success = toLong(mapVal(row, "successCount"));
        return bucket(success, success, toDecimal(mapVal(row, "successAmount")));
    }

    private static DashboardTradeBucketVo bucket(long total, long success, BigDecimal amount) {
        DashboardTradeBucketVo b = new DashboardTradeBucketVo();
        b.setTotalCount(total);
        b.setSuccessCount(success);
        b.setSuccessAmount(amount);
        b.setSuccessRate(total == 0 ? "0.00%" : String.format("%.2f%%", success * 100.0 / total));
        return b;
    }

    private static Object mapVal(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        if (map.containsKey(key)) {
            return map.get(key);
        }
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    private static long toLong(Object v) {
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        try {
            return new BigDecimal(v.toString()).longValue();
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static BigDecimal toDecimal(Object v) {
        if (v == null) {
            return BigDecimal.ZERO;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Number n) {
            return new BigDecimal(n.toString());
        }
        try {
            return new BigDecimal(v.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static long nz(Long v) {
        return v == null ? 0L : v;
    }

    private static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }
}
