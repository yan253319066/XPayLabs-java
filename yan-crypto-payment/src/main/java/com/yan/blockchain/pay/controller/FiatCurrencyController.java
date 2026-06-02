package com.yan.blockchain.pay.controller;

import cn.hutool.json.JSONUtil;
import com.yan.blockchain.pay.annotation.VerifySign;
import com.yan.blockchain.pay.factory.FiatCurrencyFactory;
import com.yan.blockchain.pay.interceptor.AuthInterceptor;
import com.yan.blockchain.pay.req.*;
import com.yan.xpay.domain.vo.MerchantVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 法币
 * 对接文档：
 * https://www.showdoc.com.cn/2598841943324732
 * 密码：112233
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/order")
public class FiatCurrencyController {
	private final FiatCurrencyFactory fiatCurrencyFactory;

	/**
	 * 法币代收
	 * @param req
	 * @return
	 */
	@VerifySign
	@RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
	@PostMapping("/fiatCurrencyIn")
	public String fiatCurrencyIn(@RequestBody @Validated FiatCurrencyInReq req, HttpServletRequest httpRequest){
		MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
		log.info("商户号：{}，请求法币代收：{}", merchant.getId(), JSONUtil.toJsonStr(req));
		String res = fiatCurrencyFactory.getService().fiatCurrencyIn(req, merchant);
		log.info("请求法币代收响应：{}", res);
		return res;
	}

	/**
	 * 法币代付
	 * @param req
	 * @return
	 */
	@VerifySign
	@RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
	@PostMapping("/fiatCurrencyOut")
	public String fiatCurrencyOut(@RequestBody @Validated FiatCurrencyOutReq req, HttpServletRequest httpRequest){
		MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
		log.info("商户号：{}，请求法币代付：{}", merchant.getId(), JSONUtil.toJsonStr(req));
		String res = fiatCurrencyFactory.getService().fiatCurrencyOut(req, merchant);
		log.info("请求法币代付响应：{}", res);
		return res;
	}

	/**
	 * 查询法币代收订单
	 * @param req
	 * @return
	 */
	@VerifySign
	@RateLimiter(count = 1000, time = 10, limitType = LimitType.IP)
	@GetMapping("/fiatCurrencyQueryIn")
	public String fiatCurrencyQueryIn(@RequestBody @Validated FiatCurrencyQueryInReq req){
		log.info("请求法币代收查询：{}", JSONUtil.toJsonStr(req));
		String res = fiatCurrencyFactory.getService().fiatCurrencyQueryIn(req);
		log.info("请求法币代收查询响应：{}", res);
		return res;
	}

	/**
	 * 查询法币代付订单
	 * @param req
	 * @return
	 */
	@VerifySign
	@RateLimiter(count = 1000, time = 10, limitType = LimitType.IP)
	@GetMapping("/fiatCurrencyQueryOut")
	public String fiatCurrencyQueryOut(@RequestBody @Validated FiatCurrencyQueryOutReq req){
		log.info("请求法币代付查询：{}", JSONUtil.toJsonStr(req));
		String res = fiatCurrencyFactory.getService().fiatCurrencyQueryOut(req);
		log.info("请求法币代付查询响应：{}", res);
		return res;
	}
}
