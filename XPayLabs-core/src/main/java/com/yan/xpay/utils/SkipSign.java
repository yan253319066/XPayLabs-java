package com.yan.xpay.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 跳转支付页面签名
 */
public class SkipSign {
	private final static String secret = "35a0b2e13f02428113fa0489477a5b45127e1cb4d424f10f04d2df71fa90c8e3";

	public static String sign(Map<String, Object> params) {
		String sortedData = buildSortedData(params);
		HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, HexUtil.decodeHex(secret));
		String sign = hmac.digestHex(sortedData);
		return sign;
	}

	public static boolean verify(Map<String, Object> params, String sign) {
		boolean isValid = sign(params).equals(sign);
		return isValid;
	}

	/**
	 * 构建标准化待签名字符串
	 */
	private static String buildSortedData(Map<String, Object> params) {
		// 1. 过滤空值并排除sign字段
		Map<String, Object> filtered = new TreeMap<>(); // TreeMap自动按键排序
		params.forEach((k,  v) -> {
			if (v != null && !"sign".equals(k)) {
				filtered.put(k,  v);
			}
		});

		// 2. 键值对用=连接，参数对用&连接
		return MapUtil.sortJoin(filtered,  "&", "=", true);
	}

	public static void main(String[] args) {
		// 生成32字节(256位)的HMAC-SHA256密钥
//		byte[] secretBytes = SecureUtil.generateKey(HmacAlgorithm.HmacSHA256.getValue()).getEncoded();
//		// 转换为Hex字符串（可选）
//		String hexSecret = HexUtil.encodeHexStr(secretBytes);
//		System.out.println("Hex 编码密钥: " + hexSecret);
		Map<String, Object> params = new HashMap<>();
		params.put("uid", "123");
		String sign = sign(params);
		System.out.println(verify(params, sign));
	}
}
