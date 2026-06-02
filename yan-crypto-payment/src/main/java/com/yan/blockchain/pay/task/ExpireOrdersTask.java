package com.yan.blockchain.pay.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.*;
import com.yan.xpay.domain.vo.NotifyMerchant;
import com.yan.xpay.enums.*;
import com.yan.xpay.event.MerchantNotifyEvent;
import com.yan.xpay.mapper.MerchantMapper;
import com.yan.xpay.mapper.PaymentOrderMapper;
import com.yan.xpay.service.IMerchantAssetsService;
import com.yan.xpay.service.IPaymentOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpireOrdersTask {
	private final IPaymentOrderService paymentOrderService;
	private final ApplicationEventPublisher eventPublisher;
	private final PaymentOrderMapper paymentOrderMapper;
	private final MerchantMapper merchantMapper;
	private final IMerchantAssetsService merchantAssetsService;

	@Scheduled(cron = "0 0/1 * * * ?") // 每分钟检查一次
	@Transactional
	public void expireOrders() {
		List<PaymentOrder> pendingOrders = paymentOrderMapper.selectList(
			new LambdaQueryWrapper<PaymentOrder>()
				.eq(PaymentOrder::getStatus, OrderStatus.INIT)
				.lt(PaymentOrder::getExpiredTime, DateUtil.currentSeconds())
		);

		for (PaymentOrder order : pendingOrders) {
			if (order.getOrderType() == OrderType.PAYOUT) {
				Merchant merchant = merchantMapper.selectById(order.getMerchantId());
				if(merchant.getMerchantSysVersion() == MerchantSysVersion.V3){
					SimpleTransfer simpleTransfer = new SimpleTransfer();
					simpleTransfer.setSymbol(order.getSymbol());
					simpleTransfer.setAmount(order.getAmount());
					simpleTransfer.setMerchantId(order.getMerchantId());
					simpleTransfer.setRate(BigDecimal.ZERO);
					simpleTransfer.setType(AssetOperType.PAYOUT_REFUND);
					simpleTransfer.setRemark("商家代付过期，资金解冻");
					simpleTransfer.setFeeRate(order.getHandingRate());
					simpleTransfer.setFee(order.getHandingFee());
					simpleTransfer.setFeeSymbol(order.getSymbol());
					simpleTransfer.setTransactionNo(order.getMerchantOrderId());
					simpleTransfer.setChain(order.getChain());
					simpleTransfer.setNetwork(merchant.getAccountType().name());
					merchantAssetsService.transfer(simpleTransfer);
					log.info("商家代付订单:{}已过期，资金已解冻", order.getMerchantOrderId());
				}
			}else {
				log.info("商家代收订单:{}已过期", order.getMerchantOrderId());
			}
			boolean b = paymentOrderService.setOrderExpired(order);
			if(b) {
				//通知商家
				NotifyOrder notifyOrder = new NotifyOrder();
				notifyOrder.setOrderId(order.getMerchantOrderId());
				notifyOrder.setOrderType(order.getOrderType());
				notifyOrder.setStatus(order.getStatus());

				NotifyPayload notifyPayload = new NotifyPayload(NotifyType.ORDER_EXPIRED, notifyOrder);

				NotifyMerchant notifyMerchant = new NotifyMerchant();
				notifyMerchant.setMerchantId(order.getMerchantId());
				notifyMerchant.setCallbackUrl(order.getCallbackUrl());
				notifyMerchant.setNotifyPayload(notifyPayload);

				MerchantNotifyEvent merchantNotifyEvent = new MerchantNotifyEvent();
				merchantNotifyEvent.setNotifyMerchant(notifyMerchant);
				eventPublisher.publishEvent(merchantNotifyEvent);
			}
		}
	}

}
