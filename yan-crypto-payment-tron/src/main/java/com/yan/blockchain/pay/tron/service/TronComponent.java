package com.yan.blockchain.pay.tron.service;

import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.config.PendingTransactionCache;
import com.yan.xpay.domain.AssetType;
import com.yan.xpay.domain.Transaction;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.event.CollectEvent;
import com.yan.xpay.event.TransactionEvent;
import com.yan.xpay.listener.BlockchainListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tron.trident.proto.Response;

import java.math.BigDecimal;
import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class TronComponent {
	private final AssetTypeCache assetTypeCache;
	private final TronService tronService;
	private final BlockchainListener blockchainListener;
	private final PendingTransactionCache pendingTransactionCache;

	public void isCollect(Transaction tx) {
		boolean isNativeToken = assetTypeCache.isNativeToken(tx.getChain(), tx.getSymbol());
		BigInteger balance;
		if(isNativeToken){
			balance = BigInteger.valueOf(tronService.getTrxBalance(tx.getChain(), tx.getTo()));
		}else {
			balance = tronService.getTrc20Balance(tx.getChain(), tx.getTo(), tx.getContractAddress());
		}
		CollectEvent collectEvent = new CollectEvent();
		collectEvent.setChain(tx.getChain());
		collectEvent.setSymbol(tx.getSymbol());
		collectEvent.setAddress(tx.getTo());
		collectEvent.setBalance(new BigDecimal(balance));
		blockchainListener.onCollect(collectEvent);
	}

	public void pendingConfirm(com.yan.xpay.enums.Chain chain, long blockNumber){
		pendingTransactionCache.getPendingTransactionList(chain).forEach((txid, tx) -> {
			long confirmations = blockNumber - tx.getBlockNum();
			AssetType assetType = assetTypeCache.getBySymbol(tx.getChain(), tx.getSymbol());
			if(confirmations >= assetType.getConfirmedNum()) {
				Response.TransactionInfo txInfo = tronService.getTransactionInfoById(tx.getChain(), tx.getTxid());
				if(txInfo != null && Response.TransactionInfo.code.SUCESS.equals(txInfo.getResult()) && tx.getBlockNum() == txInfo.getBlockNumber()){
					tx.setStatus(BlockchainStatus.SUCCESS);
				}else {
					tx.setStatus(BlockchainStatus.FAILED);
				}
				if(txInfo != null)
					tx.setTxGas(new BigDecimal(txInfo.getFee()));
				tx.setConfirmedNum((int)confirmations);
				TransactionEvent event = new TransactionEvent();
				event.setTx(tx);
				blockchainListener.onTransaction(event);

			}
		});
	}
}
