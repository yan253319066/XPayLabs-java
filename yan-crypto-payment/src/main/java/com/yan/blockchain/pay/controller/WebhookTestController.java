package com.yan.blockchain.pay.controller;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.*;
import com.yan.xpay.domain.NotifyCollect;
import com.yan.xpay.domain.NotifyOrder;
import com.yan.xpay.domain.NotifyPayload;
import com.yan.xpay.service.IPaymentOrderService;
import com.yan.xpay.utils.WebhookSignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
public class WebhookTestController {
	private static final ObjectMapper mapper = new ObjectMapper();
	private final IPaymentOrderService paymentOrderService;

	@PostMapping("/webhook")
	public void webhook(@RequestBody NotifyPayload payload) {
		try {
			boolean verified = WebhookSignUtil.verifySign("gHyI9DnpgMrYjrihIrbUghHq68j//wMQ/AnM2VJ2Fbbs0UGSxkApRg0ydZrk35nO",  mapper.convertValue(payload,  Map.class));
			log.info("Verified:  {}", verified);
			if(!verified) return;
			switch (payload.getNotifyType())  {
				case COLLECT_FAILED:
				case COLLECT_PENDING:
				case COLLECT_SUCCESS: {
					NotifyCollect collect = mapper.convertValue(payload.getData(),  NotifyCollect.class);
					log.info(" 收到收集通知: {}", collect);
					break;
				}
				default: {
					NotifyOrder order = mapper.convertValue(payload.getData(),  NotifyOrder.class);
					log.info(" 收到订单通知: {}", order);
					NotifyOrder orderStatus = paymentOrderService.getStatus(order.getOrderId());
					log.info("订单状态查询 {}", orderStatus);
				}
			}
		} catch (Exception e) {
			log.error("参数 {} 处理 webhook 失败 {}", mapper.convertValue(payload,  Map.class), e.getMessage());
		}
	}

	@PostMapping("/fiatCurrencyWebhook")
	public void fiatCurrencyWebhook(@RequestBody JSONObject obj) {
		log.info("fiatCurrencyWebhook params {}", obj.toString());
		boolean verified = WebhookSignUtil.verifySign("gHyI9DnpgMrYjrihIrbUghHq68j//wMQ/AnM2VJ2Fbbs0UGSxkApRg0ydZrk35nO",  mapper.convertValue(obj,  Map.class));
		log.info("fiatCurrencyWebhook Verified:  {}", verified);
	}
}
