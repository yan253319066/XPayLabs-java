package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import com.yan.xpay.domain.MerchantAssetDetails;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.AssetOperType;
import com.yan.xpay.enums.InOut;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 资产变动明细视图对象 t_merchant_asset_details
 *
 * @author Yan
 * @date 2025-09-15
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = MerchantAssetDetails.class)
public class MerchantAssetDetailsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 交易流水号
     */
    @ExcelProperty(value = "交易流水号")
    private String transactionNo;

    /**
     * 商家ID
     */
    @ExcelProperty(value = "商家ID")
    private Long merchantId;

    /**
     * 币种符号
     */
    @ExcelProperty(value = "币种符号")
    private String symbol;

    /**
     * 变动金额(正负代表方向)
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    @ExcelProperty(value = "变动金额(正负代表方向)")
    private BigDecimal amount;

    /**
     * 变动前可用余额
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    @ExcelProperty(value = "变动前可用余额")
    private BigDecimal oldBalance;

    /**
     * 变动后可用余额
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    @ExcelProperty(value = "变动后可用余额")
    private BigDecimal newBalance;

    /**
     * 变动前冻结余额
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    @ExcelProperty(value = "变动前冻结余额")
    private BigDecimal oldFrozen;

    /**
     * 变动后冻结余额
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    @ExcelProperty(value = "变动后冻结余额")
    private BigDecimal newFrozen;

    /**
     * 类型:deposit/withdraw/payin/payout
     */
    @ExcelProperty(value = "类型:deposit/withdraw/payin/payout")
    private AssetOperType type;

    private InOut inOut;

    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal fee;
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal feeRate;
    private String feeSymbol;
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal rate;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

    private Date createTime;

    private Date updateTime;

}
