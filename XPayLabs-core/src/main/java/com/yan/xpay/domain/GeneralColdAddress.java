package com.yan.xpay.domain;

import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;


@Data
public class GeneralColdAddress {
	@Serial
	private static final long serialVersionUID = 1L;
	private Chain chain;
	private String symbol;
	private String coldAddress;
	private BigDecimal collectAmount;
}
