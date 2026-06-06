package com.yan.xpay.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.yan.xpay.config.XPayConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
@RequiredArgsConstructor
public class SkipSign {

	private final XPayConfig config;

	public String sign(Map<String, Object> params) {
		String sortedData = buildSortedData(params);
		HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, HexUtil.decodeHex(config.getSkipSignSecret()));
		return hmac.digestHex(sortedData);
	}

	public boolean verify(Map<String, Object> params, String sign) {
		return sign(params).equals(sign);
	}

	private static String buildSortedData(Map<String, Object> params) {
		Map<String, Object> filtered = new TreeMap<>();
		params.forEach((k, v) -> {
			if (v != null && !"sign".equals(k)) {
				filtered.put(k, v);
			}
		});
		return MapUtil.sortJoin(filtered, "&", "=", true);
	}

	public static void main(String[] args) {
		XPayConfig cfg = new XPayConfig();
		cfg.setSkipSignSecret("35a0b2e13f02428113fa0489477a5b45127e1cb4d424f10f04d2df71fa90c8e3");
		SkipSign skipSign = new SkipSign(cfg);
		Map<String, Object> params = new HashMap<>();
		params.put("uid", "123");
		String sign = skipSign.sign(params);
		System.out.println(skipSign.verify(params, sign));
	}

}
