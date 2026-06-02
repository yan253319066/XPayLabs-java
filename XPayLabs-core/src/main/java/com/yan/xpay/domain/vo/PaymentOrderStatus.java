package com.yan.xpay.domain.vo;

import com.yan.xpay.enums.OrderStatus;
import lombok.Data;

@Data
public class PaymentOrderStatus {
	private OrderStatus status;
}
