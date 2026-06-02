package com.yan.xpay.enums;

public enum FiatcurrencyOrderStatus {
	INIT,//初始化
	WAIT, //待处理
	PADDING,//待支付
	SUCCESS, //交易成功
	FAIL//失败
}
