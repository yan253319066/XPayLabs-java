package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.AddressType;
import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.io.Serial;

@Data
@TableName("t_address_pool")
public class AddressPool {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 链
     */
    private Chain chain;

    private AddressType type;

    /**
     * 
     */
    private String address;

    private String keystore;
    private String encrypt;

    /**
     * 派生路径
     */
    private String path;

    /**
     * 是否已使用（0=未使用，1=已分配）
     */
    private AddressStatus used;


}
