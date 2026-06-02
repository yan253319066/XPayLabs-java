package com.yan.xpay.domain.vo;

import com.yan.xpay.domain.AddressPool;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.AddressType;
import com.yan.xpay.enums.Chain;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 地址池管理视图对象 t_address_pool
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = AddressPool.class)
public class AddressPoolVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 链
     */
    @ExcelProperty(value = "链")
    private Chain chain;

    private AddressType type;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String address;

    /**
     * 派生路径
     */
    @ExcelProperty(value = "派生路径")
    private String path;

    /**
     * 是否已使用（0=未使用，1=已分配）
     */
    @ExcelProperty(value = "是否已使用", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0==未使用，1=已分配")
    private AddressStatus used;


}
