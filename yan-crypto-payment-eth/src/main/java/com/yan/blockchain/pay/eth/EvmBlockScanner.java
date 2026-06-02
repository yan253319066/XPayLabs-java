package com.yan.blockchain.pay.eth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.blockchain.pay.eth.service.EthService;
import com.yan.xpay.domain.ErrorBlock;
import com.yan.xpay.enums.Chain;
import com.yan.blockchain.pay.eth.service.BlockProcessorService;
import com.yan.xpay.mapper.BlockHeightTrackerMapper;
import com.yan.xpay.domain.BlockHeightTracker;
import com.yan.xpay.mapper.ErrorBlockMapper;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.exceptions.ClientConnectionException;

import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EvmBlockScanner {
	private final BlockHeightTrackerMapper blockHeightTrackerMapper;
	private final BlockProcessorService blockProcessorService;
	private final EthService ethService;
	private final ErrorBlockMapper errorBlockMapper;

	private static final BigInteger STEP = BigInteger.valueOf(100);

	private final Map<Chain, BigInteger> currentHeights = new ConcurrentHashMap<>();
	private final Map<Chain, String> currentRpcs = new ConcurrentHashMap<>();

	public BigInteger getCurrentScanHeight(Chain chain) {
		return currentHeights.getOrDefault(chain,  BigInteger.ZERO);
	}
	public String getCurrentScanRpc(Chain chain) {
		return currentRpcs.getOrDefault(chain,  "null ");
	}

	public EvmBlockScanner(
		BlockHeightTrackerMapper blockHeightTrackerMapper,
		BlockProcessorService blockProcessorService,
		EthService ethService,
		ErrorBlockMapper errorBlockMapper) {
		this.blockHeightTrackerMapper  = blockHeightTrackerMapper;
		this.blockProcessorService = blockProcessorService;
		this.ethService = ethService;
		this.errorBlockMapper = errorBlockMapper;
	}

	/**
	 * 扫描指定链的区块
	 */
	public void scanChain(Chain chain) {
		BigInteger latestBlock;
		try {
			latestBlock = ethService.getNowBlockNumber(chain);
		} catch (RuntimeException e) {
			log.warn(" 链 {} rpc {} 获取最新区块高度出错: {}", chain, getCurrentScanRpc(chain), e.getMessage());
			return;
		}
		BigInteger startBlock = BigInteger.valueOf(getCurrentBlockHeight(chain)).add(BigInteger.ONE);
		// 每次最多扫描100个区块（避免超时）
		BigInteger endBlock = startBlock.add(STEP).min(latestBlock);

		for (BigInteger blockNumber = startBlock;
			 blockNumber.compareTo(endBlock)  <= 0;
			 blockNumber = blockNumber.add(BigInteger.ONE))  {
			try {
				currentHeights.put(chain,  blockNumber);
				currentRpcs.put(chain,ethService.getCurrentWeb3jUrl(chain));
				EthBlock.Block block = ethService.getBlock(chain, blockNumber);
				if (block == null) return;
				blockProcessorService.processBlock(chain, block);

				updateBlockHeight(chain, blockNumber.longValue());
			} catch (SocketTimeoutException | ClientConnectionException e) {
				log.error(" 链 {} 高度 {} rpc {} 扫描出错 SocketTimeoutException : {}", chain, blockNumber, getCurrentScanRpc(chain), e.getMessage());
				blockNumber = blockNumber.subtract(BigInteger.ONE);
			} catch (Exception e) {
				log.error(" 链 {} 高度 {} rpc {} 扫描出错: ", chain, blockNumber, getCurrentScanRpc(chain), e);
				log.info("错误区块高度 {} 加入errorBlock", blockNumber);
				ErrorBlock errorBlock = errorBlockMapper.selectOne(new LambdaQueryWrapper<ErrorBlock>().eq(ErrorBlock::getChain, chain).eq(ErrorBlock::getBlockNumber, blockNumber));
				if(errorBlock == null) {
					errorBlock = new ErrorBlock();
					errorBlock.setBlockNumber(blockNumber);
					errorBlock.setChain(chain);
					errorBlockMapper.insert(errorBlock);
				}
			}
		}
	}

	private long getCurrentBlockHeight(Chain chain) {
		BlockHeightTracker tracker = blockHeightTrackerMapper.selectOne(
			new LambdaQueryWrapper<BlockHeightTracker>()
				.eq(BlockHeightTracker::getChain, chain));
		if (tracker == null) {
			throw new RuntimeException("找不到"+chain+"链的高度跟踪器");
		}
		return tracker.getLastHeight();
	}

	private void updateBlockHeight(Chain chain, long newHeight) {
		BlockHeightTracker tracker = new BlockHeightTracker();
		tracker.setChain(chain.name());
		tracker.setLastHeight(newHeight);
		blockHeightTrackerMapper.update(tracker,  new LambdaQueryWrapper<BlockHeightTracker>()
			.eq(BlockHeightTracker::getChain, chain));
	}

}