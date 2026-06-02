package com.yan.xpay.sui.task;

import com.yan.xpay.service.IPaymentOrderService;
import com.yan.xpay.sui.config.SuiConfig;
import com.yan.xpay.sui.service.SuiPayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuiPayoutTask {
	private final SuiPayoutService suiPayoutService;
	private final IPaymentOrderService paymentOrderService;
	private final SuiConfig suiConfig;
	@Scheduled(fixedDelay = 2000, initialDelay = 2000)
	public void run() {
		paymentOrderService.getPayoutInit(suiConfig.getNetworks()).forEach(order -> {
			try {
				suiPayoutService.payout(order);
			} catch (Exception e) {
				log.error("[{}] PayoutTask error", order.getChain(), e);
			}
		});
	}

}
