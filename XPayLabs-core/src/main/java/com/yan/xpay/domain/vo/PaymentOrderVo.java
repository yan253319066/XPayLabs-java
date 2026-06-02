package com.yan.xpay.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import com.yan.xpay.domain.PaymentOrder;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.NotifyStatus;
import com.yan.xpay.enums.OrderStatus;
import com.yan.xpay.enums.OrderType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用支付订单视图对象 t_payment_order
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = PaymentOrder.class)
public class PaymentOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long merchantId;

    private String uid;

    /**
     * 商户自定义订单号
     */
    @ExcelProperty(value = "商户自定义订单号")
    private String merchantOrderId;

    private OrderType orderType;

    /**
     * 支付币种ID
     */
    @ExcelProperty(value = "支付币种ID")
    private Long assetTypeId;
    private Chain chain;
    private String symbol;

    /**
     * 用户付款地址
     */
    @ExcelProperty(value = "用户付款地址")
    private String payAddress;

    /**
     * 系统分配的收款地址
     */
    @ExcelProperty(value = "系统分配的收款地址")
    private String receiveAddress;

    /**
     * 支付金额
     */
    @ExcelProperty(value = "支付金额")
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal amount;

    /**
     * 实际支付金额
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal actualAmount;

    /**
     * 过期时间
     */
    private Long expiredTime;

    /**
     * 状态：PENDING,
     * 	PENDING_CONFIRMATION,
     * 	SUCCESS,
     * 	EXPIRED,
     * 	FAILED,
     */
    @ExcelProperty(value = "状态")
    private OrderStatus status;
    private String reason;

    /**
     * 支付交易ID
     */
    @ExcelProperty(value = "支付交易ID")
    private String txId;

    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal txGas;
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal handingFee;
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal handingRate;

    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal extraGiven;
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal alreadyGiven;

    /**
     * 回调通知状态
     */
    @ExcelProperty(value = "回调通知状态")
    private NotifyStatus notifyStatus;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String callbackUrl;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Date notifyTime;

    private Date createTime;

}
