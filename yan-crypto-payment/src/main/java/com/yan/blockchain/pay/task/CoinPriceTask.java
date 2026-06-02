package com.yan.blockchain.pay.task;

import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.yan.xpay.constant.RedisConstant;
import com.yan.xpay.domain.CryptoPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoinPriceTask {

	private final Environment environment;

	private boolean isDevEnvironment() {
		return Arrays.asList(environment.getActiveProfiles()).contains("dev");
	}

	@Scheduled(fixedDelay = 60000, initialDelay = 2000)
	public void run(){
		List<String> coins = List.of("btc", "eth", "trx", "bnb", "usdt", "pol", "avax", "sui");
		// 构建请求URL
		String url = buildCryptoUrl(coins);

		// 使用Hutool发送HTTP请求
		HttpRequest request = HttpRequest.get(url)
			.timeout(10000);

		// 只在开发环境设置代理
		if (isDevEnvironment()) {
			request.setHttpProxy("127.0.0.1", 6666);
			log.info(" 开发环境：已设置代理获取价格 127.0.0.1:6666");
		}
		try {
			String response = request.execute().body();
			if(JSONUtil.isTypeJSON(response)) {
				List<CryptoPrice> cryptoList = JSONUtil.toList(response, CryptoPrice.class);
				Map<String, CryptoPrice> cryptoPriceMap = cryptoList.stream()
					.collect(Collectors.toMap(CryptoPrice::getSymbol, crypto -> crypto));
				saveToCache(RedisConstant.CRYPTO_PRICE_KEY, cryptoPriceMap);
			}else {
				log.warn("CoinPriceTask response不是json格式");
			}
		} catch (HttpException e) {
			log.warn("获取价格超时 {}", e.getMessage());
		}
		catch (RuntimeException e) {
			log.error("CoinPriceTask run response", e);
		}

	}

	/**
	 * 构建加密货币API URL
	 */
	private String buildCryptoUrl(List<String> symbolList) {
		String symbols = String.join(",",  symbolList);
		return String.format("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&symbols=%s&precision=4",
			symbols);
	}

	/**
	 * 保存数据到Redis缓存
	 */
	private void saveToCache(String key, Map<String, CryptoPrice> map) {
		try {
			RedisUtils.setCacheMap(key, map);
		} catch (Exception e) {
			log.error(" 缓存数据失败: {}", e.getMessage());
		}
	}
}
