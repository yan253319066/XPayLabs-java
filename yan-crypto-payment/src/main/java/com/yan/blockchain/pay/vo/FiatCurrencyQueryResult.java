package com.yan.blockchain.pay.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class FiatCurrencyQueryResult {
	/**
	 * 订单号
	 */
	private String orderNo;
	/**
	 * 状态
	 */
	private String status;
	/**
	 * 金额
	 */
	private String amount;
	/**
	 * 实际到账的金额 处理业务以这个值为准
	 */
	private String actualAmount;
	/**
	 *币种
	 */
	private String currency;

	/**
	 * 原始数据
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String original;
}
