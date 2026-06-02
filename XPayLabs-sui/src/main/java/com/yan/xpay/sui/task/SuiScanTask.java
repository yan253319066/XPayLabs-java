package com.yan.xpay.sui.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.BlockHeightTracker;
import com.yan.xpay.domain.ErrorBlock;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.mapper.BlockHeightTrackerMapper;
import com.yan.xpay.mapper.ErrorBlockMapper;
import com.yan.xpay.sui.config.SuiConfig;
import com.yan.xpay.sui.service.BlockProcessorService;
import com.yan.xpay.sui.service.SuiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.math.BigInteger;
import java.util.concurrent.Executor;

import static com.yan.xpay.sui.utils.SuiUtils.getNetwork;

@Slf4j
public class SuiScanTask {

	private final SuiConfig suiConfig;
	private final BlockHeightTrackerMapper blockHeightTrackerMapper;
	private final ErrorBlockMapper errorBlockMapper;
	private final BlockProcessorService blockProcessorService;
	private final Executor suiBlockExecutor; // 异步处理区块线程池

	private static final BigInteger STEP = BigInteger.valueOf(500);

	public SuiScanTask(SuiConfig suiConfig, BlockHeightTrackerMapper blockHeightTrackerMapper, ErrorBlockMapper errorBlockMapper, BlockProcessorService blockProcessorService,
		Executor suiBlockExecutor) {
		this.suiConfig = suiConfig;
		this.blockHeightTrackerMapper = blockHeightTrackerMapper;
		this.errorBlockMapper = errorBlockMapper;
		this.blockProcessorService = blockProcessorService;
		this.suiBlockExecutor = suiBlockExecutor;
	}

	/**
	 * 公共方法，传入链类型即可扫描
	 */
	public void scanBlocks(Chain chain) {
		BigInteger latestBlock;
		try {
			latestBlock = suiConfig.executeWithRateLimit(
				() -> SuiService.getLatestCheckpointSequenceNumber(getNetwork(chain))
			);
		} catch (Exception e) {
			log.warn("链 {} 获取最新区块高度失败: {}", chain, e.getMessage());
			return;
		}

		long currentHeight = getCurrentBlockHeight(chain);
		BigInteger startBlock = BigInteger.valueOf(currentHeight + 1);
		BigInteger endBlock = startBlock.add(STEP).min(latestBlock);

		if (startBlock.compareTo(endBlock) > 0) return;

		for (BigInteger blockNumber = startBlock; blockNumber.compareTo(endBlock) <= 0; blockNumber = blockNumber.add(BigInteger.ONE)) {
			BigInteger finalBlockNumber = blockNumber;
			// 异步处理区块
			suiBlockExecutor.execute(() -> processBlockAsync(chain, finalBlockNumber));
		}

//		log.info("[{}] 批次触发 {} -> {}", chain, startBlock, endBlock);
	}

	@Async
	public void processBlockAsync(Chain chain, BigInteger blockNumber) {
		try {
			boolean success = blockProcessorService.processBlock(chain, blockNumber.toString());
			if (success) updateBlockHeight(chain, blockNumber.longValue());
			else recordErrorBlock(chain, blockNumber);
		} catch (Exception e) {
			log.error("链 {} 处理区块 {} 出错", chain, blockNumber, e);
			recordErrorBlock(chain, blockNumber);
		}
	}

	private long getCurrentBlockHeight(Chain chain) {
		BlockHeightTracker tracker = blockHeightTrackerMapper.selectOne(
			new LambdaQueryWrapper<BlockHeightTracker>().eq(BlockHeightTracker::getChain, chain.name())
		);
		if (tracker == null) throw new RuntimeException("找不到 " + chain + " 链的高度跟踪器");
		return tracker.getLastHeight();
	}

	private void updateBlockHeight(Chain chain, long newHeight) {
		BlockHeightTracker tracker = new BlockHeightTracker();
		tracker.setChain(chain.name());
		tracker.setLastHeight(newHeight);
		blockHeightTrackerMapper.update(
			tracker,
			new LambdaQueryWrapper<BlockHeightTracker>()
				.eq(BlockHeightTracker::getChain, chain.name())
				.lt(BlockHeightTracker::getLastHeight, newHeight)
		);
	}

	private void recordErrorBlock(Chain chain, BigInteger blockNumber) {
		ErrorBlock errorBlock = errorBlockMapper.selectOne(
			new LambdaQueryWrapper<ErrorBlock>()
				.eq(ErrorBlock::getChain, chain.name())
				.eq(ErrorBlock::getBlockNumber, blockNumber)
		);
		if (errorBlock == null) {
			errorBlock = new ErrorBlock();
			errorBlock.setChain(chain);
			errorBlock.setBlockNumber(blockNumber);
			errorBlockMapper.insert(errorBlock);
		}
	}
}