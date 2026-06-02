package com.yan.xpay.domain;

import com.yan.xpay.enums.AssetOperType;
import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SimpleTransfer {

	private String network;

	private Chain chain;

	private String symbol;

	private Long merchantId;

	private AssetOperType type;

	private BigDecimal amount;

	private BigDecimal fee;
	private BigDecimal feeRate;
	private String feeSymbol;
	private BigDecimal rate;

	private String transactionNo;

	private String remark;
}
