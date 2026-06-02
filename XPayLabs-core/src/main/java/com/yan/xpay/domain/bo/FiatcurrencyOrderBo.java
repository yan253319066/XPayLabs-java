package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.FiatcurrencyOrder;
import com.yan.xpay.enums.FiatcurrencyOrderStatus;
import com.yan.xpay.enums.OrderType;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 法币订单业务对象 t_fiatcurrency_order
 *
 * @author Yan
 * @date 2025-10-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = FiatcurrencyOrder.class, reverseConvertGenerate = false)
public class FiatcurrencyOrderBo extends BaseEntity {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商户订单号
     */
    @NotBlank(message = "商户订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    /**
     * 订单类型
     */
    @NotNull(message = "订单类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private OrderType orderType;

    /**
     * 商户ID
     */
    @NotNull(message = "商户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long merchantId;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amount;

    private BigDecimal actualAmount;

    /**
     * 币种
     */
    @NotBlank(message = "币种不能为空", groups = { AddGroup.class, EditGroup.class })
    private String currency;

    /**
     * 付款人姓名
     */
    @NotBlank(message = "付款人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payerName;

    /**
     * 付款人账号
     */
    @NotBlank(message = "付款人账号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payerAccount;

    /**
     * 付款人手机号
     */
    @NotBlank(message = "付款人手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payerPhone;

    /**
     * 付款人邮箱
     */
    @NotBlank(message = "付款人邮箱不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payerEmail;

    /**
     * 付款代码
     */
    @NotBlank(message = "付款代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payerCode;

    /**
     * 扩展字段，JSON格式
     */
    @NotBlank(message = "扩展字段，JSON格式不能为空", groups = { AddGroup.class, EditGroup.class })
    private String extra;

    /**
     * 收款人姓名
     */
    @NotBlank(message = "收款人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payeeName;

    /**
     * 收款人账号
     */
    @NotBlank(message = "收款人账号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payeeAccount;

    /**
     * 收款人手机号
     */
    @NotBlank(message = "收款人手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payeePhone;

    /**
     * 收款人邮箱
     */
    @NotBlank(message = "收款人邮箱不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payeeEmail;

    /**
     * 收款代码
     */
    @NotBlank(message = "收款代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payeeCode;

    /**
     * 订单状态: INIT,WAIT, PADDING, SUCCESS, FAIL
     */
    @NotBlank(message = "订单状态: INIT,WAIT, PADDING, SUCCESS, FAIL不能为空", groups = { AddGroup.class, EditGroup.class })
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


}
