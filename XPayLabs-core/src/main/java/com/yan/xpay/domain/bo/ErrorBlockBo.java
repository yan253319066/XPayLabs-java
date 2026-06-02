package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.ErrorBlock;
import com.yan.xpay.enums.Chain;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigInteger;
import java.util.Date;

/**
 * 错误的区块业务对象 t_error_block
 *
 * @author Yan
 * @date 2025-07-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ErrorBlock.class, reverseConvertGenerate = false)
public class ErrorBlockBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 错误高度
     */
    @NotNull(message = "错误高度不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigInteger blockNumber;

    private Chain chain;

    private Date createTime;
}
