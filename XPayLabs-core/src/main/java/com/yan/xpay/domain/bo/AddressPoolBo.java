package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.AddressPool;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.AddressType;
import com.yan.xpay.enums.Chain;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 地址池管理业务对象 t_address_pool
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@AutoMapper(target = AddressPool.class, reverseConvertGenerate = false)
public class AddressPoolBo {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 链
     */
    @NotNull(message = "链不能为空", groups = { AddGroup.class, EditGroup.class })
    private Chain chain;

    private AddressType type;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String address;

    /**
     * 派生路径
     */
    private String path;

    /**
     * 是否已使用（0=未使用，1=已分配）
     */
    private AddressStatus used;


}
