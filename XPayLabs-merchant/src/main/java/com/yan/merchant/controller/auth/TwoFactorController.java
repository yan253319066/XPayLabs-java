package com.yan.merchant.controller.auth;

import com.yan.merchant.help.MerchantHelp;
import com.yan.xpay.domain.bo.Verify2faBo;
import com.yan.xpay.service.GoogleAuthService;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth/login")
public class TwoFactorController {

    private final MerchantHelp merchantHelp;
    private final GoogleAuthService googleAuthService;

    @RateLimiter(count = 10, time = 60, limitType = LimitType.IP)
    @PostMapping("/2fa/verify")
    public R<Void> verify2fa(@Valid @RequestBody Verify2faBo bo) {
        boolean b = googleAuthService.verifyCode(merchantHelp.getMerchant().getName(), bo.getCode());
        if (!b) return R.fail("2FA code error");
        StpUtil.getTokenSession().set("2fa_verified", true);
        return R.ok();
    }
}
