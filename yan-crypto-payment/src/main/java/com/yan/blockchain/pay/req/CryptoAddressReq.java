package com.yan.blockchain.pay.req;

import com.yan.xpay.enums.Chain;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CryptoAddressReq {
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
	 * chain
	 */
	@NotNull(message = "The chain cannot be left blank.")
	private Chain chain;

	/**
	 * symbol
	 */
	@NotBlank(message = "The symbol cannot be left blank.")
	private String symbol;

	/**
	 * uid
	 */
	@NotBlank(message = "The uid cannot be left blank.")
	private String uid;
}
