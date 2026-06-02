package com.yan.xpay.domain.vo;

import com.yan.xpay.domain.ErrorBlock;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.Chain;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;



/**
 * 错误的区块视图对象 t_error_block
 *
 * @author Yan
 * @date 2025-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ErrorBlock.class)
public class ErrorBlockVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    private Chain chain;

    /**
     * 错误高度
     */
    @ExcelProperty(value = "错误高度")
    private BigInteger blockNumber;

    private Date createTime;

}
