package com.yan.blockchain.pay.req;

import lombok.Data;

@Data
public class NotifyInReq {
	private Integer mer_no;//	int	是	分配给用户的编号	600000
	private String order_no;//	String	是	订单号 在商户系统中必须唯一 (不能包含下划线)	20111021433351225
	private String order_amount;//	String	是	提交金额	10000
	private String success_amount;//	String	是	实际交易金额	10000 处理业务以这个金额为准
	private String status;//	String	是	交易结果	SUCCESS:交易成功 WAIT:待处理,PADDING：待支付，FAIL：失败
	private String sign;//	String	是	数据签名
}
