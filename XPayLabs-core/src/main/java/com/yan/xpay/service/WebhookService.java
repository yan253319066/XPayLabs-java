package com.yan.xpay.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.xpay.domain.*;
import com.yan.xpay.domain.vo.NotifyMerchant;
import com.yan.xpay.enums.NotifyStatus;
import com.yan.xpay.mapper.CallbackNoticeMapper;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.mapper.PaymentOrderMapper;
import com.yan.xpay.utils.WebhookSignUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

	private final MerchantMapper merchantMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final CallbackNoticeMapper callbackNoticeMapper;

	private static final ObjectMapper mapper = new ObjectMapper();

	private static Map<String, Object> convert(Object obj) {
		return mapper.convertValue(obj,  Map.class);
	}

	public boolean notifyMerchant(Long merchantId, String callbackUrl, NotifyPayload notifyPayload) {
        boolean b = false;
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
			b = response.getStatus()  == 200;
            return b;
		} catch (Exception e) {
			log.error(" 回调通知失败: {}", e.getMessage());
			return b;
		}finally {
            if (notifyPayload.getData() instanceof NotifyOrder notifyOrder) {
                PaymentOrder order = paymentOrderMapper.selectOne(new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getMerchantOrderId, notifyOrder.getOrderId()));
                if(order == null) {
                    log.error("Order Id {} 不存在", notifyOrder.getOrderId());

                }else {
                    List<CallbackNotice> list = callbackNoticeMapper.selectList(new LambdaQueryWrapper<CallbackNotice>()
                            .eq(CallbackNotice::getOrderId, order.getId()).eq(CallbackNotice::getNotifyStatus, NotifyStatus.INIT));
                    CallbackNotice callbackNotice;
                    if(ObjectUtil.isNotEmpty(list)) {
                        callbackNotice = list.get(0);
                    }else {
                        callbackNotice = new CallbackNotice();
                    }
                    callbackNotice.setMerchantId(order.getMerchantId());
                    callbackNotice.setOrderId(order.getId());
                    callbackNotice.setCallbackUrl(order.getCallbackUrl());
                    callbackNotice.setNotifyStatus(b ? NotifyStatus.SUCCESS : NotifyStatus.INIT);
                    callbackNoticeMapper.insertOrUpdate(callbackNotice);
                }
            }

        }
	}

	public boolean notifyMerchant(NotifyMerchant notifyMerchant) {
		if(ObjectUtil.isNull(notifyMerchant)) return false;
		return notifyMerchant(notifyMerchant.getMerchantId(), notifyMerchant.getCallbackUrl(), notifyMerchant.getNotifyPayload());
	}

}