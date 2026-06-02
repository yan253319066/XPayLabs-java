package com.yan.blockchain.pay.eth.service;

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
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;

@Component
@RequiredArgsConstructor
public class EthComponent {
	private final AssetTypeCache assetTypeCache;
	private final EthService ethService;
	private final BlockchainListener blockchainListener;
	private final PendingTransactionCache pendingTransactionCache;

	public void isCollect(Transaction tx) {
		boolean isNativeToken = assetTypeCache.isNativeToken(tx.getChain(), tx.getSymbol());
		BigInteger balance;
		if(isNativeToken){
			balance = ethService.getBalance(tx.getChain(), tx.getTo());
		}else {
			balance = ethService.getErc20Balance(tx.getChain(), tx.getContractAddress(), tx.getTo());
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
				TransactionReceipt receipt = ethService.getReceipt(tx.getChain(), tx.getTxid());
				if(receipt != null && receipt.isStatusOK() && tx.getBlockNum() == receipt.getBlockNumber().longValue()){
					tx.setStatus(BlockchainStatus.SUCCESS);
				}else {
					tx.setStatus(BlockchainStatus.FAILED);
				}
				if(receipt != null) {
					BigInteger txGas = ethService.calculateTxFee(receipt);
					tx.setTxGas(new BigDecimal(txGas));
				}
				tx.setConfirmedNum((int)confirmations);
				TransactionEvent event = new TransactionEvent();
				event.setTx(tx);
				blockchainListener.onTransaction(event);

			}
		});
	}
}
