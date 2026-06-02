package com.yan.xpay.sui.service;

import cn.hutool.json.JSONUtil;
import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.config.SystemAddressCache;
import com.yan.xpay.domain.AssetType;
import com.yan.xpay.domain.ErrorBlock;
import com.yan.xpay.domain.Transaction;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.event.TransactionEvent;
import com.yan.xpay.listener.BlockchainListener;
import com.yan.xpay.sui.config.SuiConfig;
import com.yan.xpay.sui.model.SuiTransaction;
import com.yan.xpay.sui.utils.SuiUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockProcessorService {

	private final AssetTypeCache assetTypeCache;
	private final SystemAddressCache systemAddressCache;
	private final BlockchainListener blockchainListener;
	private final SuiComponent suiComponent;
	private final SuiConfig suiConfig;

	private static BlockchainStatus getBlockchainStatus(String status) {
		return switch (status) {
			case "success" -> BlockchainStatus.SUCCESS;
			case "failure" -> BlockchainStatus.FAILED;
			default -> BlockchainStatus.FAILED;
		};
	}

	public Boolean retryProcessBlock(ErrorBlock errorBlock) {
		return processBlock(errorBlock.getChain(), errorBlock.getBlockNumber().toString());
	}

	/**
	 * 处理区块数据（外部入口）
	 */
	public Boolean processBlock(Chain chain, String sequenceNumber) {
//			log.info("start process block {} {}", chain, sequenceNumber);
		List<SuiTransaction> transactions = suiConfig.executeWithRateLimit(()-> SuiService.queryAllTransactionBlocks(SuiUtils.getNetwork(chain), JSONUtil.createObj().set("Checkpoint", sequenceNumber), 100));
//			List<SuiTransaction> transactions = SuiService.queryAllTransactionBlocks(SuiUtils.getNetwork(chain), JSONUtil.createObj().set("Checkpoint", sequenceNumber), 100);
		transactions.forEach(st -> {
			Optional<Transaction> optTx = toTransaction(st, chain);
			optTx.ifPresent(tx -> {
				if(systemAddressCache.existAddress(tx.getChain(), tx.getFrom()) || systemAddressCache.existAddress(tx.getChain(), tx.getTo())) {
					if("0x2::sui::SUI".equals(tx.getContractAddress())) tx.setContractAddress(null);
					AssetType assetType = assetTypeCache.getByContractAddress(chain, tx.getContractAddress());
					if (assetType == null) {
						log.warn("不支持的 chain {} symbol {} ", tx.getChain(), tx.getSymbol());
						return;
					}
					tx.setSymbol(assetType.getSymbol());
					tx.setDecimals(assetType.getDecimals());
					tx.setStatus(getBlockchainStatus(st.getEffects().getStatus().getStatus()));
					log.info("start tx {} ", tx);
					TransactionEvent event = new TransactionEvent();
					event.setTx(tx);
					blockchainListener.onTransaction(event);

					//是否需要归集
					suiComponent.isCollect(tx);
				}

			});
		});
		return true;
	}

	/**
	 * 判断是否是 SUI/USDT/USDC 转账，并转换成 Transaction Bean
	 *
	 * 顺序：
	 * 1. events
	 * 2. balanceChanges
	 */
	public static Optional<Transaction> toTransaction(SuiTransaction suiTx, Chain chain) {
		// Step 1：事件解析（比 balanceChanges 更准确）
		//不使用events解析
//		Optional<Transaction> tx = parseEventTransfer(suiTx, chain);
//		if (tx.isPresent()) return tx;

		// Step 2：balanceChanges 兜底（Testnet 常出现失败，需要 try-catch）
		return parseBalanceChangeTransfer(suiTx, chain);
	}

	/* ============================================================
	 * 1. 事件解析
	 * ============================================================ */
	private static Optional<Transaction> parseEventTransfer(SuiTransaction tx, Chain chain) {
		List<SuiTransaction.Event> events = tx.getEvents();
		if (events == null) return Optional.empty();

		for (SuiTransaction.Event ev : events) {
			Map<String, Object> json = ev.getParsedJson();
			if (json == null) continue;

			// parsedJson 中必须同时包含转账核心字段
			if (!json.containsKey("sender") || !json.containsKey("recipient") || !json.containsKey("amount"))
				continue;

			String sender = (String) json.get("sender");
			String recipient = (String) json.get("recipient");
			Object amountRaw = json.get("amount");
			if (amountRaw == null) continue;

			BigDecimal amount = new BigDecimal(amountRaw.toString());

			// coinType 尝试从多种来源获得
			String coinType = getCoinTypeFromEvent(ev);

			if (coinType == null) {
				// SuiTransferredEvent 等特殊事件，默认 SUI
				log.info("coinType 为null: {}", ev.getType());
			}

			Transaction t = new Transaction();
			t.setChain(chain);
			t.setContractAddress(coinType);
			t.setFrom(sender);
			t.setTo(recipient);
			t.setAmount(amount);
			t.setTimestamp(tx.getTimestampMs());
			t.setTxid(tx.getDigest());
			t.setStatus(BlockchainStatus.SUCCESS);
			t.setConfirmedNum(0);
			t.setBlockNum(tx.getCheckpoint());
			SuiTransaction.GasUsed gasUsed = tx.getEffects().getGasUsed();
			BigDecimal txGas = gasUsed.getComputationCost().add(gasUsed.getStorageCost()).subtract(gasUsed.getStorageRebate());
			if (txGas.compareTo(BigDecimal.ZERO) < 0) txGas = gasUsed.getComputationCost();
			t.setTxGas(txGas);

			return Optional.of(t);
		}

		return Optional.empty();
	}

	/**
	 * 获取 coinType（事件结构差异大，必须容错处理）
	 */
	private static String getCoinTypeFromEvent(SuiTransaction.Event ev) {
		String type = ev.getType();

		// 常规事件：0x2::coin::Coin<0x2::sui::SUI>
		if (type != null && type.contains("Coin<")) {
			return type.substring(type.indexOf("Coin<") + 5, type.lastIndexOf(">"));
		}

		// 某些事件放 parsedJson，如 coinType: "0x2::sui::SUI"
		if (ev.getParsedJson() != null && ev.getParsedJson().containsKey("coinType")) {
			return (String) ev.getParsedJson().get("coinType");
		}

		// 特殊事件（如 SuiTransferredEvent）可能不包含 coin 信息
		return null;
	}

	/* ============================================================
	 * 2. balanceChanges 兜底（Testnet / Mainnet 均适用）
	 * ============================================================ */
	private static Optional<Transaction> parseBalanceChangeTransfer(SuiTransaction tx, Chain chain) {
		if (tx.getBalanceChanges() == null || tx.getBalanceChanges().isEmpty()) return Optional.empty();

		String sender = tx.getTransaction().getData().getSender();

		for (SuiTransaction.BalanceChange bc : tx.getBalanceChanges()) {
			if (bc == null) continue;

			String coinType = bc.getCoinType();
			BigDecimal amount;

			try {
				amount = new BigDecimal(bc.getAmount());
			} catch (Exception e) {
				log.error("parseBalanceChangeTransfer amount error: {}", JSONUtil.toJsonStr(bc));
				continue;
			}

			// 跳过负数变化
			if (amount.compareTo(BigDecimal.ZERO) <= 0) {
//				log.info("跳过负数变化: {}", amount);
				continue;
			}

			String to = bc.getOwner() != null ? bc.getOwner().getAddressOwner() : null;

			Transaction t = new Transaction();
			t.setChain(chain);
			t.setContractAddress(coinType);
			t.setFrom(sender);
			t.setTo(to);
			t.setAmount(amount);
			t.setTimestamp(tx.getTimestampMs());
			t.setTxid(tx.getDigest());
			t.setStatus(BlockchainStatus.SUCCESS);
			t.setConfirmedNum(0);
			t.setBlockNum(tx.getCheckpoint());
			SuiTransaction.GasUsed gasUsed = tx.getEffects().getGasUsed();
			BigDecimal txGas = gasUsed.getComputationCost().add(gasUsed.getStorageCost()).subtract(gasUsed.getStorageRebate());
			if (txGas.compareTo(BigDecimal.ZERO) < 0) txGas = gasUsed.getComputationCost();
			t.setTxGas(txGas);

			return Optional.of(t);
		}

		return Optional.empty();
	}

}
