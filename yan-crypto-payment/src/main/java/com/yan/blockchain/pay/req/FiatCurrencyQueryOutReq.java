package com.yan.blockchain.pay.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FiatCurrencyQueryOutReq {
	@NotBlank(message = "The sign cannot be left blank.")
	private String sign;
	/**
	 * timestamp
	 */
	@NotNull(message = "The timestamp cannot be left blank.")
	private Long timestamp;
	/**
	 * nonce
	 */
	@NotBlank(message = "The nonce cannot be left blank.")
	private String nonce;

	/**
	 * orderNo
	 */
	@NotBlank(message = "The orderNo cannot be left blank.")
	private String orderNo;
}
