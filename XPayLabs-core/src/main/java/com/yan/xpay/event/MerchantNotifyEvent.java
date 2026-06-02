package com.yan.xpay.event;

import com.yan.xpay.domain.vo.NotifyMerchant;
import lombok.Data;

/**
 * 商家回调事件
 */
@Data
public class MerchantNotifyEvent {
	NotifyMerchant notifyMerchant;
}
