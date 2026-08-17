package com.yan.merchant.controller;

import com.yan.merchant.help.MerchantHelp;
import com.yan.xpay.domain.vo.DashboardAdminOverviewVo;
import com.yan.xpay.domain.vo.DashboardStatsVo;
import com.yan.xpay.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/xpay/dashboard")
public class DashboardController extends BaseController {

    private final IDashboardService dashboardService;
    private final MerchantHelp merchantHelp;

    private boolean isAdmin() {
        return Objects.equals(LoginHelper.getUserId(), 1L);
    }

    @GetMapping("/admin/overview")
    public R<DashboardAdminOverviewVo> adminOverview() {
        if (!isAdmin()) {
            return R.fail("无权限");
        }
        return R.ok(dashboardService.adminOverview());
    }

    @GetMapping("/stats")
    public R<DashboardStatsVo> stats(@RequestParam(defaultValue = "today") String range,
                                     @RequestParam(required = false) Long merchantId) {
        boolean admin = isAdmin();
        Long selfId = admin ? null : merchantHelp.getMerchantId();
        return R.ok(dashboardService.stats(range, merchantId, admin, selfId));
    }
}
