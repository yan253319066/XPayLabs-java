package com.yan.xpay.event;

import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CollectEvent {
	private Chain chain;
	private String symbol;
	private String address;
	private BigDecimal balance;
}
