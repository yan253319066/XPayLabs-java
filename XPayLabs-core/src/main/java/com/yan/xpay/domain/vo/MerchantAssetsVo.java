package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import com.yan.xpay.domain.MerchantAssets;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 商家资产视图对象 t_merchant_assets
 *
 * @author Yan
 * @date 2025-09-15
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = MerchantAssets.class)
public class MerchantAssetsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 商家ID
     */
    @ExcelProperty(value = "商家ID")
    private Long merchantId;

    /**
     * 币种符号(USDT,BTC等)
     */
    @ExcelProperty(value = "币种符号(USDT,BTC等)")
    private String symbol;

    /**
     * 可用余额
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    @ExcelProperty(value = "可用余额")
    private BigDecimal balance;

    /**
     * 冻结余额
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    @ExcelProperty(value = "冻结余额")
    private BigDecimal frozenBalance;

    /**
     * 总余额(冗余)
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    @ExcelProperty(value = "总余额(冗余)")
    private BigDecimal totalBalance;


}
