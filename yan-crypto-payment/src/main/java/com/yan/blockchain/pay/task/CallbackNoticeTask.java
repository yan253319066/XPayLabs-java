package com.yan.blockchain.pay.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yan.xpay.domain.CallbackNotice;
import com.yan.xpay.domain.NotifyOrder;
import com.yan.xpay.domain.NotifyPayload;
import com.yan.xpay.domain.PaymentOrder;
import com.yan.xpay.domain.vo.NotifyMerchant;
import com.yan.xpay.enums.NotifyStatus;
import com.yan.xpay.enums.NotifyType;
import com.yan.xpay.event.MerchantNotifyEvent;
import com.yan.xpay.mapper.CallbackNoticeMapper;
import com.yan.xpay.mapper.PaymentOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackNoticeTask {

    private final CallbackNoticeMapper callbackNoticeMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "10 0/1 * * * ?") //
    @Transactional
    public void expireOrders() {
        List<CallbackNotice> list = callbackNoticeMapper.selectList(new LambdaQueryWrapper<CallbackNotice>()
                .eq(CallbackNotice::getNotifyStatus, NotifyStatus.INIT));
        for (CallbackNotice callbackNotice : list) {
            PaymentOrder order = paymentOrderMapper.selectById(callbackNotice.getOrderId());
            if(order==null) return;

            switch (order.getStatus()) {
                case SUCCESS, FAILED, EXPIRED -> {
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

}
