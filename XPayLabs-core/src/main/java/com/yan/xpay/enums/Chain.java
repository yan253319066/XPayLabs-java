package com.yan.xpay.enums;

import java.math.BigInteger;

/**
 * 区块链网络枚举（仅包含链类型和chainId）
 */
public enum Chain {
	TRON("728126428"),          // TRON主网
	ETH("1"),           // 以太坊主网
	BTC("0"),           // 比特币（无chainId概念，用0占位）
	BSC("56"),          // BSC主网
	ETH_SEPOLIA("11155111"),  // 以太坊Sepolia测试网
	BSC_TEST("97"),     // BSC测试网
	TRON_TEST("2494104990"), //SHASTA TESTNET
	POLYGON("137"),//Polygon Mainnet
	POLYGON_AMOY("80002"),//Polygon Amoy
	AVAX_C_CHAIN("43114"),//
	AVAX_FUJI_TEST("43113"),//Avalanche Fuji Testnet
	SUI("sui:mainnet"), //SUI MAINNET
	SUI_TEST("sui:testnet");//SUI TESTNET

	private final String chainId;

	Chain(String chainId) {
		this.chainId  = chainId;
	}

	public String getChainId() {
		return chainId;
	}
}
