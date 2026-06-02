package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_collect_record")
public class CollectRecord {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    private Long merchantId;

    private Long blockNumber;

    /**
     * 
     */
    private String fromAddress;

    /**
     * 
     */
    private String toAddress;

    /**
     * 
     */
    private Chain chain;

    private String symbol;

    /**
     * 
     */
    private BigDecimal amount;

    /**
     * 
     */
    private String txId;

    private String contractAddress;

    private BigDecimal txFee;

    private Integer confirmedNum;

    /**
     * 交易状态：PENDING, SUCCESS, FAILED
     */
    private BlockchainStatus status;

    /**
     * 交易区块时间
     */
    private Long blockTime;

    private BigDecimal collectAmount;
    private BigDecimal fee;
    private BigDecimal feeRatio;

    private Date createTime;
}
