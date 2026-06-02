package com.yan.xpay.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yan.xpay.enums.OrderStatus;
import com.yan.xpay.enums.OrderType;
import lombok.Data;

import java.math.BigDecimal;

/**
 *Notify of order information
 */
@Data
public class NotifyOrder {
	/**
	 * order number
	 */
	private String orderId;
	/**
	 * user id
	 */
	private String uid;
	/**
	 * order type
	 */
	private OrderType orderType;
	/**
	 * cause of failure
	 */
	private String reason;

	/**
	 * order status
	 */
	private OrderStatus status;

	/**
	 * amount
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private BigDecimal amount;
	/**
	 * actual amount
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private BigDecimal actualAmount;

	/**
	 * Platform handling fee
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private BigDecimal fee;
	/**
	 * transaction information
	 */
	private Transaction transaction;
}
