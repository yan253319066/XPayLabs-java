package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.MerchantAddress;
import com.yan.xpay.enums.Chain;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 商家钱包地址业务对象 t_merchant_address
 *
 * @author Yan
 * @date 2025-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = MerchantAddress.class, reverseConvertGenerate = false)
public class MerchantAddressBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 链
     */
    private Chain chain;

    /**
     * 
     */
    private String symbol;

    /**
     * 冷钱包地址
     */
    private String coldAddress;

    /**
     * 归集触发额度
     */
    private BigDecimal collectAmount;

    /**
     * 热钱包地址
     */
    private String hotAddress;


}
