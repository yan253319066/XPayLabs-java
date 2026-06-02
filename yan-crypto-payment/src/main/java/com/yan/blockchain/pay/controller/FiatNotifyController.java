package com.yan.blockchain.pay.controller;

import com.yan.blockchain.pay.factory.FiatCurrencyFactory;
import com.yan.blockchain.pay.req.NotifyInReq;
import com.yan.blockchain.pay.req.NotifyOutReq;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.ServletUtils;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/order/notify")
public class FiatNotifyController {
	private final FiatCurrencyFactory fiatCurrencyFactory;
	/**
	 * 代收回调
	 * @param req
	 * @return
	 */
	@RateLimiter(count = 1000, time = 10, limitType = LimitType.IP)
	@PostMapping("/jdpay/fiatCurrencyNotifyIn")
	public String fiatCurrencyNotifyIn(@RequestBody @Validated NotifyInReq req){
		HttpServletRequest request = ServletUtils.getRequest();
		// 构建完整域名
		String domain = request.getScheme()  + "://" + request.getServerName();
		// 如果是非标准端口(非80/443)，需要包含端口号
		int port = request.getServerPort();
		if ((request.isSecure()  && port != 443) || (!request.isSecure()  && port != 80)) {
			domain += ":" + port;
		}
		log.info("fiatCurrencyNotifyIn本应用域名 {}", domain);
		return fiatCurrencyFactory.getService().fiatCurrencyNotifyIn(req);
	}

	/**
	 * 代付回调
	 * @param req
	 * @return
	 */
	@RateLimiter(count = 1000, time = 10, limitType = LimitType.IP)
	@PostMapping("/jdpay/fiatCurrencyNotifyOut")
	public String fiatCurrencyNotifyOut(@RequestBody @Validated NotifyOutReq req){
		HttpServletRequest request = ServletUtils.getRequest();
		// 构建完整域名
		String domain = request.getScheme()  + "://" + request.getServerName();
		// 如果是非标准端口(非80/443)，需要包含端口号
		int port = request.getServerPort();
		if ((request.isSecure()  && port != 443) || (!request.isSecure()  && port != 80)) {
			domain += ":" + port;
		}
		log.info("fiatCurrencyNotifyOut本应用域名 {}", domain);
		return fiatCurrencyFactory.getService().fiatCurrencyNotifyOut(req);
	}
}
