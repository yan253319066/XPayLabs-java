package com.yan.xpay.domain.vo;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import com.yan.xpay.domain.TxRecord;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.TxType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 链上交易记录视图对象 t_tx_record
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = TxRecord.class)
public class TxRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 对应订单ID
     */
    @ExcelProperty(value = "对应订单ID")
    private String orderId;

    private Long blockNumber;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String fromAddress;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String toAddress;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Chain chain;

    private String symbol;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private BigDecimal amount;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String txId;
    private String feeTxId;

    private String contractAddress;

    /**
     * 交易类型：COLLECTION,PAYOUT
     */
    @ExcelProperty(value = "交易类型：COLLECTION,PAYOUT")
    private TxType txType;

    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal txFee;

    private Integer confirmedNum;

    /**
     * 交易状态：
     */
    @ExcelProperty(value = "交易状态")
    private BlockchainStatus status;

    /**
     * 交易区块时间
     */
    @ExcelProperty(value = "交易区块时间")
    private Long blockTime;
}
