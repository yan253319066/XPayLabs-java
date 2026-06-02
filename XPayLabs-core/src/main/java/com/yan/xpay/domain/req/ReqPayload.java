package com.yan.xpay.domain.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqPayload <T> {
	/**
	 * Sign
	 */
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
	 * data
	 */
	@NotNull(message = "The data cannot be left blank.")
	private T data;
}
