package com.yan.xpay.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yan.xpay.domain.PaymentOrder;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.NotifyStatus;
import com.yan.xpay.enums.OrderStatus;
import com.yan.xpay.enums.OrderType;
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
 * 通用支付订单业务对象 t_payment_order
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@AutoMapper(target = PaymentOrder.class, reverseConvertGenerate = false)
public class PaymentOrderBo {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long merchantId;

    private String uid;

    /**
     * 商户自定义订单号
     */
    @NotBlank(message = "商户自定义订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String merchantOrderId;

    private OrderType orderType;

    /**
     * 支付币种ID
     */
    @NotNull(message = "支付币种ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long assetTypeId;

    private Chain chain;
    private String symbol;

    /**
     * 用户付款地址
     */
    @NotBlank(message = "用户付款地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payAddress;

    /**
     * 系统分配的收款地址
     */
    @NotBlank(message = "系统分配的收款地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String receiveAddress;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空", groups = { AddGroup.class, EditGroup.class })
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
     * 状态：PENDING,
     * 	PENDING_CONFIRMATION,
     * 	SUCCESS,
     * 	EXPIRED,
     * 	FAILED,
     */
    private OrderStatus status;
    private String reason;

    /**
     * 支付交易ID
     */
    private String txId;
    private BigDecimal txGas;
    private BigDecimal handingFee;
    private BigDecimal handingRate;

    private BigDecimal extraGiven;
    private BigDecimal alreadyGiven;

    /**
     * 回调通知状态
     */
    private NotifyStatus notifyStatus;

    /**
     * 
     */
    private String callbackUrl;

    /**
     * 
     */
    private Date notifyTime;

    /**
     * 请求参数
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
