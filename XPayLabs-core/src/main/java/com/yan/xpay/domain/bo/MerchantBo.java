package com.yan.xpay.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yan.xpay.domain.Merchant;
import com.yan.xpay.enums.*;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 商户信息业务对象 t_merchant
 *
 * @author Yan
 * @date 2025-07-12
 */
@Data
@AutoMapper(target = Merchant.class, reverseConvertGenerate = false)
public class MerchantBo {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    private Long sysUserId;

    /**
     * 商户名称
     */
    @NotBlank(message = "商户名称不能为空", groups = { AddGroup.class, EditGroup.class })
    @Length(max = 30, message = "商户名称长度不能超过30" , groups = { AddGroup.class, EditGroup.class })
    @Pattern(regexp = "^[\\x00-\\x7F]+$", message = "商户名称不能包含中文" , groups = { AddGroup.class, EditGroup.class })
    private String name;

    private Integer vip;
    private BigDecimal feeRatio;

    /**
     * 商户鉴权Token
     */
//    @NotBlank(message = "商户鉴权Token不能为空", groups = { AddGroup.class, EditGroup.class })
    private String token;

    /**
     * webhook secret
     */
    private String webhookSecret;

    private String whiteListIp;
    private EnableWhitelistIp enableWhitelistIp;
    private GeneratedAddressType generatedAddressType;

    private WithdrawalType withdrawalType;

    /**
     * 支付成功回调地址
     */
    private String callbackUrl;

    private IntoType intoType;
    private MerchantAccountType accountType;

    @NotNull(message = "商户版本不能为空", groups = { AddGroup.class, EditGroup.class })
    private MerchantSysVersion merchantSysVersion;

    private String energyApikey;

    private String googleSecretkey;
    private GoogleStatus googleStatus;

    private Date createTime;

    /**
     * 请求参数
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @TableField(exist = false)
    private Map<String, Object> params = new HashMap<>();
}
