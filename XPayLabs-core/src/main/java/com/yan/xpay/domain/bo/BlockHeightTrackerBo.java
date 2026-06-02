package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.BlockHeightTracker;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 区块监听高度追踪业务对象 t_block_height_tracker
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@AutoMapper(target = BlockHeightTracker.class, reverseConvertGenerate = false)
public class BlockHeightTrackerBo {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 链类型，如 BTC、TRON、ETH
     */
    @NotBlank(message = "链类型，如 BTC、TRON、ETH不能为空", groups = { AddGroup.class, EditGroup.class })
    private String chain;

    /**
     * 最后处理的区块高度
     */
    @NotNull(message = "最后处理的区块高度不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long lastHeight;


}
