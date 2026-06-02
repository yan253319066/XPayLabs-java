package com.yan.blockchain.pay.eth.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.blockchain.pay.eth.config.EthConfig;
import com.yan.blockchain.pay.eth.service.BlockProcessorService;
import com.yan.xpay.domain.ErrorBlock;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.mapper.ErrorBlockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorBlockTask {
	private final ErrorBlockMapper errorBlockMapper;
	private final BlockProcessorService blockProcessorService;
	private final Map<Chain, EthConfig.ChainConfig> chainConfigs;

	@Scheduled(fixedDelay = 3000, initialDelay = 2000)
	public void run() {
		List<ErrorBlock> list = errorBlockMapper.selectList(new LambdaQueryWrapper<ErrorBlock>().in(ErrorBlock::getChain, chainConfigs.keySet()));
		list.forEach(errorBlock -> {
			try {
				blockProcessorService.retryProcessBlock(errorBlock);
				errorBlockMapper.deleteById(errorBlock.getId());
			} catch (Exception e) {
				log.error("重试扫描区块报错 {} {}", errorBlock.getChain(), errorBlock.getBlockNumber(), e);
			}
		});
	}
}
