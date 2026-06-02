package com.yan.blockchain.pay.controller;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.yan.blockchain.pay.annotation.VerifySign;
import com.yan.xpay.domain.NotifyOrder;
import com.yan.xpay.domain.req.CreateCollectionOrderReq;
import com.yan.xpay.domain.req.CreatePayoutOrderReq;
import com.yan.xpay.domain.req.ReqPayload;
import com.yan.xpay.domain.vo.MerchantVo;
import com.yan.xpay.domain.vo.PayinOrderVo;
import com.yan.xpay.domain.vo.PaymentAddress;
import com.yan.blockchain.pay.interceptor.AuthInterceptor;
import com.yan.xpay.domain.vo.PaymentOrderStatus;
import com.yan.xpay.enums.MerchantSysVersion;
import com.yan.xpay.service.IPaymentOrderService;
import com.yan.xpay.utils.CryptoAddressValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.ValidatorUtils;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.ratelimiter.enums.LimitType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Order Info
 */
@VerifySign
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/order")
public class OrderController {
	private final IPaymentOrderService orderService;

	/**
	 * Collection
	 * <p></p>
	 * Merchant **initiates a collection** via API
	 * <p></p>
	 * Create Order
	 * @param req
	 * @param httpRequest
	 * @return
	 */
	@RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
	@PostMapping("/createCollection")
	public R<PaymentAddress> createCollection(@RequestBody @Validated ReqPayload<CreateCollectionOrderReq> req, HttpServletRequest httpRequest) {
		ValidatorUtils.validate(req.getData());
		MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
		if(merchant.getMerchantSysVersion() == MerchantSysVersion.V2) Assert.notBlank(req.getData().getUid(), ()-> new ServiceException("The uid cannot be left blank."));
		else if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3) {
			if(StrUtil.isNotBlank(req.getData().getUid()) && req.getData().getUid().equals("0")) throw new ServiceException("The uid cannot be zero.");
			Assert.notBlank(req.getData().getOrderId(), ()-> new ServiceException("The orderId cannot be left blank."));
		}
		return R.ok(orderService.createCollection(req, merchant));
	}

	/**
	 * Payout
	 * <p></p>
	 * Merchant **initiates a payout** via API
	 * <p></p> Create Order
	 * @param req
	 * @param httpRequest
	 * @return
	 */
	@RateLimiter(count = 100, time = 10, limitType = LimitType.IP)
	@PostMapping("/createPayout")
	public R<PaymentAddress> createPayout(@RequestBody @Validated ReqPayload<CreatePayoutOrderReq> req, HttpServletRequest httpRequest) {
		ValidatorUtils.validate(req.getData());
		if(!CryptoAddressValidator.isValidAddress(req.getData().getReceiveAddress(), req.getData().getChain()))
			return R.fail("ReceiveAddress error");
		MerchantVo merchant = (MerchantVo) httpRequest.getAttribute(AuthInterceptor.REQUEST_MERCHANT_KEY);
		if(merchant.getMerchantSysVersion() == MerchantSysVersion.V2) Assert.notBlank(req.getData().getUid(), ()-> new ServiceException("The uid cannot be left blank."));
		else if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3) {
			if(StrUtil.isNotBlank(req.getData().getUid()) && req.getData().getUid().equals("0")) throw new ServiceException("The uid cannot be zero.");
			Assert.notBlank(req.getData().getOrderId(), ()-> new ServiceException("The orderId cannot be left blank."));
		}
		return R.ok(orderService.createPayout(req, merchant));
	}

	/**
	 * Get Order Status Info
	 * @param orderId
	 * @return
	 */
	@RateLimiter(count = 1000, time = 10, limitType = LimitType.IP)
	@GetMapping("/status/{orderId}")
	public R<NotifyOrder> status(@PathVariable String orderId){
		return R.ok(orderService.getStatus(orderId));
	}

	/**
	 *  pay link
	 * @param orderId
	 * @return
	 */
	@RateLimiter(key = "#orderId", count = 2, time = 1)
	@GetMapping("/pay")
	public R<PayinOrderVo> pay(@RequestParam String orderId, @RequestParam String sign){
		return R.ok(orderService.getPayinByOrderId(orderId, sign));
	}

	/**
	 *  Get Order status
	 * @param orderId
	 * @return
	 */
	@RateLimiter(key = "#orderId", count = 2, time = 1)
	@GetMapping("/getOrderStatus")
	public R<PaymentOrderStatus> getOrderStatus(@RequestParam String orderId, @RequestParam String sign){
		PaymentOrderStatus paymentOrderStatus = new PaymentOrderStatus();
		paymentOrderStatus.setStatus(orderService.getPayinOrderStatus(orderId, sign));
		return R.ok(paymentOrderStatus);
	}
}
