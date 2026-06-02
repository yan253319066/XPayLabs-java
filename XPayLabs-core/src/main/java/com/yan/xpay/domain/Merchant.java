package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yan.xpay.enums.*;
import lombok.Data;
import org.dromara.common.encrypt.annotation.EncryptField;
import org.dromara.common.encrypt.enumd.AlgorithmType;
import org.dromara.common.encrypt.utils.EncryptUtils;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("t_merchant")
public class Merchant {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    private Long sysUserId;

    /**
     * 商户名称
     */
    private String name;

    private Integer vip;
    private BigDecimal feeRatio;

    /**
     * 商户鉴权Token
     */
    private String token;

    /**
     * webhook secret
     */
    @EncryptField(algorithm = AlgorithmType.AES, password = "xpay112233weDDkf")
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
    private MerchantSysVersion merchantSysVersion;
    private String energyApikey;
    private String googleSecretkey;
    private GoogleStatus googleStatus;

    private Date createTime;

    public static void main(String[] args) {
        String password = "xpay112233weDDkf";
        String data = "";
        String a = EncryptUtils.encryptByAes(data, password);
        System.out.println(a);
        String b = EncryptUtils.decryptByAes(a, password);
        System.out.println(b);
    }

}
