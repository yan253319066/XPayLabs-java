package com.yan.xpay.sui.service;

import com.yan.xpay.config.AssetTypeCache;
import com.yan.xpay.domain.AssetType;
import com.yan.xpay.domain.Transaction;
import com.yan.xpay.event.CollectEvent;
import com.yan.xpay.listener.BlockchainListener;
import com.yan.xpay.sui.utils.SuiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class SuiComponent {
	private final AssetTypeCache assetTypeCache;
	private final BlockchainListener blockchainListener;
	public void isCollect(Transaction tx) {
		AssetType assetType = assetTypeCache.getBySymbol(tx.getChain(), tx.getSymbol());
		BigDecimal balance = SuiService.getBalance(tx.getTo(), assetType.getContractAddress(), SuiUtils.getNetwork(tx.getChain()));
		CollectEvent collectEvent = new CollectEvent();
		collectEvent.setChain(tx.getChain());
		collectEvent.setSymbol(tx.getSymbol());
		collectEvent.setAddress(tx.getTo());
		collectEvent.setBalance(balance);
		blockchainListener.onCollect(collectEvent);
	}
}
