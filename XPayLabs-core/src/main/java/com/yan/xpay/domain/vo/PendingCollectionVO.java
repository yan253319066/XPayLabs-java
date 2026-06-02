package com.yan.xpay.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PendingCollectionVO {
	private String chainSymbol;
	private BigDecimal totalAmount;
}
