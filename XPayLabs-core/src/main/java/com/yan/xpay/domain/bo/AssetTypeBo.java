package com.yan.xpay.domain.bo;

import com.yan.xpay.domain.AssetType;
import com.yan.xpay.enums.BlockchainNetwork;
import com.yan.xpay.enums.Chain;
import org.dromara.common.core.enums.Status;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 支持的币种资产类型业务对象 t_asset_type
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@AutoMapper(target = AssetType.class, reverseConvertGenerate = false)
public class AssetTypeBo {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 币种符号，如 USDT、BTC、ETH
     */
    @NotBlank(message = "币种符号，如 USDT、BTC、ETH不能为空", groups = { AddGroup.class, EditGroup.class })
    private String symbol;

    /**
     * 链类型，如 TRON、ETH、BTC、BSC
     */
    @NotNull(message = "链类型，如 TRON、ETH、BTC、BSC不能为空", groups = { AddGroup.class, EditGroup.class })
    private Chain chain;

    /**
     * 代币合约地址（主币为空）
     */
    private String contractAddress;

    /**
     * 精度
     */
    private Integer decimals;

    private String hotAddress;
    private String coldAddress;
    private BigDecimal collectAmount;

    private Integer confirmedNum;

    private BlockchainNetwork network;

    /**
     * 是否启用
     */
    private Status enabled;


}
