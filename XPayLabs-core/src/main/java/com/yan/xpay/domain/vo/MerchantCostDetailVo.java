package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import com.yan.xpay.domain.MerchantCostDetail;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.CostType;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 商家费用明细视图对象 t_merchant_cost_detail
 *
 * @author Yan
 * @date 2025-08-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = MerchantCostDetail.class)
public class MerchantCostDetailVo implements Serializable {

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
     * 费用类型
     */
    @ExcelProperty(value = "费用类型")
    private CostType costType;

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

    /**
     * 数量
     */
    @ExcelProperty(value = "数量")
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal amount;

    /**
     * 业务ID
     */
    @ExcelProperty(value = "业务ID")
    private String businessId;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;


}
