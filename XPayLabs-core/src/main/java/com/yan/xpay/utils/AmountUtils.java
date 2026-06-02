package com.yan.xpay.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class AmountUtils {

	/**
	 * 将区块链单位转换为显示单位（如 wei → ETH）
	 * @param amount 区块链最小单位的整数值（如 wei）
	 * @param decimals 代币精度（如 18）
	 * @return 显示单位的 BigDecimal（如 1.23 ETH）
	 */
	public static BigDecimal fromAmount(String amount, int decimals) {
		if (amount == null) {
			return BigDecimal.ZERO;
		}
		if (decimals < 0) {
			throw new IllegalArgumentException("Decimals must be non-negative");
		}
		return new BigDecimal(amount).movePointLeft(decimals);
	}

	/**
	 * 将区块链单位转换为显示单位（如 wei → ETH）
	 * @param amount 区块链最小单位的整数值（如 wei）
	 * @param decimals 代币精度（如 18）
	 * @return 显示单位的 BigDecimal（如 1.23 ETH）
	 */
	public static BigDecimal fromAmount(BigDecimal amount, int decimals) {
		if (amount == null) {
			return BigDecimal.ZERO;
		}
		return fromAmount(amount.toPlainString(), decimals);
	}

	/**
	 * 将显示单位转换为区块链单位（如 ETH → wei）
	 * @param amount 显示单位的 BigDecimal（如 1.23 ETH）
	 * @param decimals 代币精度（如 18）
	 * @return 区块链最小单位的整数值（如 1230000000000000000 wei）
	 */
	public static BigInteger toAmount(BigDecimal amount, int decimals) {
		if (amount == null) return BigInteger.ZERO;
		if (decimals < 0) throw new IllegalArgumentException("Decimals must be non-negative");
		return amount.movePointRight(decimals)
			.setScale(0, RoundingMode.HALF_UP)
			.toBigInteger(); // 安全转换为 BigInteger
	}

	public static void main(String[] args) {
		System.out.println(AmountUtils.fromAmount("1", 6));
		System.out.println(AmountUtils.toAmount(new BigDecimal(100000000), 18));
	}

}
