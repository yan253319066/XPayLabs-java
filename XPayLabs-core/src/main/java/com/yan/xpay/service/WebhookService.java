package com.yan.xpay.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.xpay.domain.Merchant;
import com.yan.xpay.domain.NotifyPayload;
import com.yan.xpay.domain.vo.NotifyMerchant;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.utils.WebhookSignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

	private final MerchantMapper merchantMapper;

	private static final ObjectMapper mapper = new ObjectMapper();

	private static Map<String, Object> convert(Object obj) {
		return mapper.convertValue(obj,  Map.class);
	}

	public boolean notifyMerchant(Long merchantId, String callbackUrl, NotifyPayload notifyPayload) {
		try {
			if(merchantId == null) return false;
			Merchant merchant = merchantMapper.selectById(merchantId);
			if(StrUtil.isBlank(callbackUrl)) return false;
			if(ObjectUtil.isNull(notifyPayload)) return false;
			notifyPayload.setTimestamp(Instant.now().getEpochSecond());
			notifyPayload.setNonce(IdUtil.fastSimpleUUID());
			Map<String, Object> map = convert(notifyPayload);
//			log.info("notifyMerchant sign: {}", map);
			String sign = WebhookSignUtil.getSignature(merchant.getWebhookSecret(), map);
			notifyPayload.setSign(sign);
			String params = mapper.writeValueAsString(notifyPayload);
//			log.info("callback params {}", params);
			HttpResponse response = HttpRequest.post(callbackUrl)
				.contentType("application/json")
				.body(params)
				.execute();

			log.info("商家 {} 回调响应 - 状态码: {}, 内容: {}", merchantId, response.getStatus(),  response.body());
			return response.getStatus()  == 200;
		} catch (Exception e) {
			log.error(" 回调通知失败: {}", e.getMessage());
			return false;
		}
	}

	public boolean notifyMerchant(NotifyMerchant notifyMerchant) {
		if(ObjectUtil.isNull(notifyMerchant)) return false;
		return notifyMerchant(notifyMerchant.getMerchantId(), notifyMerchant.getCallbackUrl(), notifyMerchant.getNotifyPayload());
	}

}