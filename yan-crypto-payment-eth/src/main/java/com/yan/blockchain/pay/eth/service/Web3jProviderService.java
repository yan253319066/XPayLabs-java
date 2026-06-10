package com.yan.blockchain.pay.eth.service;

import com.yan.xpay.enums.Chain;
import com.yan.blockchain.pay.eth.config.EthConfig;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Web3jProviderService {
	private final Map<Chain, Web3j[]> web3jInstances;
	private final Map<Chain, String[]> web3jUrls; // 新增URL存储
	private final Map<Chain, AtomicInteger> currentIndex;

	public Web3jProviderService(Map<Chain, EthConfig.ChainConfig> chainConfigs) {
		this.web3jInstances  = new HashMap<>();
		this.web3jUrls  = new HashMap<>(); // 初始化
		this.currentIndex  = new HashMap<>();

		chainConfigs.forEach((chain,  config) -> {
			int providerCount = config.getRpcUrls().size();
			Web3j[] providers = new Web3j[providerCount];
			String[] urls = new String[providerCount];

			for (int i = 0; i < providers.length;  i++) {
				String rpcUrl = config.getRpcUrls().get(i  % config.getRpcUrls().size());
				providers[i] = Web3j.build(new HttpService(rpcUrl));
				urls[i] = rpcUrl; // 保存URL
			}

			web3jInstances.put(chain,  providers);
			web3jUrls.put(chain,  urls); // 保存URL映射
			currentIndex.put(chain,  new AtomicInteger(0));
		});
	}

	public Web3j getNextWeb3j(Chain chain) {
		Web3j[] providers = web3jInstances.get(chain);
		if (providers == null) {
			throw new IllegalArgumentException("未配置该链的Web3j提供者: " + chain);
		}
		// 直接使用数组长度
		int index = currentIndex.get(chain).getAndIncrement() % providers.length;
		return providers[index];
	}

	public String getCurrentWeb3jUrl(Chain chain) {
		Web3j[] providers = web3jInstances.get(chain);
		if (providers == null) return null;

		int index = currentIndex.get(chain).get()  % providers.length;
		return web3jUrls.get(chain)[index];  // 从映射中获取URL
	}
}