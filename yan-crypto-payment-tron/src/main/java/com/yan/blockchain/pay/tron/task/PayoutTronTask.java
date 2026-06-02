package com.yan.blockchain.pay.tron.task;

import com.yan.blockchain.pay.tron.config.TronConfig;
import com.yan.xpay.service.IPaymentOrderService;
import com.yan.blockchain.pay.tron.service.PayoutTronService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutTronTask {
	private final IPaymentOrderService paymentOrderService;
	private final PayoutTronService payoutTronService;
	private final TronConfig tronConfig;
	@Scheduled(fixedDelay = 3000, initialDelay = 2000)
	public void run() {
		paymentOrderService.getPayoutInit(tronConfig.getNetworks()).forEach(order -> {
			try {
				payoutTronService.payout(order);
			} catch (Exception e) {
				log.error("PayoutTronTask error", e);
			}
		});
	}
}
