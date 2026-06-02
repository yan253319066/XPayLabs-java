package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.NotifyStatus;
import com.yan.xpay.enums.OrderStatus;
import com.yan.xpay.enums.OrderType;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_payment_order")
public class PaymentOrder {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 
     */
    private Long merchantId;

    private String uid;

    /**
     * 商户自定义订单号
     */
    private String merchantOrderId;

    private OrderType orderType;

    /**
     * 支付币种ID
     */
    private Long assetTypeId;
    private Chain chain;
    private String symbol;

    /**
     * 用户付款地址
     */
    private String payAddress;

    /**
     * 系统分配的收款地址
     */
    private String receiveAddress;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 实际支付金额
     */
    private BigDecimal actualAmount;

    /**
     * 过期时间
     */
    private Long expiredTime;

    /**
     * 状态：PENDING,PENDING_CONFIRMATION,SUCCESS,EXPIRED,FAILED,PENDING_COLLECT,COLLECTED
     */
    private OrderStatus status;
    /**
     * 失败原因
     */
    private String reason;

    /**
     * 支付交易ID
     */
    private String txId;

    /**
     * gas费
     */
    private BigDecimal txGas;
    /**
     * 平台手续费
     */
    private BigDecimal handingFee;
    /**
     * 平台手续费费率（百分比）
     */
    private BigDecimal handingRate;

    /**
     * 用户付款多给的钱
     */
    private BigDecimal extraGiven;
    /**
     *用户付款已付的钱
     */
    private BigDecimal alreadyGiven;

    /**
     * 回调通知状态
     */
    private NotifyStatus notifyStatus;

    /**
     * 回调URL地址
     */
    private String callbackUrl;

    /**
     * 通知时间
     */
    private Date notifyTime;

    private Date createTime;
    private Date updateTime;

}
