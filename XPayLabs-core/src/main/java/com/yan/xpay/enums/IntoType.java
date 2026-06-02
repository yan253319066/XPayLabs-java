package com.yan.xpay.enums;

public enum IntoType {
	/**
	 * 归集到热钱包 （商家热钱包地址，由平台生成）
	 */
	HOT,
	/**
	 * 归集到冷钱包 （商家冷钱包地址，由商家提供）
	 */
	COLD,
	/**
	 * 归集平台钱包 （归集到平台冷钱包）
	 */
	PLATFORM
}
