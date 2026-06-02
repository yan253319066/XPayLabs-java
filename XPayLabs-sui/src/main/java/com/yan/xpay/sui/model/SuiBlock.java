package com.yan.xpay.sui.model;

import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class SuiBlock {
	private BigInteger sequenceNumber;
	private String digest;
	private Long timestampMs;//	毫秒
	private List<String> transactions; // txId
}
