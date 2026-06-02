package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.UserAddress;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.UserAddressCollectible;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 用户地址业务对象 t_user_address
 *
 * @author Yan
 * @date 2025-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = UserAddress.class, reverseConvertGenerate = false)
public class UserAddressBo extends BaseEntity {

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
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userId;

    /**
     * 链
     */
    @NotBlank(message = "链不能为空", groups = { AddGroup.class, EditGroup.class })
    private Chain chain;

    private String symbol;

    /**
     * 地址
     */
    @NotBlank(message = "地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String address;

    private BigDecimal amount;

    /**
     * 是否可归集
     */
    @NotBlank(message = "是否可归集不能为空", groups = { AddGroup.class, EditGroup.class })
    private UserAddressCollectible collectible;

    private AddressStatus status;
}
