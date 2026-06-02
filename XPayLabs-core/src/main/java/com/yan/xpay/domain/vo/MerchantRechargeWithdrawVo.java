package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import com.yan.xpay.domain.MerchantRechargeWithdraw;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.RechargeWithdraw;
import com.yan.xpay.enums.RechargeWithdrawStatus;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 商家充值提现视图对象 t_merchant_recharge_withdraw
 *
 * @author Yan
 * @date 2025-08-29
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = MerchantRechargeWithdraw.class)
public class MerchantRechargeWithdrawVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    private String transactionNo;

    /**
     * 商家ID
     */
    @ExcelProperty(value = "商家ID")
    private Long merchantId;

    /**
     * 记录类型：充值，提现
     */
    @ExcelProperty(value = "记录类型：充值，提现")
    private RechargeWithdraw type;

    /**
     * 链
     */
    @ExcelProperty(value = "链")
    private Chain chain;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种")
    private String symbol;

    private String contractAddress;

    /**
     * 支付地址
     */
    @ExcelProperty(value = "支付地址")
    private String payAddress;

    /**
     * 接收地址
     */
    @ExcelProperty(value = "接收地址")
    private String receiveAddress;

    /**
     * 数量
     */
    @ExcelProperty(value = "数量")
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal amount;

    /**
     * 状态：PENDING,SUCCESS,FAILED;
     */
    @ExcelProperty(value = "状态：PENDING,SUCCESS,FAILED;")
    private RechargeWithdrawStatus status;

    /**
     * 失败原因
     */
    @ExcelProperty(value = "失败原因")
    private String reason;

    /**
     * txId
     */
    @ExcelProperty(value = "txId")
    private String txId;

    /**
     * GAS费
     */
    @ExcelProperty(value = "GAS费")
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal txGas;

    /**
     * 平台手续费
     */
    @ExcelProperty(value = "平台手续费")
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal fee;

    /**
     *平台手续费费率（百分比）
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal rate;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;


}
