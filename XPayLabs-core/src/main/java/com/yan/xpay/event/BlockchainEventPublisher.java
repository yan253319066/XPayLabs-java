package com.yan.xpay.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BlockchainEventPublisher {
	private final ApplicationEventPublisher eventPublisher;

	public void publisherTransactionEvent(TransactionEvent event) {
		eventPublisher.publishEvent(event);
	}
	public void publisherCollectEvent(CollectEvent event) {
		eventPublisher.publishEvent(event);
	}
}
