package com.yan.blockchain.pay.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yan.blockchain.pay.annotation.VerifySign;
import com.yan.blockchain.pay.constant.JdpayConstant;
import com.yan.blockchain.pay.factory.FiatCurrencyFactory;
import com.yan.blockchain.pay.interceptor.AuthInterceptor;
import com.yan.blockchain.pay.req.FiatCurrencyInReq;
import com.yan.blockchain.pay.req.FiatCurrencyOutReq;
import com.yan.blockchain.pay.req.FiatCurrencyQueryInReq;
import com.yan.blockchain.pay.req.FiatCurrencyQueryOutReq;
import com.yan.blockchain.pay.vo.FiatCurrencyInResult;
import com.yan.blockchain.pay.vo.FiatCurrencyQueryResult;
import com.yan.xpay.domain.bo.FiatcurrencyOrderBo;
import com.yan.xpay.domain.vo.FiatcurrencyOrderVo;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.enums.FiatcurrencyOrderStatus;
import com.yan.xpay.enums.OrderType;
import com.yan.xpay.service.IFiatcurrencyOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.R;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 法币
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/v2/order")
public class FiatCurrencyV2Controller extends BaseController {
	private final FiatCurrencyFactory fiatCurrencyFactory;
	private final IFiatcurrencyOrderService fiatcurrencyOrderService;

	/**
	 * 法币代收
	 * V2
	 * @param req
	 * @return
	 */
	@VerifySign
	@RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
	@PostMapping("/fiatCurrencyIn")
	public R<FiatCurrencyInResult> fiatCurrencyIn(@RequestBody @Validated FiatCurrencyInReq req, HttpServletRequest httpRequest){
		MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
		log.info("fiatCurrencyIn v2 merchant id {} req {}", merchant.getId(), JSONUtil.toJsonStr(req));
		FiatcurrencyOrderBo bo = new FiatcurrencyOrderBo();
		bo.setCurrency(req.getCurrency());
		bo.setStatus(FiatcurrencyOrderStatus.INIT);
		bo.setAmount(new BigDecimal(req.getAmount()));
		bo.setChannelCode(FiatCurrencyFactory.defaultName);
		bo.setMerchantId(merchant.getId());
		bo.setOrderNo(req.getOrderNo());
		bo.setNotifyUrl(req.getNotifyUrl());
		bo.setOrderType(OrderType.COLLECTION);
		bo.setPayerCode(req.getPayCode());
		bo.setPayerName(JdpayConstant.name);
		bo.setPayerAccount(JdpayConstant.account);
		bo.setPayerEmail(JdpayConstant.email);
		if(StrUtil.isBlank(req.getPhone()))
			bo.setPayerPhone(JdpayConstant.phone);
		else bo.setPayerPhone(req.getPhone());

		FiatCurrencyInResult result = fiatCurrencyFactory.getService().fiatCurrencyInV2(bo);

		if(ObjectUtil.isNull(result)) return R.fail();
		else return R.ok(result);
	}

	/**
	 * 法币代付
	 * V2
	 * @param req
	 * @return
	 */
	@VerifySign
	@RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
	@PostMapping("/fiatCurrencyOut")
	public R<JSONObject> fiatCurrencyOut(@RequestBody @Validated FiatCurrencyOutReq req, HttpServletRequest httpRequest){
		MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
		log.info("fiatCurrencyOut v2 merchant id {} req {}", merchant.getId(), JSONUtil.toJsonStr(req));
		FiatcurrencyOrderBo bo = new FiatcurrencyOrderBo();
		bo.setCurrency(req.getCurrency());
		bo.setStatus(FiatcurrencyOrderStatus.INIT);
		bo.setAmount(new BigDecimal(req.getAmount()));
		bo.setChannelCode(FiatCurrencyFactory.defaultName);
		bo.setMerchantId(merchant.getId());
		bo.setOrderNo(req.getOrderNo());
		bo.setNotifyUrl(req.getNotifyUrl());
		bo.setOrderType(OrderType.PAYOUT);
		bo.setPayeeAccount(req.getAccount());
		bo.setPayeeEmail(req.getEmail());
		bo.setPayeeCode(req.getBankCode());
		bo.setPayeeName(req.getName());
		bo.setPayeePhone(req.getPhone());

		boolean b = fiatCurrencyFactory.getService().fiatCurrencyOutV2(bo);

		if(b) return R.ok();
		else return R.fail();
	}

	/**
	 * 查询法币代收订单
	 * @param req
	 * @return
	 */
	@VerifySign
	@RateLimiter(count = 1000, time = 10, limitType = LimitType.IP)
	@GetMapping("/fiatCurrencyQueryIn")
	public R<FiatCurrencyQueryResult> fiatCurrencyQueryIn(@Validated FiatCurrencyQueryInReq req, HttpServletRequest httpRequest){
		MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
		log.info("fiatCurrencyQueryIn v2 merchant id {} req {}", merchant.getId(), JSONUtil.toJsonStr(req));
		FiatcurrencyOrderVo vo = fiatcurrencyOrderService.queryByOrderNoAndMerchantId(req.getOrderNo(), merchant.getId());
		FiatCurrencyQueryResult result = new FiatCurrencyQueryResult();
		result.setActualAmount(vo.getAmount().toPlainString());
		result.setCurrency(vo.getCurrency());
		result.setStatus(vo.getStatus().name());
		result.setOrderNo(vo.getOrderNo());
		result.setAmount(vo.getAmount().toPlainString());
		return R.ok(result);
	}

	/**
	 * 查询法币代付订单
	 * @param req
	 * @return
	 */
	@VerifySign
	@RateLimiter(count = 1000, time = 10, limitType = LimitType.IP)
	@GetMapping("/fiatCurrencyQueryOut")
	public R<FiatCurrencyQueryResult> fiatCurrencyQueryOut(@Validated FiatCurrencyQueryOutReq req, HttpServletRequest httpRequest){
		MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
		log.info("fiatCurrencyQueryOut v2 merchant id {} req {}", merchant.getId(), JSONUtil.toJsonStr(req));
		FiatcurrencyOrderVo vo = fiatcurrencyOrderService.queryByOrderNoAndMerchantId(req.getOrderNo(), merchant.getId());
		FiatCurrencyQueryResult result = new FiatCurrencyQueryResult();
		result.setActualAmount(vo.getActualAmount().toPlainString());
		result.setCurrency(vo.getCurrency());
		result.setStatus(vo.getStatus().name());
		result.setOrderNo(vo.getOrderNo());
		result.setAmount(vo.getAmount().toPlainString());
		return R.ok(result);
	}
}
