package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.RechargeWithdraw;
import com.yan.xpay.enums.RechargeWithdrawStatus;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_merchant_recharge_withdraw")
public class MerchantRechargeWithdraw {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    private String transactionNo;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 记录类型：充值，提现
     */
    private RechargeWithdraw type;

    /**
     * 链
     */
    private Chain chain;

    /**
     * 币种
     */
    private String symbol;

    private String contractAddress;

    /**
     * 支付地址
     */
    private String payAddress;

    /**
     * 接收地址
     */
    private String receiveAddress;

    /**
     * 数量
     */
    private BigDecimal amount;

    /**
     * 状态：PENDING,SUCCESS,FAILED;
     */
    private RechargeWithdrawStatus status;

    /**
     * 失败原因
     */
    private String reason;

    /**
     * txId
     */
    private String txId;

    /**
     * GAS费
     */
    private BigDecimal txGas;

    /**
     * 平台手续费
     */
    private BigDecimal fee;

    /**
     *平台手续费费率（百分比）
     */
    private BigDecimal rate;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
