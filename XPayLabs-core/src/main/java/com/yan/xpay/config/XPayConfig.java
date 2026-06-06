package com.yan.xpay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "xpaylabs")
public class XPayConfig {
	/**
	 * 订单超时时间（秒）
	 */
	private Long orderExpiredTime = 43200L;
	/**
	 * 支付域名
	 */
	private String payDomain;

	/**
	 * SkipSign HMAC-SHA256 secret
	 */
	private String skipSignSecret;
}
