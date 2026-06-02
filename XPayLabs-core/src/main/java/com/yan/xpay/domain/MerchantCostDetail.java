package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.CostType;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_merchant_cost_detail")
public class MerchantCostDetail {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 费用类型
     */
    private CostType costType;

    /**
     * 链
     */
    private Chain chain;

    /**
     * 币种
     */
    private String symbol;

    /**
     * 数量
     */
    private BigDecimal amount;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}
