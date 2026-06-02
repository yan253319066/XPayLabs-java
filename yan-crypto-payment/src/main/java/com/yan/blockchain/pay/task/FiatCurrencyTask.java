package com.yan.blockchain.pay.task;

import com.yan.blockchain.pay.event.FiatcurrencyNotifyEvent;
import com.yan.blockchain.pay.listener.FiatcurrencyListener;
import com.yan.blockchain.pay.service.FiatCurrencyStatusService;
import com.yan.xpay.domain.vo.FiatcurrencyOrderVo;
import com.yan.xpay.enums.FiatcurrencyOrderStatus;
import com.yan.xpay.service.IFiatcurrencyOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 法币定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FiatCurrencyTask {
	private final IFiatcurrencyOrderService fiatcurrencyOrderService;
	private final FiatCurrencyStatusService fiatCurrencyStatusService;
	private final FiatcurrencyListener fiatcurrencyListener;
	/**
	 * 定时检查法币订单状态
	 */
//	@Scheduled(fixedDelay = 1000, initialDelay = 1000)
	@Transactional
	public void run(){
		List<FiatcurrencyOrderStatus> statusList = List.of(FiatcurrencyOrderStatus.INIT, FiatcurrencyOrderStatus.WAIT, FiatcurrencyOrderStatus.PADDING);
		List<FiatcurrencyOrderVo> voList = fiatcurrencyOrderService.queryUnfilledOrder(statusList);
		voList.forEach((order)->{
			boolean b = fiatCurrencyStatusService.updateFiatCurrencyStatus(order);
			if(b){
				FiatcurrencyNotifyEvent event = new FiatcurrencyNotifyEvent();
				event.setOrder(order);
				fiatcurrencyListener.handleFiatcurrencyNotification(event);
			}
		});
	}
}
