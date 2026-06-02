package com.yan.xpay.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yan.xpay.domain.MerchantCostDetail;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.CostType;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 商家费用明细业务对象 t_merchant_cost_detail
 *
 * @author Yan
 * @date 2025-08-28
 */
@Data
@AutoMapper(target = MerchantCostDetail.class, reverseConvertGenerate = false)
public class MerchantCostDetailBo {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商家ID
     */
    @NotNull(message = "商家ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long merchantId;

    /**
     * 费用类型
     */
    @NotBlank(message = "费用类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private CostType costType;

    /**
     * 链
     */
    @NotBlank(message = "链不能为空", groups = { AddGroup.class, EditGroup.class })
    private Chain chain;

    /**
     * 币种
     */
    @NotBlank(message = "币种不能为空", groups = { AddGroup.class, EditGroup.class })
    private String symbol;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amount;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 请求参数
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();

}
