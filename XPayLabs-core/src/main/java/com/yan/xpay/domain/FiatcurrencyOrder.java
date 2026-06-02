package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yan.xpay.enums.FiatcurrencyOrderStatus;
import com.yan.xpay.enums.OrderType;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_fiatcurrency_order")
public class FiatcurrencyOrder {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 商户订单号
     */
    private String orderNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 订单类型
     */
    private OrderType orderType;

    /**
     * 金额
     */
    private BigDecimal amount;

    private BigDecimal actualAmount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 付款人姓名
     */
    private String payerName;

    /**
     * 付款人账号
     */
    private String payerAccount;

    /**
     * 付款人手机号
     */
    private String payerPhone;

    /**
     * 付款人邮箱
     */
    private String payerEmail;

    /**
     * 付款代码
     */
    private String payerCode;

    /**
     * 扩展字段，JSON格式
     */
    private String extra;

    /**
     * 收款人姓名
     */
    private String payeeName;

    /**
     * 收款人账号
     */
    private String payeeAccount;

    /**
     * 收款人手机号
     */
    private String payeePhone;

    /**
     * 收款人邮箱
     */
    private String payeeEmail;

    /**
     * 收款代码
     */
    private String payeeCode;

    /**
     * 订单状态: INIT,WAIT, PADDING, SUCCESS, FAIL
     */
    private FiatcurrencyOrderStatus status;

    /**
     * 支付通道代码
     */
    private String channelCode;

    /**
     * 商户通知地址
     */
    private String notifyUrl;

    private BigDecimal handingFee;
    private BigDecimal handingRate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 第三方响应内容
     */
    private String thirdPartyResponse;

    /**
     * 第三方回调内容
     */
    private String callbackContent;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
