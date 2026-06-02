package com.yan.xpay.listener;

import com.yan.xpay.event.MerchantNotifyEvent;
import com.yan.xpay.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MerchantNotifyListener {
	private final WebhookService webhookService;
	@Async
	@EventListener
	public void handleMerchantNotification(MerchantNotifyEvent event){
		webhookService.notifyMerchant(event.getNotifyMerchant());
	}

}
