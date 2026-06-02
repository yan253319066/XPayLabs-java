package com.yan.xpay.sui.utils;

import com.yan.xpay.constant.RedisConstant;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.utils.AmountUtils;
import org.dromara.common.redis.utils.RedisUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class SuiUtils {
	public static String getNetwork(Chain chain) {
		return switch (chain) {
			case SUI -> "mainnet";
			case SUI_TEST -> "testnet";
			default -> throw new RuntimeException("不支持的链");
		};
	}

	/**
	 * gas（区块链单位）
	 * @param chain
	 * @return
	 */
	public static BigInteger getGasBudget(Chain chain) {
		BigDecimal _gas = RedisUtils.getCacheMapValue(RedisConstant.GAS_KEY, chain.name());
		if(_gas == null || _gas.compareTo(BigDecimal.ZERO) <= 0) throw new RuntimeException(chain.name()+" 请先设置gas");
		return AmountUtils.toAmount(_gas, 9);
	}
}
