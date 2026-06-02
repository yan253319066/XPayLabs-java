package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@TableName("t_block_height_tracker")
public class BlockHeightTracker {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 链类型，如 BTC、TRON、ETH
     */
    private String chain;

    /**
     * 最后处理的区块高度
     */
    private Long lastHeight;


}
