package com.yan.xpay.domain.vo;

import lombok.Data;

@Data
public class ApiKeyVo {
	/**
	 * API KEY
	 */
	private String apiKey;/**
	 * Webhook secret
	 */
	private String webhookSecret;
}
