package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yan.xpay.config.BigDecimalStringSerializer;
import com.yan.xpay.domain.CollectRecord;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.BlockchainStatus;
import com.yan.xpay.enums.Chain;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 链上归集记录视图对象 t_collect_record
 *
 * @author Yan
 * @date 2025-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CollectRecord.class)
public class CollectRecordVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long merchantId;

    /**
     * 
     */
    @ExcelProperty(value = "")
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

    /**
     * 
     */
    @ExcelProperty(value = "")
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

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String contractAddress;

    /**
     * 
     */
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    @ExcelProperty(value = "")
    private BigDecimal txFee;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Integer confirmedNum;

    /**
     * 交易状态：PENDING, SUCCESS, FAILED
     */
    @ExcelProperty(value = "交易状态：PENDING, SUCCESS, FAILED")
    private BlockchainStatus status;

    /**
     * 交易区块时间
     */
    @ExcelProperty(value = "交易区块时间")
    private Long blockTime;

    /**
     * 预计收集数量
     */
    @ExcelProperty(value = "预计收集数量")
    private BigDecimal collectAmount;

    /**
     * 平台手续费
     */
    @ExcelProperty(value = "平台手续费")
    @JsonSerialize(using = BigDecimalStringSerializer.class)
    private BigDecimal fee;

    /**
     * 平台手续费率
     */
    @ExcelProperty(value = "平台手续费率")
    private BigDecimal feeRatio;


}
