package com.yan.blockchain.pay.vo;

import com.yan.xpay.enums.Chain;
import lombok.Data;

@Data
public class CryptoAddressResult {
	private Chain chain;
	private String symbol;
	private String address;
}
