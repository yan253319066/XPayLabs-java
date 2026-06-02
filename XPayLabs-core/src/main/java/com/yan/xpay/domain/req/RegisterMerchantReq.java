package com.yan.xpay.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterMerchantReq {
	/**
	 * Merchant Name
	 */
	@NotBlank
	private String name;
	/**
	 * Callback URL
	 */
	private String callbackUrl;
}
