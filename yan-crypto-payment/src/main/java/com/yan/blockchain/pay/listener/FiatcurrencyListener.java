package com.yan.blockchain.pay.listener;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.blockchain.pay.event.FiatcurrencyNotifyEvent;
import com.yan.xpay.domain.Merchant;
import com.yan.xpay.domain.vo.FiatcurrencyOrderVo;
import com.yan.xpay.enums.OrderType;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.utils.WebhookSignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FiatcurrencyListener {
	private final MerchantMapper merchantMapper;
	@Async
	@EventListener
	public void handleFiatcurrencyNotification(FiatcurrencyNotifyEvent event){
		FiatcurrencyOrderVo order = event.getOrder();
		if(OrderType.COLLECTION == order.getOrderType()) {
			fiatcurrencyInNotify(order);
		}else if(OrderType.PAYOUT == order.getOrderType()) {
			fiatcurrencyOutNotify(order);
		}
	}

	private void fiatcurrencyInNotify(FiatcurrencyOrderVo order){
		String notifyUrl = order.getNotifyUrl();
		if(StrUtil.isBlank(notifyUrl)) {
			log.error("notifyInUrl is null order_no {}", order.getOrderNo());
			return;
		}
		Merchant merchant = merchantMapper.selectById(order.getMerchantId());
		String secret = merchant.getWebhookSecret();
		JSONObject obj = new JSONObject();
		obj.set("orderNo", order.getOrderNo());
		obj.set("status", order.getStatus().name());
		obj.set("amount", order.getAmount().toPlainString());
		obj.set("actualAmount", order.getActualAmount().toPlainString());
		obj.set("timestamp", Instant.now().getEpochSecond());
		obj.set("nonce", IdUtil.fastSimpleUUID());
		ObjectMapper mapper = new ObjectMapper();
		obj.set("sign", WebhookSignUtil.getSignature(secret, mapper.convertValue(obj, Map.class)));
		String params = null;
		try {
			params = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		log.info("fiatcurrencyInNotify params {}", params);
		String response = HttpRequest.post(notifyUrl).contentType("application/json").body(params).execute().body();
		log.info("回调商家接口 fiatcurrencyInNotify response {}", response);
	}

	private void fiatcurrencyOutNotify(FiatcurrencyOrderVo order){
		String notifyUrl = order.getNotifyUrl();
		if(StrUtil.isBlank(notifyUrl)) {
			log.error("notifyOutUrl is null order_no {}", order.getOrderNo());
			return;
		}
		Merchant merchant = merchantMapper.selectById(order.getMerchantId());
		String secret = merchant.getWebhookSecret();
		JSONObject obj = new JSONObject();
		obj.set("orderNo", order.getOrderNo());
		obj.set("status", order.getStatus().name());
		obj.set("amount", order.getAmount().toPlainString());
		obj.set("actualAmount", order.getActualAmount().toPlainString());
		obj.set("timestamp", Instant.now().getEpochSecond());
		obj.set("nonce", IdUtil.fastSimpleUUID());
		ObjectMapper mapper = new ObjectMapper();
		obj.set("sign", WebhookSignUtil.getSignature(secret, mapper.convertValue(obj, Map.class)));
		String params = null;
		try {
			params = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		log.info("fiatcurrencyOutNotify params {}", params);
		String response = HttpRequest.post(notifyUrl).contentType("application/json").body(params).execute().body();
		log.info("回调商家接口 fiatcurrencyOutNotify response {}", response);
	}
}
