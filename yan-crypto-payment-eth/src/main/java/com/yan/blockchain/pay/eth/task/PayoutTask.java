package com.yan.blockchain.pay.eth.task;

import com.yan.xpay.enums.Chain;
import com.yan.blockchain.pay.eth.config.EthConfig;
import com.yan.blockchain.pay.eth.service.PayoutService;
import com.yan.xpay.service.IPaymentOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutTask {
	private final PayoutService payoutService;
	private final IPaymentOrderService paymentOrderService;
	private final Map<Chain, EthConfig.ChainConfig> chainConfigs;
	@Scheduled(fixedDelay = 2000, initialDelay = 2000)
	public void run() {
		paymentOrderService.getPayoutInit(chainConfigs.keySet()).forEach(order -> {
			try {
				payoutService.payout(order);
			} catch (Exception e) {
				log.error("[{}] PayoutTask error", order.getChain(), e);
			}
		});
	}

}
