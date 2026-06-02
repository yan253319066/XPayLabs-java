package com.yan.blockchain.pay.tron.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.config.SystemAddressCache;
import com.yan.xpay.domain.*;
import com.yan.xpay.event.TransactionEvent;
import com.yan.xpay.listener.BlockchainListener;
import com.yan.xpay.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tron.trident.core.exceptions.IllegalException;
import org.tron.trident.core.utils.ByteArray;
import org.tron.trident.proto.Chain;
import org.tron.trident.proto.Contract;
import org.tron.trident.proto.Response;
import org.tron.trident.utils.Base58Check;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockScanTronService {
	private final AssetTypeCache assetTypeCache;
	private final TronService tronService;

	private final BlockchainListener blockchainListener;
	private final SystemAddressCache systemAddressCache;
	private final TronComponent tronComponent;

	public void retryProcessBlockTransactions(com.yan.xpay.enums.Chain chain, ErrorBlock errorBlock) throws IllegalException {
		Response.BlockExtention block = tronService.getBlockByNum(chain, errorBlock.getBlockNumber().longValue());
		if (block == null) throw new RuntimeException(errorBlock.getChain()+ "-" +errorBlock.getBlockNumber()+" 区块为空");
		processBlockTransactions(chain, block);
	}

	public void processBlockTransactions(com.yan.xpay.enums.Chain chain, Response.BlockExtention block) {

		List<Transaction> transactions = parseBlockTransactions(chain, block);
		if (CollectionUtils.isEmpty(transactions))  {
			return;
		}
		transactions.forEach(tx  -> {
			processTransaction(tx);
		});
	}

	private List<Transaction> parseBlockTransactions(com.yan.xpay.enums.Chain chain, Response.BlockExtention block) {

		tronComponent.pendingConfirm(chain, block.getBlockHeader().getRawData().getNumber());

		List<Transaction> transactions = new ArrayList<>();
		for (var txExtention : block.getTransactionsList())  {
			try {
				Transaction tx = parseTransaction(chain, txExtention, block);
				if (tx != null) {
					transactions.add(tx);
				}
			} catch (Exception e) {
				throw new RuntimeException("解析交易失败:", e);
			}
		}
		return transactions;
	}

	private Transaction parseTransaction(com.yan.xpay.enums.Chain chain, Response.TransactionExtention txExtention, Response.BlockExtention block) throws InvalidProtocolBufferException {
		String txId = org.tron.trident.core.utils.ByteArray.toHexString(txExtention.getTxid().toByteArray());
		Chain.Transaction transaction = txExtention.getTransaction();
		//if (transaction.getRetList().get(0).getContractRet() != Chain.Transaction.Result.contractResult.SUCCESS) continue;

		var contract = transaction.getRawData().getContract(0);
		Any parameter = contract.getParameter();
		Transaction trx = new Transaction();
		trx.setChain(chain);
		trx.setTxid(txId);
		trx.setBlockNum(block.getBlockHeader().getRawData().getNumber());

		switch (contract.getType()) {
			case TriggerSmartContract -> {
				var deployContract = parameter.unpack(Contract.TriggerSmartContract.class);
				trx.setFrom(Base58Check.bytesToBase58(deployContract.getOwnerAddress().toByteArray()));
				trx.setContractAddress(Base58Check.bytesToBase58(deployContract.getContractAddress().toByteArray()));

				// 解析TRC20转账数据
				String dataHex = ByteArray.toHexString(deployContract.getData().toByteArray());

				// TRC20转账数据格式: a9059cbb + 接收地址(32字节) + 金额(32字节)
				if (dataHex.startsWith("a9059cbb")  && dataHex.length()  >= 72) {
					String toAddressHex = dataHex.substring(8,  72);
					String amountHex = dataHex.substring(72);

					// 转换接收地址
					String toAddress = "41" + toAddressHex.substring(24);  // 去掉前面的12字节0
					byte[] addressBytes = ByteArray.fromHexString(toAddress);
					String toAddressBase58 = Base58Check.bytesToBase58(addressBytes);

					if(StrUtil.isBlank(amountHex)) return null;
					// 转换金额
					BigInteger amount = new BigInteger(amountHex, 16);

					if (amount.compareTo(BigInteger.ZERO)  <= 0) {
//						log.warn("转账金额无效");
						return null;
					}

					trx.setTo(toAddressBase58);
					trx.setAmount(new BigDecimal(amount));
					trx.setContractAddress(Base58Check.bytesToBase58(deployContract.getContractAddress().toByteArray()));
					AssetType assetType = assetTypeCache.getByContractAddress(chain,  trx.getContractAddress());
					if(assetType == null) return null;
					trx.setSymbol(assetType.getSymbol());
					trx.setDecimals(assetType.getDecimals());
				}else {
					//log.info("不是TRC20转账");
					return null;
				}


			}
			case TransferContract -> {
				var deployContract = parameter.unpack(Contract.TransferContract.class);
				trx.setFrom(Base58Check.bytesToBase58(deployContract.getOwnerAddress().toByteArray()));
				trx.setTo(Base58Check.bytesToBase58(deployContract.getToAddress().toByteArray()));
				trx.setAmount(new BigDecimal(deployContract.getAmount()));
				AssetType assetType = assetTypeCache.getByContractAddress(chain,  null);
				trx.setSymbol(assetType.getSymbol());
				trx.setDecimals(assetType.getDecimals());
			}
			default -> {
				return null;
			}
		}
		trx.setTimestamp(block.getBlockHeader().getRawData().getTimestamp());
		trx.setStatus(BlockchainStatus.PENDING);
		return trx;
	}

	private void processTransaction(Transaction tx) {
//		if(tx.getTo().equalsIgnoreCase("TTh7NarV9xkpBLuXX6ZiUGBA7vBPx6Qi9Z")){
//			log.info("--");
//		}

		if(systemAddressCache.existAddress(tx.getChain(), tx.getFrom()) || systemAddressCache.existAddress(tx.getChain(), tx.getTo())) {
			TransactionEvent event = new TransactionEvent();
			event.setTx(tx);
			blockchainListener.onTransaction(event);

			//是否需要归集
			tronComponent.isCollect(tx);
		}

	}
}
