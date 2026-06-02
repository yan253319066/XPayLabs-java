package com.yan.xpay.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.yan.xpay.enums.Chain;

public class CryptoAddressValidator {

	private static final String BASE58_CHARS = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	private static final String HEX_CHARS = "0123456789abcdef";

	/**
	 * 统一验证方法
	 */
	public static boolean isValidAddress(String address, Chain chain) {
		if (StrUtil.isBlank(address))  {
			return false;
		}

		return switch (chain) {
			case BTC -> isValidBTCAddress(address);
			case ETH, ETH_SEPOLIA, BSC, BSC_TEST, AVAX_C_CHAIN, AVAX_FUJI_TEST, POLYGON, POLYGON_AMOY -> isValidETHAddress(address);
			case TRON, TRON_TEST -> isValidTRXAddress(address);
			default -> false;
		};
	}

	public static boolean isValidBTCAddress(String address) {
		return (address.startsWith("1")  || address.startsWith("3")  ||
			address.startsWith("bc1"))  &&
			(address.length()  >= 26 && address.length()  <= 62);
	}

	public static boolean isValidETHAddress(String address) {
		if (!address.startsWith("0x")  || address.length()  != 42) {
			return false;
		}
		return isValidHex(address.substring(2).toLowerCase());
	}

	public static boolean isValidTRXAddress(String address) {
		return address.startsWith("T")  && address.length()  == 34 &&
			isValidBase58(address.substring(1));
	}

	private static boolean isValidBase58(String input) {
		for (char c : input.toCharArray())  {
			if (BASE58_CHARS.indexOf(c)  == -1) {
				return false;
			}
		}
		return true;
	}

	private static boolean isValidHex(String input) {
		for (char c : input.toCharArray())  {
			if (HEX_CHARS.indexOf(c)  == -1) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		// 测试 BTC 地址
//		String btcAddress = "bc1puj6lkkpaeav9d6trexmpzyj0w7ee4x6aq6e202z2x9e37wuc7ymsjjyvct";
//		System.out.println("BTC  地址有效: " +
//			CryptoAddressValidator.isValidBTCAddress(btcAddress));
//
		// 测试 ETH 地址
		String ethAddress = "0x5773a72a94c9C5420228c9959150Ce50436A632c";
//		System.out.println("ETH  地址有效: " +
//			CryptoAddressValidator.isValidETHAddress(ethAddress));
//
//		// 测试 TRX 地址
//		String trxAddress = "TQTdR9EMACFcZCTsCzTzsEKLmYAvZ3WF4H";
//		System.out.println("TRX  地址有效: " +
//			CryptoAddressValidator.isValidTRXAddress(trxAddress));
//
//		// 统一验证
//		System.out.println(" 统一验证: " +
//			CryptoAddressValidator.isValidAddress(btcAddress,
//				Chain.BTC));
//		// 统一验证
//		System.out.println(" 统一验证: " +
//			CryptoAddressValidator.isValidAddress(ethAddress,
//				Chain.BSC_TEST));
//		// 统一验证
//		System.out.println(" 统一验证: " +
//			CryptoAddressValidator.isValidAddress(trxAddress,
//				Chain.TRON));

		// 统一验证
		System.out.println(" 统一验证: " +
			CryptoAddressValidator.isValidAddress(ethAddress,
				Chain.AVAX_C_CHAIN));
	}
}
