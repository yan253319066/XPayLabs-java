package com.yan.blockchain.pay.eth.task;

import com.yan.blockchain.pay.eth.config.EthConfig;
import com.yan.blockchain.pay.eth.service.EthService;
import com.yan.xpay.constant.RedisConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
@RequiredArgsConstructor
public class GasTask {

	private final EthService ethService;
	private final EthConfig ethConfig;

	@Scheduled(fixedDelay = 1000 * 60 * 5, initialDelay = 2000)
	public void run(){
		ethConfig.getNetworks().forEach((chain)-> {
			try {
				BigDecimal gas = ethService.calculateGas(chain, "0xdAC17F958D2ee523a2206206994597C13D831ec7");
				RedisUtils.setCacheMapValue(RedisConstant.GAS_KEY, chain.name(), getGas(gas, new BigDecimal("20")));
			} catch (IOException e) {
				log.error("[{}] gas获取失败 {}", chain, e.getMessage());
			}
		});
	}

	private BigDecimal getGas(BigDecimal original, BigDecimal percentage) {
		// 计算逻辑：100 × (1 + 20/100) = 100 × 1.2
		BigDecimal result = original.multiply(
			BigDecimal.ONE.add(percentage.divide(new BigDecimal("100"), 6, RoundingMode.DOWN))
		);
		return result;
	}
}
