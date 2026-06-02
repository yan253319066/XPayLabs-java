package com.yan.xpay.sui.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.ErrorBlock;
import com.yan.xpay.mapper.ErrorBlockMapper;
import com.yan.xpay.sui.config.SuiConfig;
import com.yan.xpay.sui.service.BlockProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuiErrorBlockTask {
	private final ErrorBlockMapper errorBlockMapper;
	private final BlockProcessorService blockProcessorService;
	private final SuiConfig suiConfig;

	@Scheduled(fixedDelay = 3000, initialDelay = 2000)
	public void run() {
		List<ErrorBlock> list = errorBlockMapper.selectList(new LambdaQueryWrapper<ErrorBlock>().in(ErrorBlock::getChain, suiConfig.getNetworks()));
		list.forEach(errorBlock -> {
			boolean b = blockProcessorService.retryProcessBlock(errorBlock);
			if (b) errorBlockMapper.deleteById(errorBlock.getId());

		});
	}
}
