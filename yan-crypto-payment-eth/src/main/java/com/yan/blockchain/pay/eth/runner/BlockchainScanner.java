package com.yan.blockchain.pay.eth.runner;

import cn.hutool.json.JSONUtil;
import com.yan.xpay.enums.Chain;
import com.yan.blockchain.pay.eth.config.EthConfig;
import com.yan.blockchain.pay.eth.EvmBlockScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BlockchainScanner {
	private final EvmBlockScanner evmBlockScanner;
	private final Map<Chain, Long> scanIntervals;  // <Chain, 扫描间隔(秒)>
	private final ScheduledExecutorService scheduledExecutorService;

	public BlockchainScanner(EvmBlockScanner evmBlockScanner,
		Map<Chain, EthConfig.ChainConfig> chainConfigs, ScheduledExecutorService scheduledExecutorService) {
		this.evmBlockScanner  = evmBlockScanner;

		// 从配置中提取扫描间隔
		this.scanIntervals  = chainConfigs.entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> e.getValue().getScanIntervalSeconds()
			));
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public void run() {
//		log.info("scanIntervals: {}", JSONUtil.toJsonStr(scanIntervals));
		scanIntervals.forEach((chain,  interval) -> {
			// 为每条链创建定时任务，使用各自配置的间隔
			scheduledExecutorService.scheduleAtFixedRate(()  -> {
				evmBlockScanner.scanChain(chain);
			}, 1, interval, TimeUnit.SECONDS);
			log.info("开启[{}]扫描", chain);
		});
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startOnReady() {
		run();
	}

}