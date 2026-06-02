package com.yan.xpay.utils;

import java.util.Map;

public class MapUtils {
	/**
	 * key忽略大小写
	 * @param map
	 * @param key
	 * @return
	 * @param <V>
	 */
	public static <V> V getIgnoreCase(Map<String, V> map, String key) {
		return map.entrySet().stream()
			.filter(e -> e.getKey().equalsIgnoreCase(key))
			.findFirst()
			.map(Map.Entry::getValue)
			.orElse(null);
	}
}
