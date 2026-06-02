package com.yan.blockchain.pay.event;

import com.yan.xpay.domain.vo.FiatcurrencyOrderVo;
import lombok.Data;

@Data
public class FiatcurrencyNotifyEvent {
	FiatcurrencyOrderVo order;
}
