package com.yan.xpay.domain.vo;

import com.yan.xpay.domain.AssetType;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.BlockchainNetwork;
import com.yan.xpay.enums.Chain;
import org.dromara.common.core.enums.Status;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Supported Symbol
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AssetType.class)
public class AssetTypeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * Symbol，EX. USDT、BTC、ETH
     */
    @ExcelProperty(value = "币种符号，如 USDT、BTC、ETH")
    private String symbol;

    /**
     * Chain，EX. TRON、ETH、BTC、BSC
     */
    @ExcelProperty(value = "链类型，如 TRON、ETH、BTC、BSC")
    private Chain chain;

    /**
     * Token contract address (with the main currency being empty)
     */
    @ExcelProperty(value = "代币合约地址", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "主=币为空")
    private String contractAddress;

    /**
     * Decimals
     */
    @ExcelProperty(value = "精度")
    private Integer decimals;

    private String hotAddress;
    private String coldAddress;
    private BigDecimal collectAmount;

    /**
     * Confirmed Number
     */
    private Integer confirmedNum;

    private BlockchainNetwork network;

    /**
     * Whether to enable Ex. ENABLED DISABLED
     */
    @ExcelProperty(value = "是否启用")
    private Status enabled;


}
