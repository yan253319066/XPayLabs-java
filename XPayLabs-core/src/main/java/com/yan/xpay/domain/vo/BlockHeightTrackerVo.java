package com.yan.xpay.domain.vo;

import com.yan.xpay.domain.BlockHeightTracker;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 区块监听高度追踪视图对象 t_block_height_tracker
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = BlockHeightTracker.class)
public class BlockHeightTrackerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 链类型，如 BTC、TRON、ETH
     */
    @ExcelProperty(value = "链类型，如 BTC、TRON、ETH")
    private String chain;

    /**
     * 最后处理的区块高度
     */
    @ExcelProperty(value = "最后处理的区块高度")
    private Long lastHeight;


}
