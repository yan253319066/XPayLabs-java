package com.yan.xpay.domain.vo;

import com.yan.xpay.enums.Chain;
import lombok.Data;

@Data
public class PaymentAddress {
	/**
	 * Receiving Address
	 */
	private String address;
	/**
	 * Amount
	 */
	private String amount;
	/**
	 * Symbol
	 */
	private String symbol;
	/**
	 * Chain
	 */
	private Chain chain;
	/**
	 * uid
	 */
	private String uid;
	/**
	 * Order ID
	 */
	private String orderId;
	/**
	 * Expired Time
	 */
	private Long expiredTime;

	/**
	 *  Payment Url
	 */
	private String paymentUrl;

	/**
	 * icon
	 */
//	private String icon;
}
