package com.yan.xpay.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yan.xpay.domain.MerchantRechargeWithdraw;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.RechargeWithdraw;
import com.yan.xpay.enums.RechargeWithdrawStatus;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 商家充值提现业务对象 t_merchant_recharge_withdraw
 *
 * @author Yan
 * @date 2025-08-29
 */
@Data
@AutoMapper(target = MerchantRechargeWithdraw.class, reverseConvertGenerate = false)
public class MerchantRechargeWithdrawBo {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    private String transactionNo;

    /**
     * 商家ID
     */
    @NotNull(message = "商家ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long merchantId;

    /**
     * 记录类型：充值，提现
     */
    @NotBlank(message = "记录类型：充值，提现不能为空", groups = { AddGroup.class, EditGroup.class })
    private RechargeWithdraw type;

    /**
     * 链
     */
    @NotBlank(message = "链不能为空", groups = { AddGroup.class, EditGroup.class })
    private Chain chain;

    /**
     * 币种
     */
    @NotBlank(message = "币种不能为空", groups = { AddGroup.class, EditGroup.class })
    private String symbol;

    private String contractAddress;

    /**
     * 支付地址
     */
    @NotBlank(message = "支付地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payAddress;

    /**
     * 接收地址
     */
    @NotBlank(message = "接收地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String receiveAddress;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amount;

    /**
     * 状态：PENDING,SUCCESS,FAILED;
     */
    @NotBlank(message = "状态：PENDING,SUCCESS,FAILED;不能为空", groups = { AddGroup.class, EditGroup.class })
    private RechargeWithdrawStatus status;

    /**
     * 失败原因
     */
    @NotBlank(message = "失败原因不能为空", groups = { AddGroup.class, EditGroup.class })
    private String reason;

    /**
     * txId
     */
    @NotBlank(message = "txId不能为空", groups = { AddGroup.class, EditGroup.class })
    private String txId;

    /**
     * GAS费
     */
    @NotNull(message = "GAS费不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal txGas;

    /**
     * 平台手续费
     */
    @NotNull(message = "平台手续费不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal fee;

    /**
     *平台手续费费率（百分比）
     */
    private BigDecimal rate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 请求参数
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
