package com.yan.blockchain.pay.controller;

import com.yan.blockchain.pay.annotation.VerifySign;
import com.yan.blockchain.pay.interceptor.AuthInterceptor;
import com.yan.blockchain.pay.req.BalanceReq;
import com.yan.blockchain.pay.req.CryptoAddressReq;
import com.yan.blockchain.pay.vo.CryptoAddressResult;
import com.yan.xpay.domain.bo.MerchantBo;
import com.yan.xpay.domain.req.RegisterMerchantReq;
import com.yan.xpay.domain.vo.ApiKeyVo;
import com.yan.xpay.domain.vo.MerchantAssetsVo;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.enums.*;
import com.yan.xpay.service.IMerchantAssetsService;
import com.yan.xpay.service.IUserAddressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.core.domain.R;
import com.yan.xpay.service.IMerchantService;

import java.math.BigDecimal;

/**
 * Merchant Info
 *
 * @author Yan
 * @date 2025-07-12
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/merchant")
public class MerchantController extends BaseController {

    private final IMerchantService merchantService;
    private final IMerchantAssetsService merchantAssetsService;
    private final IUserAddressService userAddressService;

    /**
     * Register Merchant
     * @param req
     * @return
     */
    @RateLimiter(count = 2, time = 10, limitType = LimitType.IP)
    @PostMapping("/register")
    public R<ApiKeyVo> register(@RequestBody @Validated RegisterMerchantReq req, HttpServletRequest httpRequest) {
        MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
        if(merchant.getId() != 1944052832788480002L){
            return R.fail("Not supported yet");
        }
        MerchantBo bo = new MerchantBo();
        bo.setName(req.getName());
        bo.setCallbackUrl(req.getCallbackUrl());
        bo.setFeeRatio(BigDecimal.ZERO);
        bo.setWithdrawalType(WithdrawalType.AUTO);
        bo.setIntoType(IntoType.COLD);
        bo.setEnableWhitelistIp(EnableWhitelistIp.DISABLED);
        bo.setMerchantSysVersion(MerchantSysVersion.V2);
        bo.setEnergyApikey(merchant.getEnergyApikey());
        ApiKeyVo apiKeyVo = merchantService.registerMerchant(bo);
        return R.ok(apiKeyVo);
    }

    /**
     * 按币种获取余额
     * @param req
     * @param httpRequest
     * @return
     */
    @VerifySign
    @RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
    @GetMapping("/getBalance")
    public R<MerchantAssetsVo> getBalance(@Validated BalanceReq req, HttpServletRequest httpRequest) {
        MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
        return R.ok(merchantAssetsService.getVoBalance(merchant.getId(), req.getSymbol()));
    }

    /**
     * 获取用户地址（一个用户对应一个地址）
     * @param req
     * @param httpRequest
     * @return
     */
    @VerifySign
    @RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
    @GetMapping("/getCryptoAddress")
    public R<CryptoAddressResult> getCryptoAddress(@Validated CryptoAddressReq req, HttpServletRequest httpRequest) {
        MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
        if(GeneratedAddressType.USER != merchant.getGeneratedAddressType()) return R.fail("Address acquisition is not supported.");
        String address = userAddressService.getUserAddress(req.getChain(), req.getSymbol(), merchant.getId(), req.getUid());
        CryptoAddressResult result = new CryptoAddressResult();
        result.setAddress(address);
        result.setChain(req.getChain());
        result.setSymbol(req.getSymbol());
        return R.ok(result);
    }

}
