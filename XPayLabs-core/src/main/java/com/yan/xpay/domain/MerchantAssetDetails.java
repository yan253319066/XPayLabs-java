package com.yan.xpay.domain;


import com.baomidou.mybatisplus.annotation.*;
import com.yan.xpay.enums.AssetOperType;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.InOut;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_merchant_asset_details")
public class MerchantAssetDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 交易流水号
     */
    private String transactionNo;

    /**
     * 商家ID
     */
    private Long merchantId;

    private String network;

    private Chain chain;

    /**
     * 币种符号
     */
    private String symbol;

    /**
     * 变动金额(正负代表方向)
     */
    private BigDecimal amount;

    /**
     * 变动前可用余额
     */
    private BigDecimal oldBalance;

    /**
     * 变动后可用余额
     */
    private BigDecimal newBalance;

    /**
     * 变动前冻结余额
     */
    private BigDecimal oldFrozen;

    /**
     * 变动后冻结余额
     */
    private BigDecimal newFrozen;

    /**
     * 类型:deposit/withdraw/payin/payout
     */
    private AssetOperType type;

    private InOut inOut;

    private BigDecimal fee;
    private BigDecimal feeRate;
    private String feeSymbol;
    private BigDecimal rate;

    /**
     * 备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
