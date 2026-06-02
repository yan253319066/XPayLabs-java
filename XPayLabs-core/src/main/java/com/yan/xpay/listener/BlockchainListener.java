package com.yan.xpay.listener;

import com.yan.xpay.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlockchainListener {
	private final BlockchainEventPublisher blockchainEventPublisher;

	public void onTransaction(TransactionEvent event) {
		blockchainEventPublisher.publisherTransactionEvent(event);
	}

	/**
	 * 是否需要归集监听
	 * @param event
	 */
	public void onCollect(CollectEvent event) {
		blockchainEventPublisher.publisherCollectEvent(event);
	}
}
