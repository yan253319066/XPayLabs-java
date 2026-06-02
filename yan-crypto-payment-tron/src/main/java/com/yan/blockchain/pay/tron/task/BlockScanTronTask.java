package com.yan.blockchain.pay.tron.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.blockchain.pay.tron.config.TronConfig;
import com.yan.xpay.domain.ErrorBlock;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.mapper.BlockHeightTrackerMapper;
import com.yan.blockchain.pay.tron.service.BlockScanTronService;
import com.yan.xpay.mapper.ErrorBlockMapper;
import com.yan.blockchain.pay.tron.service.TronService;
import com.yan.xpay.domain.BlockHeightTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.tron.trident.proto.Response.BlockExtention;

import java.math.BigInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BlockScanTronTask {

	private final TronService tronService;
	private final BlockHeightTrackerMapper blockHeightTrackerMapper;
	private final BlockScanTronService blockScanTronService;
	private final ErrorBlockMapper errorBlockMapper;
	private final ScheduledExecutorService scheduledExecutorService;
	private final TronConfig tronConfig;

	public void run() {
		tronConfig.getNetworks().forEach(chain -> {
			// 为每条链创建定时任务，使用各自配置的间隔
			scheduledExecutorService.scheduleAtFixedRate(()  -> {
				long startBlockNumber = getCurrentBlockHeight(chain);
				scanBlocks(chain, startBlockNumber);
			}, 1, 2, TimeUnit.SECONDS);
			log.info("开启[{}]扫描", chain);
		});
	}

	@EventListener(ApplicationReadyEvent.class)
	public void startOnReady() {
		run();
	}

	private long getCurrentBlockHeight(Chain chain) {
		BlockHeightTracker tracker = blockHeightTrackerMapper.selectOne(
			new LambdaQueryWrapper<BlockHeightTracker>()
				.eq(BlockHeightTracker::getChain, chain));

		if (tracker == null) {
			throw new RuntimeException("["+chain+"]找不到TRON链的高度跟踪器");
		}
		return tracker.getLastHeight();
	}

	private void scanBlocks(Chain chain, long startBlockNumber) {
		long currentBlockNumber = getCurrentBlockNumber(chain);

		for (long i = startBlockNumber + 1; i <= currentBlockNumber; i++) {
			try {
				BlockExtention block = tronService.getBlockByNum(chain, startBlockNumber);
				if(block == null) return;
//				log.info("[TRON]  扫描区块: {}, {} 笔交易", startBlockNumber, block.getTransactionsCount());
				processBlock(chain, block, startBlockNumber);
				startBlockNumber = i; // 更新已扫描区块
			} catch (Exception e) {
				log.error("[{}] startBlockNumber {} 区块扫描异常 {}", chain, startBlockNumber, e.getMessage());
				ErrorBlock errorBlock = errorBlockMapper.selectOne(new LambdaQueryWrapper<ErrorBlock>().eq(ErrorBlock::getChain, chain).eq(ErrorBlock::getBlockNumber, startBlockNumber));
				if(errorBlock == null) {
					errorBlock = new ErrorBlock();
					errorBlock.setBlockNumber(BigInteger.valueOf(startBlockNumber));
					errorBlock.setChain(chain);
					errorBlockMapper.insert(errorBlock);
				}
			}
		}
	}

	private long getCurrentBlockNumber(Chain chain) {
		return tronService.getNowBlock(chain).getBlockHeader().getRawData().getNumber();
	}

	private void processBlock(Chain chain, BlockExtention block, long blockNumber) {
		blockScanTronService.processBlockTransactions(chain, block);
		updateBlockHeight( chain,blockNumber + 1);
	}

	private void updateBlockHeight(Chain chain, long newHeight) {
		BlockHeightTracker tracker = new BlockHeightTracker();
		tracker.setChain(chain.name());
		tracker.setLastHeight(newHeight);
		blockHeightTrackerMapper.update(tracker, new LambdaQueryWrapper<BlockHeightTracker>()
			.eq(BlockHeightTracker::getChain, chain));
	}
}
