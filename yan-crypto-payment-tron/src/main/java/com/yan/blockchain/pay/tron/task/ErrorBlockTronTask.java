package com.yan.blockchain.pay.tron.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.blockchain.pay.tron.config.TronConfig;
import com.yan.blockchain.pay.tron.service.BlockScanTronService;
import com.yan.xpay.domain.ErrorBlock;
import com.yan.xpay.mapper.ErrorBlockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorBlockTronTask {
	private final ErrorBlockMapper errorBlockMapper;
	private final TronConfig tronConfig;
	private final BlockScanTronService blockScanTronService;

	@Scheduled(fixedDelay = 3000, initialDelay = 2000)
	public void run() {
		List<ErrorBlock> list = errorBlockMapper.selectList(new LambdaQueryWrapper<ErrorBlock>().in(ErrorBlock::getChain, tronConfig.getNetworks()));
		list.forEach(errorBlock -> {
			try {
				blockScanTronService.retryProcessBlockTransactions(errorBlock.getChain(), errorBlock);
				errorBlockMapper.deleteById(errorBlock.getId());
			} catch (Exception e) {
				log.error("重试扫描区块报错 {} {}", errorBlock.getChain(), errorBlock.getBlockNumber(), e);
			}
		});
	}
}
