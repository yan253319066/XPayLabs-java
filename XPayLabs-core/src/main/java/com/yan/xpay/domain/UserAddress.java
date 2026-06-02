package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yan.xpay.enums.AddressStatus;
import com.yan.xpay.enums.Chain;
import com.yan.xpay.enums.UserAddressCollectible;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 用户地址
 */
@Data
@TableName("t_user_address")
public class UserAddress {
	@Serial
	private static final long serialVersionUID = 1L;
	@TableId(value = "id")
	private Long id;
	private Long merchantId;
	private String userId;
	private Chain chain;
	private String symbol;
	private String address;
	private BigDecimal amount;
	private UserAddressCollectible collectible;
	private AddressStatus status;
}
