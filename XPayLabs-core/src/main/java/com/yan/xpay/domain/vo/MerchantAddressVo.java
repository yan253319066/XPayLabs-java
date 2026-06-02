package com.yan.xpay.domain.vo;

import com.yan.xpay.domain.MerchantAddress;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.Chain;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 商家钱包地址视图对象 t_merchant_address
 *
 * @author Yan
 * @date 2025-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = MerchantAddress.class)
public class MerchantAddressVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 商家ID
     */
    @ExcelProperty(value = "商家ID")
    private Long merchantId;

    /**
     * 链
     */
    @ExcelProperty(value = "链")
    private Chain chain;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String symbol;

    /**
     * 冷钱包地址
     */
    @ExcelProperty(value = "冷钱包地址")
    private String coldAddress;

    /**
     * 归集触发额度
     */
    @ExcelProperty(value = "归集触发额度")
    private BigDecimal collectAmount;

    /**
     * 热钱包地址
     */
    @ExcelProperty(value = "热钱包地址")
    private String hotAddress;


}
