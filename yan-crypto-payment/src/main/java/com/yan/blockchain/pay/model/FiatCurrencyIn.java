package com.yan.blockchain.pay.model;

import lombok.Data;

@Data
public class FiatCurrencyIn {
	private String mer_no;//	String	是	分配给用户的编号	600000
	private String order_no;//	String	是	商户订单号	12345678
	private String amount;//	String	是	交易金额	支持两位小数 单位：元，
	private String name;//	String	是	姓名	客户英文名,无特殊说明可填固定值
	private String email;//	String	是	客户邮箱	test@email.com,无特殊说明可填固定值
	private String phone;//	String	是	手机号	手机号,无特殊说明可填固定值
	private String currency;//	String	是	交易币种	详细参考文档：[交易币种]
	private String pay_code;//	String	是	支付类型编码	详细参考文档：[支付类型编码]
	private String notify_url;//	String	是	交易结果接收地址
	private String sign;//	String	是	数据签名
	private String callback_url;//	String	否	同步跳转地址
}
