package com.yan.xpay.domain;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_merchant_assets")
public class MerchantAssets {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 币种符号(USDT,BTC等)
     */
    private String symbol;

    /**
     * 可用余额
     */
    private BigDecimal balance;

    /**
     * 冻结余额
     */
    private BigDecimal frozenBalance;

    /**
     * 总余额(冗余)
     */
    @TableField(value = "total_balance", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private BigDecimal totalBalance;

    /**
     * 乐观锁版本号
     */
    @Version
    private Long version;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}
