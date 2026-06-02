package com.yan.xpay.domain.vo;

import com.yan.xpay.domain.NotifyPayload;
import lombok.Data;

@Data
public class NotifyMerchant {
	private Long merchantId;
	private String callbackUrl;
	private NotifyPayload notifyPayload;
}
