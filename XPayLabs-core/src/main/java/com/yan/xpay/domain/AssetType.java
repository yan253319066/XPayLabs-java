package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yan.xpay.enums.BlockchainNetwork;
import com.yan.xpay.enums.Chain;
import lombok.Data;
import org.dromara.common.core.enums.Status;

import java.io.Serial;
import java.math.BigDecimal;

@Data
@TableName("t_asset_type")
public class AssetType {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 币种符号，如 USDT、BTC、ETH
     */
    private String symbol;

    /**
     * 链类型，如 TRON、ETH、BTC、BSC
     */
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
