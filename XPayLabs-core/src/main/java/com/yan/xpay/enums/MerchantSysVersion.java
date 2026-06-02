package com.yan.xpay.enums;

public enum MerchantSysVersion {
	V2,//商家自己出GAS费用，只有在提币和代付时才收取平台手续费,归集会归集到商家的热钱包或者冷钱包（根据IntoType决定）
	V3//平台收取手续费，归集到平台冷钱包地址
}
