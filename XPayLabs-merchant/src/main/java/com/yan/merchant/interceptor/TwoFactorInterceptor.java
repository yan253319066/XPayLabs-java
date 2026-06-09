package com.yan.merchant.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.yan.xpay.enums.GoogleStatus;
import com.yan.xpay.service.IMerchantService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TwoFactorInterceptor implements HandlerInterceptor {

    private final IMerchantService merchantService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!LoginHelper.isLogin()) return true;

        String username = LoginHelper.getUsername();
        var merchantVo = merchantService.getMerchantByName(username);
        if (merchantVo == null || merchantVo.getGoogleStatus() != GoogleStatus.BOUND) return true;

        Object verified = StpUtil.getTokenSession().get("2fa_verified");
        if (verified instanceof Boolean && (Boolean) verified) return true;

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(403);
        response.getWriter().write(JSONUtil.toJsonStr(R.fail(403, "2FA required")));
        return false;
    }
}
