package com.yan.blockchain.pay.model;

import lombok.Data;

@Data
public class FiatCurrencyOut {
	private String mer_no;//	String	是	商户编号	600000
	private String order_no;//	String	是	商户订单号	201335511
	private String amount;//	String	是	出款的资金	支持两位小数 ，单位元
	private String currency;//	String	是	币种	参考文档：[交易币种]
	private String bank_code;//	String	是	收款银行编号	参考文档：[银行编码]
	private String name;//	String	是	收款人姓名	tom （一般为字母）
	private String account;//	String	是	收款人账号	银行账号
	private String email;//	String	是	收款人邮箱	无特殊说明可填固定邮箱
	private String phone;//	String	是	收款人手机号	无特殊说明可填固定手机号
	private String notify_url;//	String	是	交易结果接收地址
	private String sign;//	String	是	数据签名
}
