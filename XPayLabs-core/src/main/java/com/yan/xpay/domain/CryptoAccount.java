package com.yan.xpay.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CryptoAccount {
	private String address;     // Base58 地址
	private String privateKey;  // Hex 格式私钥
}
