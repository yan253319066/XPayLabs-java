package com.yan.xpay.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yan.xpay.domain.MerchantAssetDetails;
import com.yan.xpay.enums.AssetOperType;
import com.yan.xpay.enums.InOut;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 资产变动明细业务对象 t_merchant_asset_details
 *
 * @author Yan
 * @date 2025-09-15
 */
@Data
@AutoMapper(target = MerchantAssetDetails.class, reverseConvertGenerate = false)
public class MerchantAssetDetailsBo {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 交易流水号
     */
    @NotBlank(message = "交易流水号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String transactionNo;

    /**
     * 商家ID
     */
    @NotBlank(message = "商家ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long merchantId;

    /**
     * 币种符号
     */
    @NotBlank(message = "币种符号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String symbol;

    /**
     * 变动金额(正负代表方向)
     */
    @NotNull(message = "变动金额(正负代表方向)不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amount;

    /**
     * 变动前可用余额
     */
    @NotNull(message = "变动前可用余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal oldBalance;

    /**
     * 变动后可用余额
     */
    @NotNull(message = "变动后可用余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal newBalance;

    /**
     * 变动前冻结余额
     */
    @NotNull(message = "变动前冻结余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal oldFrozen;

    /**
     * 变动后冻结余额
     */
    @NotNull(message = "变动后冻结余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal newFrozen;

    /**
     * 类型:deposit/withdraw/payin/payout
     */
    @NotBlank(message = "类型:deposit/withdraw/payin/payout不能为空", groups = { AddGroup.class, EditGroup.class })
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

    /**
     * 请求参数
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();

}
