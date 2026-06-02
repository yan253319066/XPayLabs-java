package com.yan.xpay.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yan.xpay.domain.Merchant;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.yan.xpay.enums.*;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 商户信息视图对象 t_merchant
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Merchant.class)
public class MerchantVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    @JsonIgnore
    @JsonProperty
    private Long sysUserId;

    /**
     * 商户名称
     */
    @ExcelProperty(value = "商户名称")
    private String name;

    private Integer vip;
    private BigDecimal feeRatio;

    /**
     * 商户鉴权Token
     */
    @ExcelProperty(value = "商户鉴权Token")
    @JsonIgnore
    @JsonProperty
    private String token;

    /**
     * webhook secret
     */
    @JsonIgnore
    @JsonProperty
    private String webhookSecret;

    private String whiteListIp;
    private EnableWhitelistIp enableWhitelistIp;
    private GeneratedAddressType generatedAddressType;

    private WithdrawalType withdrawalType;

    /**
     * 支付成功回调地址
     */
    @ExcelProperty(value = "支付成功回调地址")
    private String callbackUrl;

    private IntoType intoType;
    private MerchantAccountType accountType;
    private MerchantSysVersion merchantSysVersion;
    @JsonIgnore
    @JsonProperty
    private String energyApikey;
    @JsonIgnore
    @JsonProperty
    private String googleSecretkey;
    private GoogleStatus googleStatus;

    private Date createTime;
}
