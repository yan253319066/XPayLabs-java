package com.yan.blockchain.pay.vo;

import lombok.Data;

@Data
public class FiatCurrencyInResult {
	/**
	 * 返回支付链接或收款卡号
	 */
	private String orderData;
	/**
	 * 印度的deeplink链接
	 */
	private String deepLink;

	/**
	 * 金额
	 */
	private String amount;
	/**
	 * 订单号
	 */
	private String orderNo;
}
