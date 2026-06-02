package com.yan.xpay.domain.req;

import com.yan.xpay.enums.Chain;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BaseCreateOrderReq {
	/**
	 * Payment Amount
	 */
	@NotNull(message = "The amount cannot be left blank.")
	private BigDecimal amount;
	/**
	 * Symbol EX. USDT
	 */
	@NotBlank(message = "The symbol cannot be left blank.")
	private String symbol;
	/**
	 * Chain EX. TRON、ETH、BTC
	 */
	@NotNull(message = "The chain cannot be left blank.")
	private Chain chain;

	/**
	 * Order ID
	 */
//	@NotNull(message = "The OrderId cannot be left blank.")
	private String orderId;

	/**
	 * UID
	 */
//	@NotBlank(message = "The uid cannot be left blank.")
	private String uid;
}
