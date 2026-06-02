package com.yan.xpay.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yan.xpay.enums.Chain;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商家冷钱包地址
 */
@Data
@TableName("t_merchant_address")
public class MerchantAddress {
	@Serial
	private static final long serialVersionUID = 1L;
	@TableId(value = "id")
	private Long id;
	private Long merchantId;
	private Chain chain;
	private String symbol;
	private String coldAddress;
	private BigDecimal collectAmount;
	private String hotAddress;
}
