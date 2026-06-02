package com.yan.xpay.domain.vo;

import com.yan.xpay.domain.UserAddress;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.UserAddressCollectible;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 用户地址视图对象 t_user_address
 *
 * @author Yan
 * @date 2025-07-28
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = UserAddress.class)
public class UserAddressVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 商家ID
     */
    @ExcelProperty(value = "商家ID")
    private Long merchantId;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private String userId;

    /**
     * 链
     */
    @ExcelProperty(value = "链")
    private Chain chain;

    private String symbol;

    /**
     * 地址
     */
    @ExcelProperty(value = "地址")
    private String address;

    private BigDecimal amount;

    /**
     * 是否可归集
     */
    @ExcelProperty(value = "是否可归集")
    private UserAddressCollectible collectible;

    private AddressStatus status;


}
