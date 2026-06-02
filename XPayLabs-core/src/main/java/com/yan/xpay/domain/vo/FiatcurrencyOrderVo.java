package com.yan.xpay.domain.vo;

import java.math.BigDecimal;
import com.yan.xpay.domain.FiatcurrencyOrder;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.FiatcurrencyOrderStatus;
import com.yan.xpay.enums.OrderType;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 法币订单视图对象 t_fiatcurrency_order
 *
 * @author Yan
 * @date 2025-10-10
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = FiatcurrencyOrder.class)
public class FiatcurrencyOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 商户订单号
     */
    @ExcelProperty(value = "商户订单号")
    private String orderNo;

    /**
     * 商户ID
     */
    @ExcelProperty(value = "商户ID")
    private Long merchantId;

    /**
     * 订单类型
     */
    @ExcelProperty(value = "订单类型")
    private OrderType orderType;

    /**
     * 金额
     */
    @ExcelProperty(value = "金额")
    private BigDecimal amount;

    private BigDecimal actualAmount;

    /**
     * 币种
     */
    @ExcelProperty(value = "币种")
    private String currency;

    /**
     * 付款人姓名
     */
    @ExcelProperty(value = "付款人姓名")
    private String payerName;

    /**
     * 付款人账号
     */
    @ExcelProperty(value = "付款人账号")
    private String payerAccount;

    /**
     * 付款人手机号
     */
    @ExcelProperty(value = "付款人手机号")
    private String payerPhone;

    /**
     * 付款人邮箱
     */
    @ExcelProperty(value = "付款人邮箱")
    private String payerEmail;

    /**
     * 付款代码
     */
    @ExcelProperty(value = "付款代码")
    private String payerCode;

    /**
     * 扩展字段，JSON格式
     */
    @ExcelProperty(value = "扩展字段，JSON格式")
    private String extra;

    /**
     * 收款人姓名
     */
    @ExcelProperty(value = "收款人姓名")
    private String payeeName;

    /**
     * 收款人账号
     */
    @ExcelProperty(value = "收款人账号")
    private String payeeAccount;

    /**
     * 收款人手机号
     */
    @ExcelProperty(value = "收款人手机号")
    private String payeePhone;

    /**
     * 收款人邮箱
     */
    @ExcelProperty(value = "收款人邮箱")
    private String payeeEmail;

    /**
     * 收款代码
     */
    @ExcelProperty(value = "收款代码")
    private String payeeCode;

    /**
     * 订单状态: INIT,WAIT, PADDING, SUCCESS, FAIL
     */
    @ExcelProperty(value = "订单状态: INIT,WAIT, PADDING, SUCCESS, FAIL")
    private FiatcurrencyOrderStatus status;

    /**
     * 支付通道代码
     */
    @ExcelProperty(value = "支付通道代码")
    private String channelCode;

    /**
     * 商户通知地址
     */
    @ExcelProperty(value = "商户通知地址")
    private String notifyUrl;

    private BigDecimal handingFee;
    private BigDecimal handingRate;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    /**
     * 第三方响应内容
     */
    @ExcelProperty(value = "第三方响应内容")
    private String thirdPartyResponse;

    /**
     * 第三方回调内容
     */
    @ExcelProperty(value = "第三方回调内容")
    private String callbackContent;


}
