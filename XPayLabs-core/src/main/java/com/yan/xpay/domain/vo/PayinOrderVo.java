package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayinOrderVo {
	private String orderId;
	private Chain chain;
	private String symbol;
	private String address;
	@JsonSerialize(using = BigDecimalStringSerializer.class)
	private BigDecimal amount;
	/**
	 * 实际支付金额
	 */
	@JsonSerialize(using = BigDecimalStringSerializer.class)
	private BigDecimal actualAmount;
	private Long expiredTime;
	private OrderStatus status;
	private String reason;
}
