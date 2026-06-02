package com.yan.xpay.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.yan.xpay.constant.RedisConstant;
import com.yan.xpay.domain.CryptoPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoPriceService {

	/**
	 * 获取加密货币价格
	 * @param symbol
	 * @return
	 */
	public CryptoPrice getCryptoPrice(String symbol) {
		return RedisUtils.getCacheMapValue(RedisConstant.CRYPTO_PRICE_KEY, symbol.toLowerCase());
	}

	/**
	 * 获取加密货币价格
	 * @return
	 */
	public List<CryptoPrice> getCryptoPrices() {
		Map<String, CryptoPrice> map = RedisUtils.getCacheMap(RedisConstant.CRYPTO_PRICE_KEY);
		return new ArrayList<>(map.values());
	}

	/**
	 * 获取法币汇率
	 */
	public BigDecimal getForexRates(String from, String to, BigDecimal amount) {
		HttpRequest request = HttpRequest.get(String.format("https://api.frankfurter.dev/v1/latest?base=%s&symbols=%s", from, to))
			.timeout(10000);
		String response = request.execute().body();
		BigDecimal rate = JSONUtil.parseObj(response).getJSONObject("rates").getBigDecimal(to.toUpperCase());
		return amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
	}
}
