package com.yan.blockchain.pay.constant;

/**
 * jdpay常量
 */
public interface JdpayConstant {
	static final String jdpayUrl = "https://api.jdpay.club/open-api/";
	static final String fiatCurrencyIn = jdpayUrl + "order/in";
	static final String fiatCurrencyOut = jdpayUrl + "order/out";
	static final String fiatCurrencyQueryIn = jdpayUrl + "order/query/in";
	static final String fiatCurrencyQueryOut = jdpayUrl + "order/query/out";
	static final String merchantBalance = jdpayUrl + "merchant/balance";
	static final String name = "test";//	String	是	姓名	客户英文名,无特殊说明可填固定值
	static final String account = "";//String	是 银行帐号等
	static final String email = "test@email.com";//	String	是	客户邮箱	test@email.com,无特殊说明可填固定值
	static final String phone = "1234567891";//	String	是	手机号	手机号,无特殊说明可填固定值
	static final String notifyUrl = "";//	String	是	交易结果接收地址
	static final String callbackUrl = "";//	String	否	同步跳转地址
}
