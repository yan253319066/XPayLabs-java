package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.TxType;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;

@Data
@TableName("t_tx_record")
public class TxRecord {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 对应订单ID
     */
    private String orderId;

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
    private String feeTxId;

    private String contractAddress;

    /**
     * 交易类型：COLLECTION,PAYOUT
     */
    private TxType txType;

    private BigDecimal txFee;

    private Integer confirmedNum;

    /**
     * 交易状态
     */
    private BlockchainStatus status;

    /**
     * 交易区块时间
     */
    private Long blockTime;

}
