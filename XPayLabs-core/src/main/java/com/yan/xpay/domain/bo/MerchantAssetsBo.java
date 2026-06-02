package com.yan.xpay.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yan.xpay.domain.MerchantAssets;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 商家资产业务对象 t_merchant_assets
 *
 * @author Yan
 * @date 2025-09-15
 */
@Data
@AutoMapper(target = MerchantAssets.class, reverseConvertGenerate = false)
public class MerchantAssetsBo {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商家ID
     */
    @NotBlank(message = "商家ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long merchantId;

    /**
     * 币种符号(USDT,BTC等)
     */
    @NotBlank(message = "币种符号(USDT,BTC等)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String symbol;

    /**
     * 可用余额
     */
    @NotNull(message = "可用余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal balance;

    /**
     * 冻结余额
     */
    @NotNull(message = "冻结余额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal frozenBalance;

    /**
     * 总余额(冗余)
     */
    private BigDecimal totalBalance;

    /**
     * 请求参数
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
