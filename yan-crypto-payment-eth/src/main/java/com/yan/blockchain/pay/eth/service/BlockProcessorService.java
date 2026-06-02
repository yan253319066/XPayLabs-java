package com.yan.blockchain.pay.eth.service;

import cn.hutool.core.util.StrUtil;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.config.SystemAddressCache;
import com.yan.xpay.domain.*;
import com.yan.xpay.event.TransactionEvent;
import com.yan.xpay.listener.BlockchainListener;
import com.yan.xpay.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockProcessorService {
	private final AssetTypeCache assetTypeCache;
	private final EthService ethService;

	private final BlockchainListener blockchainListener;
	private final SystemAddressCache systemAddressCache;
	private final EthComponent ethComponent;

	public void retryProcessBlock(ErrorBlock errorBlock) throws IOException {
		EthBlock.Block block = ethService.getBlock(errorBlock.getChain(), errorBlock.getBlockNumber());
		if (block == null) throw new RuntimeException(errorBlock.getChain()+ "-" +errorBlock.getBlockNumber()+" 区块为空");
		processBlock(errorBlock.getChain(), block);
	}

	/**
	 * 处理区块数据
	 */
	public void processBlock(Chain chain, EthBlock.Block block) {
//		log.info("{}  区块高度 {} 交易数量 {}",
//			chain, block.getNumber().longValue(),  block.getTransactions().size());

		block.getTransactions().forEach(tr  -> {
			EthBlock.TransactionObject txObj = (EthBlock.TransactionObject) tr.get();
			Transaction tx = convertToTransaction(chain, txObj, block.getTimestamp().longValue());
			if(tx == null) return;

//			if(t.getTo().equalsIgnoreCase("0xbFE53E085A751ccEC99C87F722617C2c0D29bB92")) {
//				log.info("--------");
//			}

			if(systemAddressCache.existAddress(tx.getChain(), tx.getFrom()) || systemAddressCache.existAddress(tx.getChain(), tx.getTo())) {
				TransactionEvent event = new TransactionEvent();
				event.setTx(tx);
				blockchainListener.onTransaction(event);

				//是否需要归集
				ethComponent.isCollect(tx);
			}

		});

		ethComponent.pendingConfirm(chain, block.getNumber().longValue());
	}

	/**
	 * 转换为标准交易格式
	 */
	private Transaction convertToTransaction(Chain chain,
		EthBlock.TransactionObject txObj, Long timestamp) {
		Transaction tx = new Transaction();
		String input = txObj.getInput();

		if (input == null || input.equals("0x"))  {
			// 原生代币转账
			if (txObj.getValue().compareTo(BigInteger.ZERO) > 0) {
				AssetType assetType = assetTypeCache.getByContractAddress(chain,  null);
				tx.setTo(txObj.getTo());
				tx.setAmount(new BigDecimal(txObj.getValue()));
				tx.setSymbol(assetType.getSymbol());
				tx.setDecimals(assetType.getDecimals());
			} else {
				return null;
			}
		} else if (txObj.getTo()  == null) {
			// 合约创建交易
			return null;
		} else {
			// 合约调用
			if (!parseContractInput(chain, txObj, tx)) return null;
			if (StrUtil.isBlank(tx.getTo()))  {
				return null;
			}
		}

		tx.setChain(chain);
		tx.setTxid(txObj.getHash());
		tx.setFrom(txObj.getFrom());
		tx.setTimestamp(timestamp);
		tx.setConfirmedNum(0);
		tx.setBlockNum(txObj.getBlockNumber().longValue());
		tx.setStatus(BlockchainStatus.PENDING);
		return tx;
	}

	private boolean parseContractInput(Chain chain, EthBlock.TransactionObject transactionObject, Transaction tx) {
		String input = transactionObject.getInput();
		if (input == null || input.equals("0x")  || input.length()  < 10) {
			return false;
		}

		AssetType assetType = assetTypeCache.getByContractAddress(chain,  transactionObject.getTo());
		if (assetType == null) {
			return false;
		}
		tx.setSymbol(assetType.getSymbol());
		tx.setDecimals(assetType.getDecimals());

		String selector = input.substring(0,  10);
		if ("0xa9059cbb".equals(selector)) {
			return parseERC20Transfer(transactionObject, tx);
		}
		return false;
	}

	private boolean parseERC20Transfer(EthBlock.TransactionObject transactionObject, Transaction tx) {
		String input = transactionObject.getInput();
		try {
			// 参数1：接收地址（跳过选择器和填充）
			String to = "0x" + input.substring(34,  74);
			// 参数2：金额
			BigInteger amount = new BigInteger(input.substring(74),  16);
			if (amount.compareTo(BigInteger.ZERO) > 0) {
				tx.setContractAddress(transactionObject.getTo());
				tx.setTo(to);
				tx.setAmount(new  BigDecimal(amount));
			}
		}catch (Exception e) {
			return false;
		}
		return true;
	}

}
