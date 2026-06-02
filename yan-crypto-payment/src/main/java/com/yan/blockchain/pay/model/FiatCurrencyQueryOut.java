package com.yan.blockchain.pay.model;

import lombok.Data;

@Data
public class FiatCurrencyQueryOut {
	private String mer_no;//	String	是	分配给用户的编号	600000
	private String order_no;//	String	是	商户订单号必须唯一	20111021433351225
	private String sign;//	String	是	数据签名
}
