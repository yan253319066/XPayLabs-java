package com.yan.blockchain.pay.req;

import lombok.Data;

@Data
public class NotifyOutReq {
	private String order_no;//	String	是	付款订单号 在商户系统中唯一 不能包含下划线	2013355
	private String order_amount;//	String	是	申请的金额	10000
	private String success_amount;//	String	是	实际到账的金额 处理业务以这个值为准	10000
	private String status;//	String	是	交易结果	SUCCESS:交易成功 WAIT:待处理,PADDING：待支付，FAIL：失败
	private String msg;//	String	否	交易结果失败原因	交易失败时返回
	private String sys_no;//	String	是	渠道订单号
	private String utr;//	String	否	utr	部分印度代付成功时，返回对应utr
	private String sign;//	String	是	数据签名
}
